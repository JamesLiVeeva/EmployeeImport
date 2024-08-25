package org.humanresources.wonderhealth;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import org.humanresources.constants.URIConstants;
import org.humanresources.service.HttpClientService;
import org.humanresources.model.Employee;
import org.humanresources.model.*;
import org.humanresources.processor.FileProcessorFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.humanresources.constants.URIConstants.*;

@Component
public class UserDataImport implements ApplicationRunner {

    private static final String HUMAN_RESOURCES_BUCKET_NAME = "human-resources-bucket-for-demo";

    private static final String TEMP_FILE_PATH = "/tmp/";

    private static final String FILE_NAME_PREFIX_EMPLOYEE_INSERT = "Insert_Employees_";
    private static final String FILE_NAME_PREFIX_EMPLOYEE_UPDATE = "Update_Employees_";
    private static final String FILE_NAME_PREFIX_OFFICE_INSERT = "Insert_Offices_";

    private static final String CSV_HEADER_EMPLOYEE_INSERT = "employee_id__c,office__c,first_name__c,last_name__c,role__c,on_board_date__c,name__v";
    private static final String CSV_HEADER_EMPLOYEE_UPDATE = "id,employee_id__c,office__c,first_name__c,last_name__c,role__c,on_board_date__c,name__v";
    private static final String CSV_HEADER_OFFICE_INSERT = "name__v";

    private final AmazonS3 s3Client;
    private final HttpClientService httpClientService;

    private final ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    private String sessionId = null;

    private Map<String, Employee> existingEmployees = new HashMap<>();
    private Map<String, Role> existingRoles = new HashMap<>();
    private Map<String, Office> existingOffices = new HashMap<>();

    @Autowired
    public UserDataImport(AmazonS3 s3Client, HttpClientService httpClientService) {
        this.s3Client = s3Client;
        this.httpClientService = httpClientService;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        prepare();
        startProcess();
        shutdown();
    }

    private void prepare() throws JSONException, IOException {
        // this session id is used to get/post/put data to vault system, we need to extend session valid duration if the process takes a long time to run
        sessionId = httpClientService.getSessionId();
        // load Vault data to memory for validation, the return data is built into a map so we could get the needed object directly without looping the data
        existingEmployees = httpClientService.retrieveEmployees(URIConstants.VAULT_API_URL + URIConstants.EMPLOYEE_OBJECTS_WITH_FIELDS, sessionId);
        existingOffices = httpClientService.retrieveOffices(URIConstants.VAULT_API_URL + URIConstants.OFFICE_OBJECTS_WITH_FIELDS, sessionId);
        existingRoles = httpClientService.retrieveRoles(URIConstants.VAULT_API_URL + URIConstants.ROLE_PICKLISTS, sessionId);
    }

    private void shutdown(){
        executor.shutdown();
    }

    private void startProcess() throws IOException, JSONException {
        // the configurations for departments are added to the enum, this could be configured in files or DB table for more complex cases
        for (Department department : Department.values()) {

            S3ObjectSummary fileObjectSummary = getUserDataFileToImport(department);
            if(fileObjectSummary != null) {

                List<Employee> employeeRecords = FileProcessorFactory.getFileProcessor(department).process(downloadFile(fileObjectSummary));
                // create new offices and roles first, so the employees could reference them
                processOfficesAndRoles(employeeRecords);
                processEmployees(employeeRecords);

                archiveProcessedFile(fileObjectSummary.getKey());
            }
        }
    }

    private void processEmployees(List<Employee> employeeRecords) {
        List<Employee> employeesToInsert = new ArrayList<>();
        List<Employee> employeesToUpdate = new ArrayList<>();

        for (Employee employee : employeeRecords) {
            if (existingEmployees.containsKey(employee.getEmployeeId())) {
                employeesToUpdate.add(employee);
            } else {
                employeesToInsert.add(employee);
            }
        }

        processEmployeeRecordsByActionType(employeesToInsert, false);
        processEmployeeRecordsByActionType(employeesToUpdate, true);
    }

