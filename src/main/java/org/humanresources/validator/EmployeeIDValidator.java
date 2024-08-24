package org.humanresources.validator;

import org.humanresources.model.Employee;

public class EmployeeIDValidator implements Validator<Employee> {

    @Override
    public boolean validate(Employee employee) {
        return employee.getEmployeeId() != null && !employee.getEmployeeId().isBlank();
    }
}
