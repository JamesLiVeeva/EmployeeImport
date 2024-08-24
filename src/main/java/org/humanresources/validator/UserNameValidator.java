package org.humanresources.validator;

import org.humanresources.model.Employee;

public class UserNameValidator implements Validator<Employee> {

    @Override
    public boolean validate(Employee employee) {
        return (employee.getFirstName() != null && !employee.getFirstName().isBlank()) &&
                (employee.getLastName() != null && !employee.getLastName().isBlank());
    }
}
