package org.humanresources.utils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DateUtil {

    private static final DateTimeFormatter INPUT_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy/M/d");
    private static final DateTimeFormatter OUTPUT_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static String formatDate(String date) {
        return LocalDate.parse(date, INPUT_DATE_FORMATTER).format(OUTPUT_DATE_FORMATTER);
    }

}
