package ru.practicum.dto.compilation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.dto.event.EventShortDto;

import java.util.HashSet;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CompilationDto {
    private Long id;
    @Builder.Default
    private Set<EventShortDto> events = new HashSet<>();
    private Boolean pinned;
    private String title;

    public void addEvent(EventShortDto event) {
        events.add(event);
    }

    public void removeEvent(EventShortDto event) {
        events.remove(event);
    }
}
