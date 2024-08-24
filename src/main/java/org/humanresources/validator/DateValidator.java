package org.humanresources.validator;

import org.humanresources.model.Employee;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class DateValidator implements Validator<Employee> {
    
    private static final String DEFAULT_DATE_PATTERN = "yyyy-MM-dd";
    
    @Override
    public boolean validate(Employee employee) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DEFAULT_DATE_PATTERN);
            LocalDate.parse(employee.getOnboardingDate(), formatter);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }
    
}
