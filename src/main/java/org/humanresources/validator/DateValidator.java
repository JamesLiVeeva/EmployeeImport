package org.humanresources.validator;

import org.humanresources.model.Employee;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class DateValidator implements Validator<Employee> {
    
    private static final DateTimeFormatter defaultFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    @Override
    public boolean validate(Employee employee) {
        try {
            LocalDate.parse(employee.getOnboardingDate(), defaultFormatter);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }
    
}
