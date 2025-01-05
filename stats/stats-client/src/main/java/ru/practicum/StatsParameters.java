package ru.practicum;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import java.util.List;

/**
 * A helper class to group the parameters desired for fetching statistics.
 *
 * @param <R> The type of the class that will be used when returning the statistics.
 */
@Getter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StatsParameters<R> {
    String start;
    String end;
    List<String> uris;
    boolean unique;
    Class<R> responseType;
}
