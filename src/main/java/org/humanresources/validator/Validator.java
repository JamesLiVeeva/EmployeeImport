package org.humanresources.validator;

public interface Validator<T> {

    boolean validate(T input);

}
