package ru.practicum.constants;

import java.time.format.DateTimeFormatter;

public interface DataTransferConvention {
    String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(DATE_TIME_PATTERN);
    String STAT_SERVICE_URL = "http://stats-server:9090";
    String STATS_PATH = "/stats";
    String HIT_PATH = "/hit";
}