    private void processOfficesAndRoles(List<Employee> employeeRecords) throws JSONException, IOException {
        Set<String> newOffices = new HashSet<>();
        Set<String> newRoles = new HashSet<>();

        for (Employee employee : employeeRecords) {
            if(!existingOffices.containsKey(employee.getOffice())){
                newOffices.add(employee.getOffice());
            }
            employee.getRoles().forEach(role ->{
                if(!existingRoles.containsKey(role)){
                    newRoles.add(role);
                }
            });

        }

        createNewOffices(newOffices);
        // createNewRoles(newRoles); -- my current user has no permission to create new roles

        // refresh data
        existingOffices = httpClientService.retrieveOffices(URIConstants.VAULT_API_URL + URIConstants.OFFICE_OBJECTS_WITH_FIELDS, sessionId);
        existingRoles = httpClientService.retrieveRoles(URIConstants.VAULT_API_URL + URIConstants.ROLE_PICKLISTS, sessionId);

    }

    private void createNewOffices(Set<String> offices) {
        int chunkSize = 500;
        List<String> newOffices = new ArrayList<>(offices);
        for (int i = 0; i < newOffices.size(); i += chunkSize) {
            final int start = i;
            final int end = Math.min(i + chunkSize, newOffices.size());

            List<String> officesChunk = newOffices.subList(start, end);

            // we could get the execution results by FutureTask if needed
            executor.submit(() -> {
                try {
                    File officesInsertFile = createCSVFile(FILE_NAME_PREFIX_OFFICE_INSERT, start/chunkSize);
                    writeOfficeRecordsToCSV(officesInsertFile, officesChunk);
                    System.out.println("Create new offices: ");
                    httpClientService.postDataWithCSVFile(VAULT_API_URL + OFFICE_OBJECTS, officesInsertFile, sessionId);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

    private void createNewRoles(Set<String> roles) {
        int chunkSize = 500;
        List<String> newRoles = new ArrayList<>(roles);
        for (int i = 0; i < newRoles.size(); i += chunkSize) {
            final int end = Math.min(i + chunkSize, newRoles.size());
            List<String> rolesChunk = newRoles.subList(i, end);

            // we could get the execution results by FutureTask if needed
            executor.submit(() -> {
                try {
                    AtomicInteger index = new AtomicInteger(1);
                    String requestBody = rolesChunk.stream()
                            .map(newRole ->  "value_" + (index.getAndIncrement()) + "=" + newRole)
                            .collect(Collectors.joining("&"));
                    System.out.println("Create new roles: ");
                    httpClientService.postDataWithTextEntity(VAULT_API_URL + ROLE_PICKLISTS, sessionId, requestBody);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

    private File downloadFile(S3ObjectSummary fileObjectSummary) {
        GetObjectRequest objectRequest = new GetObjectRequest(HUMAN_RESOURCES_BUCKET_NAME, fileObjectSummary.getKey());
        File csvFile = new File(TEMP_FILE_PATH + fileObjectSummary.getKey());
        s3Client.getObject(objectRequest, csvFile);
        return csvFile;
    }

    private void archiveProcessedFile(String objectKey) {
        CopyObjectRequest copyObjRequest = new CopyObjectRequest(HUMAN_RESOURCES_BUCKET_NAME,
                objectKey, HUMAN_RESOURCES_BUCKET_NAME, "archive/" + objectKey);
        s3Client.copyObject(copyObjRequest);

        DeleteObjectRequest deleteObjRequest = new DeleteObjectRequest(HUMAN_RESOURCES_BUCKET_NAME, objectKey);
        s3Client.deleteObject(deleteObjRequest);
    }

    private S3ObjectSummary getUserDataFileToImport(Department department){
        ListObjectsV2Result objects = s3Client.listObjectsV2(new ListObjectsV2Request()
                .withBucketName(HUMAN_RESOURCES_BUCKET_NAME)
                .withPrefix(department.getFilePath()));
        for (S3ObjectSummary objectSummary : objects.getObjectSummaries()) {
            String objectSummaryKey = objectSummary.getKey();
            if(isTargetFileToProcess(department, objectSummaryKey)){
                return objectSummary;
            }
        }
        return null;
    }

    private boolean isTargetFileToProcess(Department department, String filename){
        boolean result = false;
        if(filename != null ) {
            if(filename.contains("/")){
                filename = filename.substring(filename.lastIndexOf("/") + 1);
            }

            if(filename.toLowerCase().startsWith(department.getFileNamePrefix().toLowerCase())
                    && filename.toLowerCase().endsWith(department.getFileType().getExtension().toLowerCase())){
                String dateInFile = filename.substring(department.getFileNamePrefix().length(), filename.length() - department.getFileType().getExtension().length() - 1);
                DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(department.getDatePattern());
                try{
                    dateTimeFormatter.parse(dateInFile);
                    result = true;
                } catch (DateTimeParseException exp){
                    System.out.println("Fail to parse date in filename: " + filename);
                }
            }
        }

        return result;
    }

    private void processEmployeeRecordsByActionType(List<Employee> employees, boolean isUpdateAction) {
        int chunkSize = 500;
        for (int i = 0; i < employees.size(); i += chunkSize) {
            final int start = i;
            final int end = Math.min(i + chunkSize, employees.size());

            List<Employee> employeesChunk = employees.subList(start, end);
            // we could get the execution results by Future interface if needed
            executor.submit(() -> {
                try {
                    File employeesUpdateFile = createCSVFile(isUpdateAction? FILE_NAME_PREFIX_EMPLOYEE_UPDATE : FILE_NAME_PREFIX_EMPLOYEE_INSERT, start/chunkSize);
                    writeEmployeeRecordsToCSV(employeesUpdateFile, employeesChunk, isUpdateAction);

                    if(isUpdateAction){
                        System.out.println("Update existing employees: ");
                        httpClientService.putDataWithCSVFile(VAULT_API_URL + EMPLOYEE_OBJECTS, employeesUpdateFile, sessionId);
                    }else {
                        System.out.println("Create new employees: ");
                        httpClientService.postDataWithCSVFile(VAULT_API_URL + EMPLOYEE_OBJECTS, employeesUpdateFile, sessionId);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

    private void writeOfficeRecordsToCSV(File csvFile, List<String> newOffices) {
        if(!CollectionUtils.isEmpty(newOffices)) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(csvFile))) {
                writer.append(CSV_HEADER_OFFICE_INSERT).append("\n");
                for (String office: newOffices) {
                    writer.append(office).append("\n");
                }
                writer.flush();
            } catch (IOException e) {
                System.out.println("Fail to write offices to csv file with exception: " + e.getMessage());
            }
        }
    }

    private void writeEmployeeRecordsToCSV(File csvFile, List<Employee> employees, boolean isUpdate) throws IOException{
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(csvFile))) {
            writer.append(isUpdate? CSV_HEADER_EMPLOYEE_UPDATE : CSV_HEADER_EMPLOYEE_INSERT).append("\n");
            for (Employee employee : employees) {
                writer.append(isUpdate?  getEmployeeObjectId(employee.getEmployeeId()) + "," : "");
                writer.append(employee.getEmployeeId()).append(",")
                        .append(getOfficeObjectId(employee.getOffice())).append(",")
                        .append(employee.getFirstName()).append(",")
                        .append(employee.getLastName()).append(",\"")
                        .append(getRoleObjectValueName(employee.getRoles())).append("\",")
                        .append(employee.getOnboardingDate()).append(",")
                        .append(employee.getFirstName()).append(" ")
                        .append(employee.getLastName()).append("\n");

            }
            writer.flush();
        } catch (IOException e) {
            System.out.println("Fail to write employees to csv file with exception: " + e.getMessage());
        }
    }

    private static File createCSVFile(String fileNamePrefix, int fileCount) throws RuntimeException, IOException {
        File fileToProcess = new File(TEMP_FILE_PATH + fileNamePrefix + fileCount + ".csv");
        fileToProcess.getParentFile().mkdirs();
        fileToProcess.createNewFile();
        return fileToProcess;
    }

    private String getRoleObjectValueName(List<String> roleInRecord){
        if(!CollectionUtils.isEmpty(roleInRecord)){
            return roleInRecord.stream().map(role -> {
                if (existingRoles != null && existingRoles.containsKey(role)){
                    return existingRoles.get(role);
                }
                return null;
            }).filter(Objects::nonNull).map(Role::getName).collect(Collectors.joining(","));
        }
        return "";
    }


    private String getOfficeObjectId(String officeNameInRecord){
        if(existingOffices != null && existingOffices.containsKey(officeNameInRecord)){
            return existingOffices.get(officeNameInRecord).getId();
        }
        return "";
    }

    private String getEmployeeObjectId(String employeeIdInRecord){
        if(existingEmployees != null && existingEmployees.containsKey(employeeIdInRecord)){
            return existingEmployees.get(employeeIdInRecord).getId();
        }
        return "";
    }

}