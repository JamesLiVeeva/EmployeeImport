package org.humanresources.validator;

import org.humanresources.model.Employee;

public class ValidationChainBuilder {

    public static ValidationChain<Employee> getDefaultValidationChain() {
        ValidationChain<Employee> chain = new ValidationChain<>();
        chain.addValidator(new EmployeeIDValidator());
        chain.addValidator(new UserNameValidator());
        chain.addValidator(new DateValidator());
        return chain;
    }

}
