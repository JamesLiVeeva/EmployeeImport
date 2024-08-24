package org.humanresources.processor;

import org.humanresources.model.Employee;
import org.humanresources.validator.ValidationChain;

import java.io.File;
import java.io.IOException;
import java.util.List;

public interface FileProcessor {

    List<Employee> process(File file) throws IOException;

    void setFieldValidators(ValidationChain fieldValidators);

    void setDelimiter(String delimiter);

}
