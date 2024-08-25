package org.humanresources.processor;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.humanresources.model.Employee;
import org.humanresources.validator.ValidationChain;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

public class CSVFileProcessor implements FileProcessor {

    private ValidationChain fieldValidators;

    @Override
    public void setFieldValidators(ValidationChain fieldValidators) {
        this.fieldValidators = fieldValidators;
    }

    @Override
    public void setDelimiter(String delimiter) {
    }

    @Override
    public List<Employee> process(File csvFile) throws IOException {
        List<Employee> employees = new ArrayList<>();

        Reader csvReader = new FileReader(csvFile);
        CSVParser csvParser = new CSVParser(csvReader,
                CSVFormat.Builder.create().setHeader().setSkipHeaderRecord(true).setTrim(true).build());

        try (csvReader; csvParser) {
            for (CSVRecord csvRecord : csvParser) {
                Employee employee = geEmployee(csvRecord);
                if (fieldValidators.validate(employee)){
                    employees.add(employee);
                } else {
                    System.out.println("Employee record is not well-formatted: " + csvRecord);
                }
            }
        }
        return employees;
    }

    private Employee geEmployee(CSVRecord csvRecord) {
        return Employee.builder()
                .employeeId(csvRecord.get("Employee ID"))
                .firstName(csvRecord.get("First Name"))
                .lastName(csvRecord.get("Last Name"))
                .office(csvRecord.get("Office"))
                .roles(List.of(csvRecord.get("Role").split(",")))
                .onboardingDate(csvRecord.get("On Board Date"))
                .build();
    }

}
