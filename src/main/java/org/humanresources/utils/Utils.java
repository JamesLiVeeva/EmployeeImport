package org.humanresources.utils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Utils {

    private static final DateTimeFormatter INPUT_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy/M/d");
    private static final DateTimeFormatter OUTPUT_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static String formatDate(String dateStr) {
        LocalDate date = LocalDate.parse(dateStr, INPUT_DATE_FORMATTER);
        return date.format(OUTPUT_DATE_FORMATTER);
    }

}
