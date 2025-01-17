package ru.practicum;

import java.time.format.DateTimeFormatter;

public interface DataTransferConvention {
    String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(DATE_TIME_PATTERN);
    String FROM = "0";
    String SIZE = "10";
}
