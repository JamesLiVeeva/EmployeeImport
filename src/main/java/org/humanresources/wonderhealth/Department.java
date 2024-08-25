package org.humanresources.wonderhealth;

import lombok.Getter;
import org.humanresources.processor.FileType;

@Getter
public enum Department {

    WonderDrugs("WonderDrugs", "WonderDrugsHR-", "yyyy-MM-dd", FileType.CSV, ""),
    WonderPharma("WonderPharma", "WonderPharmaHR-", "yyyy-MM-dd", FileType.CSV, ""),
    WonderBio("WonderBio", "HRExported-", "yyyy-MM-dd",FileType.TXT, "\\|");

    private final String filePath;
    private final String fileNamePrefix;
    private final String datePattern;
    private final FileType fileType;
    private final String delimiter;

    Department(String filePath, String fileNamePrefix, String datePattern, FileType fileType, String delimiter) {
        this.filePath = filePath;
        this.fileNamePrefix = fileNamePrefix;
        this.datePattern = datePattern;
        this.fileType = fileType;
        this.delimiter = delimiter;
    }

}
