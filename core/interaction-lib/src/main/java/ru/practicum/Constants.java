package ru.practicum;

import lombok.experimental.UtilityClass;

import java.time.LocalDateTime;

@UtilityClass
public class Constants {
    public static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final LocalDateTime DEFAULT_SEARCH_START_DATE = LocalDateTime.now().minusYears(666L);
}