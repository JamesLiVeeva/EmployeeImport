package org.humanresources.utils;

import lombok.Getter;

@Getter
public enum FileType {

    CSV("csv"),
    TXT("txt");

    private final String extension;

    FileType(String extension) {
        this.extension = extension;
    }
}
