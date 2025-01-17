package ru.practicum.util;

import ru.practicum.dto.event.EventDto;

import java.util.Comparator;

public class EventDtoByDateTimeComparator implements Comparator<EventDto> {
    @Override
    public int compare(EventDto x, EventDto y) {
        return x.getCreatedOn().isBefore(y.getCreatedOn()) ? -1 :
                x.getCreatedOn().isAfter(y.getCreatedOn()) ? 1 : 0;
    }
}
