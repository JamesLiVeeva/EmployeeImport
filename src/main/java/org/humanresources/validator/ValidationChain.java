package org.humanresources.validator;

import java.util.ArrayList;
import java.util.List;

public class ValidationChain<T> {

    private final List<Validator<T>> validators = new ArrayList<>();

    public void addValidator(Validator<T> validator) {
        validators.add(validator);
    }

    public boolean validate(T data) {
        for (Validator<T> validator : validators) {
            if (!validator.validate(data)) {
                return false;
            }
        }
        return true;
    }
}
