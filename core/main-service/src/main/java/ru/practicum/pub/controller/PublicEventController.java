package ru.practicum.pub.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.DataTransferConvention;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.dto.event.SortCriterium;
import ru.practicum.pub.service.PublicEventService;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Обратите внимание:
 * <p>
 *     <ul>
 *         <li>это публичный эндпоинт, соответственно в выдаче должны быть только опубликованные события</li>
 *         <li>текстовый поиск (по аннотации и подробному описанию) должен быть без учета регистра букв</li>
 *         <li>если в запросе не указан диапазон дат [rangeStart-rangeEnd], то нужно выгружать события,
 *             которые произойдут позже текущей даты и времени</li>
 *         <li>информация о каждом событии должна включать в себя количество просмотров
 *             и количество уже одобренных заявок на участие</li>
 *         <li>информацию о том, что по этому эндпоинту был осуществлен и обработан запрос,
 *             нужно сохранить в сервисе статистики</li>
 *     </ul>
 * </p>
 * В случае, если по заданным фильтрам не найдено ни одного события, возвращает пустой список
 */
@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class PublicEventController {
    private final PublicEventService publicEventService;

    @GetMapping
    public ResponseEntity<List<EventShortDto>> getEvents(@RequestParam(required = false) String text,
                                                         @RequestParam(required = false) Long[] categories,
                                                         @RequestParam(required = false) Boolean paid,
                                                         @RequestParam(required = false)
                                                         @DateTimeFormat(pattern = DataTransferConvention.DATE_TIME_PATTERN)
                                                         LocalDateTime rangeStart,
                                                         @RequestParam(required = false)
                                                         @DateTimeFormat(pattern = DataTransferConvention.DATE_TIME_PATTERN)
                                                         LocalDateTime rangeEnd,
                                                         @RequestParam(defaultValue = "false")
                                                         Boolean onlyAvailable,
                                                         @RequestParam(required = false) SortCriterium sort,
                                                         @RequestParam(defaultValue = DataTransferConvention.FROM)
                                                         Integer from,
                                                         @RequestParam(defaultValue = DataTransferConvention.SIZE)
                                                         Integer size, HttpServletRequest request) {
        return new ResponseEntity<>(
                publicEventService.getEvents(text, categories, paid, rangeStart, rangeEnd, onlyAvailable, sort, from, size,
                        request),
                HttpStatus.OK);
    }

    /**
     * Обратите внимание:
     * событие должно быть опубликовано
     * информация о событии должна включать в себя количество просмотров и количество подтвержденных запросов
     * информацию о том, что по этому эндпоинту был осуществлен и обработан запрос, нужно сохранить в сервисе статистики
     * В случае, если события с заданным id не найдено, возвращает статус код 404
     */
    @GetMapping("/{id}")
    public ResponseEntity<EventFullDto> getEvent(@PathVariable Long id, HttpServletRequest request) {
        return new ResponseEntity<>(publicEventService.getEvent(id, request), HttpStatus.OK);
    }
}
