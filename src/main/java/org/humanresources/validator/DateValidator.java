package org.humanresources.validator;

import org.humanresources.model.Employee;
import org.humanresources.utils.DateUtil;

import java.time.format.DateTimeParseException;

public class DateValidator implements Validator<Employee> {

    @Override
    public boolean validate(Employee employee) {
        try {
            DateUtil.formatDate(employee.getOnboardingDate());
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }
    
}
