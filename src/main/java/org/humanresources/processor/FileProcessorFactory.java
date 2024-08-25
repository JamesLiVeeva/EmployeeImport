package org.humanresources.processor;

import org.humanresources.validator.ValidationChainBuilder;
import org.humanresources.wonderhealth.Department;

public class FileProcessorFactory {

    public static FileProcessor getFileProcessor(Department department) {
        FileProcessor processor;
        switch (department.getFileType()) {
            case CSV:
                processor =  new CSVFileProcessor();
                processor.setFieldValidators(ValidationChainBuilder.buildDefaultValidationChain());
                processor.setDelimiter(department.getDelimiter());
                return processor;
            case TXT:
                processor =  new TXTFileProcessor();
                processor.setFieldValidators(ValidationChainBuilder.buildDefaultValidationChain());
                processor.setDelimiter(department.getDelimiter());
                return processor;
            default:
                throw new IllegalArgumentException("Unsupported file type with extension of: "
                        + department.getFileType().name());
        }
    }

}
