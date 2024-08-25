package org.humanresources.processor;

import org.humanresources.model.Employee;
import org.humanresources.validator.ValidationChain;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class TXTFileProcessor implements FileProcessor {

    private static final int EMPLOYEE_ID_INDEX = 0;
    private static final int OFFICE_INDEX = 1;
    private static final int FIRST_NAME_INDEX = 2;
    private static final int LAST_NAME_INDEX = 3;
    private static final int ROLE_INDEX = 4;
    private static final int ONBOARDING_DATE_INDEX = 5;

    private ValidationChain fieldValidators;
    private String delimiter;

    public List<Employee> process(File txtFile) throws IOException {
        List<Employee> employees = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(txtFile))) {
            String userInfo;
            reader.readLine(); // remove header
            while ((userInfo = reader.readLine()) != null) {
                Employee employee = geEmployee(userInfo.split(delimiter));
                if (fieldValidators.validate(employee)){
                    employees.add(employee);
                } else {
                    System.out.println("Employee record is not well-formatted: " + userInfo);
                }
            }
        }
        return employees;
    }

    @Override
    public void setFieldValidators(ValidationChain fieldValidators) {
        this.fieldValidators = fieldValidators;
    }

    @Override
    public void setDelimiter(String delimiter) {
        this.delimiter = delimiter;
    }

    private Employee geEmployee(String[] columns) {
        return Employee.builder()
                .employeeId(columns[EMPLOYEE_ID_INDEX])
                .firstName(columns[FIRST_NAME_INDEX])
                .lastName(columns[LAST_NAME_INDEX])
                .office(columns[OFFICE_INDEX])
                .roles(List.of(columns[ROLE_INDEX].split(",")))
                .onboardingDate(columns[ONBOARDING_DATE_INDEX])
                .build();
    }

}
