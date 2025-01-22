package ru.practicum.event.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import ru.practicum.category.model.Category;
import ru.practicum.category.repository.CategoryRepository;
import ru.practicum.client.request.RequestClient;
import ru.practicum.client.user.UserClient;
import ru.practicum.dto.StatRequestDto;
import ru.practicum.dto.StatResponseDto;
import ru.practicum.dto.event.*;
import ru.practicum.dto.request.EventRequestStatusUpdateRequestDto;
import ru.practicum.dto.request.EventRequestStatusUpdateResultDto;
import ru.practicum.dto.request.ParticipationRequestDto;
import ru.practicum.dto.user.UserShortDto;
import ru.practicum.enums.*;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.mapper.LocationMapper;
import ru.practicum.event.model.Event;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.event.repository.EventSpecifications;
import ru.practicum.exception.DataTimeException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.RestrictionsViolationException;
import ru.practicum.feign.StatsServerHttpClient;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static ru.practicum.Constants.DEFAULT_SEARCH_START_DATE;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {
    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;
    private final StatsServerHttpClient statClient;
    private final UserClient userClient;
    private final RequestClient requestClient;
    private final EventMapper eventMapper;
    private final LocationMapper locationMapper;
    @Value("${spring.application.name}")
    private String appName;

    @Override
    @Transactional
    public EventFullDto addEvent(NewEventDto newEventDto, long userId) {
        log.info("Adding new event for userId={} with event data: {}", userId, newEventDto);
        verifyUserExists(userId);
        Category category = fetchCategory(newEventDto.getCategory());
        validateEventDate(newEventDto.getEventDate(), 2);
        setDefaultValuesForEvent(newEventDto);
        Event newEvent = mapNewEventDtoToEvent(newEventDto, category, userId);
        Event savedEvent = eventRepository.save(newEvent);
        log.debug("Event saved to repository: {}", savedEvent);
        EventFullDto eventFullDto = eventMapper.eventToEventFullDto(savedEvent);
        eventFullDto.setViews(0L);
        log.info("Successfully added event: {}", eventFullDto);
        return eventFullDto;
    }

    private void verifyUserExists(long userId) {
        log.debug("Verifying if user with id={} exists", userId);
        if (!userClient.getUserExists(userId)) {
            log.error("User with id={} not found", userId);
            throw new NotFoundException("User with id=" + userId + " was not found");
        }
        log.debug("User with id={} exists", userId);
    }

    private Category fetchCategory(Long categoryId) {
        return fetchEntityById(
                () -> categoryRepository.findById(categoryId),
                "Category with id=" + categoryId + " was not found"
        );
    }

    private void validateEventDate(LocalDateTime eventDate, long minHoursAhead) {
        log.debug("Validating event date: {}", eventDate);
        if (eventDate.isBefore(LocalDateTime.now().plusHours(minHoursAhead))) {
            log.error("Invalid event date: {}. Must be at least {} hours ahead.", eventDate, minHoursAhead);
            throw new DataTimeException("The date and time of the event cannot be earlier than "
                    + minHoursAhead + " hours from now.");
        }
        log.debug("Event date {} is valid", eventDate);
    }

    private void setDefaultValuesForEvent(NewEventDto newEventDto) {
        log.debug("Setting default values for event if not specified");
        Boolean paid = newEventDto.getPaid();
        newEventDto.setPaid(paid != null ? paid : false);
        Boolean requestModeration = newEventDto.getRequestModeration();
        newEventDto.setRequestModeration(requestModeration != null ? requestModeration : true);
        Long participantLimit = newEventDto.getParticipantLimit();
        newEventDto.setParticipantLimit(participantLimit != null ? participantLimit : 0L);
        log.debug("Default values set for newEventDto: {}", newEventDto);
    }

    private Event mapNewEventDtoToEvent(NewEventDto newEventDto, Category category, long userId) {
        log.debug("Mapping NewEventDto to Event entity");
        Event event = eventMapper.newEventDtoToEvent(newEventDto);
        event.setCategory(category);
        event.setCreatedOn(LocalDateTime.now());
        event.setInitiatorId(userId);
        event.setPublishedOn(LocalDateTime.now());
        event.setState(State.PENDING);
        event.setConfirmedRequests(0L);
        log.debug("Mapped Event entity: {}", event);
        return event;
    }

    @Override
    public EventFullDto findEventByUserIdAndEventId(long userId, long eventId, HttpServletRequest request) {
        log.info("Fetching event by userId={} and eventId={}", userId, eventId);
        verifyUserExists(userId);
        Event event = fetchEventById(eventId);
        long views = fetchEventViews(event, request.getRequestURI());
        EventFullDto eventFullDto = eventMapper.eventToEventFullDto(event);
        eventFullDto.setViews(views);
        log.info("Successfully fetched event: {}", eventFullDto);
        return eventFullDto;
    }

    private long fetchEventViews(Event event, String requestURL) {
        log.debug("Fetching view stats for event id={}", event.getId());
        List<StatResponseDto> viewStats = getViewStats(List.of(event), requestURL);
        if (!CollectionUtils.isEmpty(viewStats)) {
            long views = viewStats.get(0).getHits();
            log.debug("Fetched views: {}", views);
            return views;
        }
        log.debug("No view stats found for event id={}", event.getId());
        return 0L;
    }

    private Event fetchEventById(long eventId) {
        log.debug("Fetching event with id={}", eventId);
        return fetchEntityById(
                () -> eventRepository.findById(eventId),
                "Event with id=" + eventId + " not found"
        );

    }

    @Override
    public List<EventShortDto> findEventsByUser(long userId, int from, int size, HttpServletRequest request) {
        log.info("Fetching events created by userId={} from={} size={}", userId, from, size);
        verifyUserExists(userId);
        List<Event> userEvents = fetchUserEvents(userId, from, size);
        setViews(userEvents, request.getRequestURI());
        List<EventShortDto> eventShortDtos = eventMapper.listEventToListEventShortDto(userEvents);
        log.info("Successfully fetched {} events for userId={}", eventShortDtos.size(), userId);
        return eventShortDtos;
    }

    private List<Event> fetchUserEvents(long userId, int from, int size) {
        log.debug("Fetching events for userId={} with pagination: from={}, size={}", userId, from, size);
        PageRequest pageRequest = PageRequest.of(from, size);
        Page<Event> pageEvents = eventRepository.findByInitiatorId(userId, pageRequest);
        List<Event> events = pageEvents.getContent();
        if (events.isEmpty()) {
            log.warn("No events found for userId={}", userId);
        } else {
            log.debug("Fetched {} events for userId={}", events.size(), userId);
        }
        return events;
    }

    @Override
    @Transactional
    public EventFullDto updateEvent(UpdateEventUserRequest updateEvent, long userId, long eventId) {
        log.info("Updating event with eventId={} for userId={}", eventId, userId);
        verifyUserExists(userId);
        Event event = fetchEventById(eventId);
        validateEventStateForUpdate(event);
        updateEventFields(updateEvent, event);
        EventFullDto updatedEventDto = eventMapper.eventToEventFullDto(event);
        log.info("Successfully updated event with eventId={} for userId={}", eventId, userId);
        return updatedEventDto;
    }

    private void validateEventStateForUpdate(Event event) {
        if (event.getState().equals(State.PUBLISHED)) {
            log.error("Cannot update published event with id={}", event.getId());
            throw new RestrictionsViolationException("You can only change canceled events or events" +
                    " in the waiting state for moderation");
        }
    }

    private void updateEventFields(UpdateEventUserRequest updateEvent, Event event) {
        log.debug("Updating fields for event with id={}", event.getId());
        updateField(updateEvent.getAnnotation(), event::setAnnotation);
        updateField(updateEvent.getCategory(), categoryId ->
                event.setCategory(fetchCategory(categoryId))
        );
        updateField(updateEvent.getDescription(), event::setDescription);
        updateField(updateEvent.getEventDate(), eventDate -> {
            validateEventDate(eventDate, 2);
            event.setEventDate(eventDate);
        });
        updateField(updateEvent.getLocation(),
                location -> event.setLocation(locationMapper.locationDtoToLocation(location))
        );
        updateField(updateEvent.getPaid(), event::setPaid);
        updateField(updateEvent.getParticipantLimit(), event::setParticipantLimit);
        updateField(updateEvent.getRequestModeration(), event::setRequestModeration);
        updateField(updateEvent.getTitle(), event::setTitle);
        updateField(updateEvent.getStateAction(), stateAction -> updateStateAction(event, stateAction));
        log.debug("Fields for event with id={} have been updated", event.getId());
    }

    private <T> void updateField(T field, Consumer<T> updater) {
        if (field != null) {
            updater.accept(field);
        }
    }

    private void updateStateAction(Event event, StateActionUser stateAction) {
        switch (stateAction) {
            case CANCEL_REVIEW -> event.setState(State.CANCELED);
            case SEND_TO_REVIEW -> event.setState(State.PENDING);
        }
    }

    @Override
    public List<ParticipationRequestDto> findRequestByEventId(long userId, long eventId) {
        log.info("Fetching participation requests for userId={} and eventId={}", userId, eventId);
        verifyUserExists(userId);
        verifyEventExists(eventId);
        List<ParticipationRequestDto> requests = requestClient.findAllRequestsByEventId(eventId);
        log.info("Successfully fetched {} participation requests for eventId={}", requests.size(), eventId);
        return requests;
    }


    private void verifyEventExists(long eventId) {
        log.debug("Verifying existence of event with id={}", eventId);
        if (!eventRepository.existsById(eventId)) {
            log.error("Event with id={} not found", eventId);
            throw new NotFoundException("Event with id=" + eventId + " was not found");
        }
        log.debug("Event with id={} exists", eventId);
    }

    @Transactional
    @Override
    public EventRequestStatusUpdateResultDto updateRequestByEventId(EventRequestStatusUpdateRequestDto updateRequests,
                                                                    long userId,
                                                                    long eventId) {
        log.info("Starting process to update requests. User ID: {}, Event ID: {}", userId, eventId);
        verifyUserExists(userId);
        Event event = getEventIfExists(eventId);
        verifyParticipantLimitNotReached(event);
        List<ParticipationRequestDto> requests = getPendingRequests(updateRequests.getRequestIds());
        List<ParticipationRequestDto> updatedRequests = updateRequestStatuses(updateRequests, requests);
        if (updateRequests.getStatus() == Status.CONFIRMED) {
            incrementConfirmedRequests(event, updatedRequests.size());
        }
        log.info("Requests updated successfully for Event ID: {}", eventId);
        return createEventRequestStatusUpdateResult(updatedRequests);
    }

    private Event getEventIfExists(long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));
    }

    private void verifyParticipantLimitNotReached(Event event) {
        boolean limitReached = event.getParticipantLimit() != 0 &&
                event.getParticipantLimit().equals(event.getConfirmedRequests());
        if (limitReached) {
            throw new RestrictionsViolationException("The limit on applications for this event has been reached");
        }
    }

    private List<ParticipationRequestDto> getPendingRequests(Set<Long> requestIds) {
        List<ParticipationRequestDto> requests = requestClient.findAllRequestsByRequestsId(requestIds);
        boolean hasNonPendingRequests = requests.stream()
                .map(ParticipationRequestDto::status)
                .anyMatch(status -> status != Status.PENDING);

        if (hasNonPendingRequests) {
            throw new RestrictionsViolationException("The status can only be changed for applications that " +
                    "are in the PENDING state");
        }

        return requests;
    }

    private List<ParticipationRequestDto> updateRequestStatuses(EventRequestStatusUpdateRequestDto updateRequests,
                                                                List<ParticipationRequestDto> requests) {
        return requestClient.updateRequest(updateRequests.getRequestIds(), updateRequests.getStatus().name());
    }

    private void incrementConfirmedRequests(Event event, int confirmedCount) {
        event.setConfirmedRequests(event.getConfirmedRequests() + confirmedCount);
    }

    @Override
    @Transactional
    public List<EventShortDto> findAllPublicEvents(String text, List<Long> categories, Boolean paid,
                                                   LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                                   boolean onlyAvailable, EventPublicSort sort, int from, int size,
                                                   HttpServletRequest request) {
        log.info("Finding public events with filters: text={}, categories={}, paid={}, rangeStart={}, " +
                        "rangeEnd={}, onlyAvailable={}, sort={}, from={}, size={}",
                text, categories, paid, rangeStart, rangeEnd, onlyAvailable, sort, from, size);
        validateDateRange(rangeStart, rangeEnd);
        PageRequest pageRequest = buildPageRequest(from, size, sort, Sort.Direction.ASC);
        Page<Event> events = fetchPublicEventsWithFilters(
                text, categories, paid, rangeStart, rangeEnd, onlyAvailable, pageRequest);
        setEventViews(events.getContent(), request.getRequestURI());
        List<EventShortDto> eventShortDtos = mapToEventShortDto(events.getContent());
        log.info("Successfully found {} public events with filters: text={}, categories={}, paid={}, rangeStart={}, " +
                        "rangeEnd={}, onlyAvailable={}, sort={}",
                eventShortDtos.size(), text, categories, paid, rangeStart, rangeEnd, onlyAvailable, sort);

        events.getContent().forEach(
                event -> {
                    StatRequestDto statRequest = StatRequestDto.builder()
                            .app(appName)
                            .uri(request.getRequestURI() + "/" + event.getId())
                            .ip(request.getRemoteAddr())
                            .timestamp(LocalDateTime.now())
                            .build();
                    statClient.registerHit(statRequest);
                }
        );
        return eventShortDtos;
    }

    private void validateDateRange(LocalDateTime rangeStart, LocalDateTime rangeEnd) {
        if ((rangeStart != null) && (rangeEnd != null) && (rangeStart.isAfter(rangeEnd))) {
            log.error("Validation error: Start time {} is after end time {}", rangeStart, rangeEnd);
            throw new DataTimeException("Start time after end time");
        }
        log.debug("Date range is valid: rangeStart={}, rangeEnd={}", rangeStart, rangeEnd);
    }

    private Page<Event> fetchPublicEventsWithFilters(String text, List<Long> categories, Boolean paid,
                                                     LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                                     boolean onlyAvailable, PageRequest pageRequest) {
        log.debug("Fetching public events from repository with filters: text={}, categories={}, paid={}, " +
                        "rangeStart={}, rangeEnd={}, onlyAvailable={}, pageRequest={}",
                text, categories, paid, rangeStart, rangeEnd, onlyAvailable, pageRequest);
        Specification<Event> spec = Specification.where(EventSpecifications.withTextFilter(text))
                .and(EventSpecifications.withCategories(categories))
                .and(EventSpecifications.withPaid(paid))
                .and(EventSpecifications.withDateRange(rangeStart, rangeEnd))
                .and(EventSpecifications.withOnlyAvailable(onlyAvailable));
        Page<Event> events = eventRepository.findAll(spec, pageRequest);
        log.debug("Fetched {} events from repository", events.getContent().size());
        return events;
    }

    private void setEventViews(List<Event> events, String requestURL) {
        if (CollectionUtils.isEmpty(events)) {
            log.warn("No events found to set views");
            return;
        }
        log.debug("Setting views for {} events", events.size());
        setViews(events, requestURL);
        log.debug("Views successfully set for events");
    }

    private List<EventShortDto> mapToEventShortDto(List<Event> events) {
        log.debug("Mapping {} events to EventShortDto", events.size());
        List<EventShortDto> eventShortDtos = eventMapper.listEventToListEventShortDto(events);
        log.debug("Successfully mapped events to EventShortDto");
        return eventShortDtos;
    }

    @Override
    @Transactional
    public EventFullDto findPublicEventById(long id, HttpServletRequest request) {
        log.info("Looking for public event with id={}", id);
        Event event = fetchPublishedEventById(id);
        log.debug("Fetched published event: {}", event);
        setEventViews(event);
        log.debug("Updated event views for event with id={}: {}", id, event.getViews());
        EventFullDto eventFullDto = mapEventToFullDto(event);
        log.debug("Mapped event to EventFullDto: {}", eventFullDto);
        StatRequestDto statRequest = StatRequestDto.builder()
                .app(appName)
                .uri(request.getRequestURI())
                .ip(request.getRemoteAddr())
                .timestamp(LocalDateTime.now())
                .build();
        statClient.registerHit(statRequest);
        log.debug("Registered stat hit: {}", statRequest);
        log.info("Successfully retrieved public event with id={} and dto={}", id, eventFullDto);
        return eventFullDto;
    }

    private Event fetchPublishedEventById(long id) {
        log.debug("Fetching published event with id={}", id);
        return fetchEntityById(
                () -> eventRepository.findByIdAndState(id, State.PUBLISHED),
                "Published event with id=" + id + " not found"
        );
    }

    private void setEventViews(Event event) {
        log.debug("Setting views for event with id={}", event.getId());
        setViews(List.of(event), "/events");
        log.debug("Views successfully set for event with id={}", event.getId());
    }

    private EventFullDto mapEventToFullDto(Event event) {
        log.debug("Mapping event with id={} to EventFullDto", event.getId());
        EventFullDto eventFullDto = eventMapper.eventToEventFullDto(event);
        log.debug("Mapped EventFullDto: {}", eventFullDto);
        return eventFullDto;
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventFullDto> findAllAdminEvents(List<Long> users, State state, List<Long> categories,
                                                 LocalDateTime rangeStart, LocalDateTime rangeEnd, int from,
                                                 int size, HttpServletRequest request) {
        log.info("Fetching admin events with filters: users={}, state={}, categories={}, rangeStart={}," +
                        " rangeEnd={}, from={}, size={}",
                users, state, categories, rangeStart, rangeEnd, from, size);
        validateDateRange(rangeStart, rangeEnd);
        PageRequest pageRequest = buildPageRequest(from, size, EventPublicSort.EVENT_DATE, Sort.Direction.ASC);
        List<Event> events = fetchAdminFilteredEvents(users, state, categories, rangeStart, rangeEnd, pageRequest);
        if (events.isEmpty()) {
            log.warn("No events found for admin with the given filters");
            return Collections.emptyList();
        }
        setEventViews(events, request.getRequestURI());
        List<EventFullDto> eventFullDtos = mapEventsToFullDtos(events);
        log.info("Successfully fetched {} events for admin with filters", eventFullDtos.size());
        return eventFullDtos;
    }

    private List<Event> fetchAdminFilteredEvents(List<Long> users, State state,
                                                 List<Long> categories,
                                                 LocalDateTime rangeStart,
                                                 LocalDateTime rangeEnd,
                                                 PageRequest pageRequest) {
        log.debug("Fetching admin filtered events with: users={}, state={}, categories={}, rangeStart={}, " +
                        "rangeEnd={}, pageRequest={}",
                users, state, categories, rangeStart, rangeEnd, pageRequest);

        Specification<Event> spec = Specification.where(EventSpecifications.withUsers(users))
                .and(EventSpecifications.withCategories(categories))
                .and(EventSpecifications.withState(state))
                .and(EventSpecifications.withDateRange(rangeStart, rangeEnd))
                .and(EventSpecifications.withOnlyAvailable(true));

        var events = eventRepository.findAll(spec, pageRequest).getContent();
        log.debug("Fetched {} events from repository", events.size());
        return events;
    }

    private List<EventFullDto> mapEventsToFullDtos(List<Event> events) {
        log.debug("Mapping events to EventFullDto. Event count: {}", events.size());
        List<EventFullDto> eventFullDtos = eventMapper.listEventToListEventFullDto(events);
        log.debug("Events successfully mapped to EventFullDto");
        return eventFullDtos;
    }

    @Transactional
    @Override
    public EventFullDto updateEventAdmin(UpdateEventAdminRequest updateEvent, long eventId) {
        log.info("Admin requested to update event with id={} using data: {}", eventId, updateEvent);
        Event event = fetchEventById(eventId);
        log.debug("Starting to update fields for event id={} by admin", eventId);
        updateAdminEventFields(updateEvent, event);
        EventFullDto updatedEventDto = eventMapper.eventToEventFullDto(event);
        log.info("Event with id={} successfully updated by admin: {}", eventId, updatedEventDto);
        return updatedEventDto;
    }

    private void updateAdminEventFields(UpdateEventAdminRequest updateEvent, Event event) {
        updateField(updateEvent.getAnnotation(), event::setAnnotation, "annotation", event);
        updateField(updateEvent.getDescription(), event::setDescription, "description", event);
        updateField(updateEvent.getTitle(), event::setTitle, "title", event);
        if (updateEvent.getCategory() != null) {
            log.debug("Updating category for event id={} with new category id={}",
                    event.getId(), updateEvent.getCategory());
            Category category = categoryRepository.findById(updateEvent.getCategory())
                    .orElseThrow(() -> new NotFoundException(
                            "Category with id=" + updateEvent.getCategory() + " was not found"
                    ));
            event.setCategory(category);
        }
        if (updateEvent.getEventDate() != null) {
            log.debug("Updating event date for event id={} with new date={}",
                    event.getId(), updateEvent.getEventDate());
            if (updateEvent.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
                log.error("Invalid event date for event id={}: {}", event.getId(), updateEvent.getEventDate());
                throw new DataTimeException("The date and time for which the event is scheduled cannot be earlier " +
                        "than two hours from the current moment");
            }
            event.setEventDate(updateEvent.getEventDate());
        }
        if (updateEvent.getLocation() != null) {
            log.debug("Updating location for event id={} with new location={}",
                    event.getId(), updateEvent.getLocation());
            event.setLocation(locationMapper.locationDtoToLocation(updateEvent.getLocation()));
        }
        updateField(updateEvent.getPaid(), event::setPaid, "paid status", event);
        updateField(updateEvent.getParticipantLimit(),
                event::setParticipantLimit,
                "participant limit", event
        );
        updateField(updateEvent.getRequestModeration(),
                event::setRequestModeration,
                "request moderation", event
        );
        if (updateEvent.getStateAction() != null) {
            log.debug("Updating state action for event id={} with action={}",
                    event.getId(), updateEvent.getStateAction());
            setStateByAdmin(event, updateEvent.getStateAction());
        }
    }

    private <T> void updateField(T fieldValue, Consumer<T> setter, String fieldName, Event event) {
        if (fieldValue != null) {
            log.debug("Updating {} for event id={} with new value: {}", fieldName, event.getId(), fieldValue);
            setter.accept(fieldValue);
        }
    }

    @Override
    public boolean findExistEventByEventIdAndInitiatorId(Long eventId, Long initiatorId) {
        return eventRepository.existsByIdAndInitiatorId(eventId, initiatorId);
    }

    @Override
    public EventFullDto findEventById(Long eventId) {
        log.info("Fetching event with id={}", eventId);
        Event event = fetchEventById(eventId);
        log.debug("Mapping event with id={} to EventFullDto", event.getId());
        EventFullDto eventFullDto = eventMapper.eventToEventFullDto(event);
        eventFullDto.setInitiator(new UserShortDto(event.getInitiatorId(), null));
        log.info("Successfully fetched event: {}", eventFullDto);
        return eventFullDto;
    }

    @Override
    public boolean findExistEventByEventId(Long eventId) {
        return eventRepository.existsById(eventId);
    }

    private void setStateByAdmin(Event event, StateActionAdmin stateActionAdmin) {
        log.debug("Setting state by admin for event id={} with action={}", event.getId(), stateActionAdmin);
        if (stateActionAdmin.equals(StateActionAdmin.PUBLISH_EVENT)) {
            handlePublishEvent(event);
        } else if (stateActionAdmin.equals(StateActionAdmin.REJECT_EVENT)) {
            handleRejectEvent(event);
        } else {
            log.warn("Unknown state action attempted for event id={}: {}", event.getId(), stateActionAdmin);
            throw new IllegalArgumentException("Unknown state action: " + stateActionAdmin);
        }
    }

    private void handlePublishEvent(Event event) {
        if (event.getEventDate().isBefore(LocalDateTime.now().plusHours(1))) {
            log.error("Unable to publish event id={} due to date restriction", event.getId());
            throw new DataTimeException("The start date of the event to be modified must be no earlier " +
                    "than one hour from the date of publication.");
        }
        if (!event.getState().equals(State.PENDING)) {
            log.error("Event id={} cannot be published as it is not in PENDING state", event.getId());
            throw new RestrictionsViolationException("An event can be published only if it is in the waiting state " +
                    "for publication.");
        }
        event.setState(State.PUBLISHED);
        event.setPublishedOn(LocalDateTime.now());
        log.info("Event id={} successfully published", event.getId());
    }

    private void handleRejectEvent(Event event) {
        if (event.getState().equals(State.PUBLISHED)) {
            log.error("Event id={} cannot be rejected as it is already PUBLISHED", event.getId());
            throw new RestrictionsViolationException("An event can be rejected only if it has not been " +
                    "published yet.");
        }
        event.setState(State.CANCELED);
        log.info("Event id={} successfully rejected", event.getId());
    }

    private PageRequest getCustomPage(int from, int size, EventPublicSort sort) {
        if (sort == null) {
            log.debug("No sort provided, returning default PageRequest");
            return PageRequest.of(from, size);
        }
        return switch (sort) {
            case EVENT_DATE -> createSortedPageRequest(from, size, "eventDate", Sort.Direction.ASC);
            case VIEWS -> createSortedPageRequest(from, size, "views", Sort.Direction.ASC);
            default -> {
                log.warn("Unknown sort type provided: {}", sort);
                throw new IllegalArgumentException("Unknown sort type: " + sort);
            }
        };
    }

    private PageRequest createSortedPageRequest(int from, int size, String sortField, Sort.Direction direction) {
        log.debug("Creating PageRequest: from={}, size={}, sortField={}, direction={}",
                from, size, sortField, direction);
        return PageRequest.of(from, size, Sort.by(direction, sortField));
    }

    private List<StatResponseDto> getViewStats(List<Event> events, String requestURL) {
        if (CollectionUtils.isEmpty(events)) {
            log.warn("No events provided to fetch view statistics");
            return Collections.emptyList();
        }
        List<String> url = events.stream()
                .map(event -> requestURL + "/" + event.getId())
                .toList();
        log.debug("Fetching stats for event URLs: {}", url);
        return Optional.ofNullable(
                statClient.getStats(DEFAULT_SEARCH_START_DATE,
                        LocalDateTime.now(),
                        url.toArray(String[]::new),
                        true
                )
        ).orElse(Collections.emptyList());
    }

    private void setViews(List<Event> events, String requestURL) {
        if (CollectionUtils.isEmpty(events)) {
            log.warn("No events to set views");
            return;
        }
        Map<String, Long> mapUriAndHits = getViewStats(events, requestURL).stream()
                .collect(Collectors.toMap(StatResponseDto::getUri, StatResponseDto::getHits));
        for (Event event : events) {
            long views = mapUriAndHits.getOrDefault(requestURL + "/" + event.getId(), 0L);
            event.setViews(views);
            log.debug("Set {} views for event id={}", views, event.getId());
        }
    }

    private EventRequestStatusUpdateResultDto createEventRequestStatusUpdateResult(
            List<ParticipationRequestDto> requests) {
        if (CollectionUtils.isEmpty(requests)) {
            log.warn("No participation requests provided for result creation");
            return new EventRequestStatusUpdateResultDto(Collections.emptyList(), Collections.emptyList());
        }
        log.debug("Received participation requests for result creation: {}", requests);
        log.debug("Creating update result for {} requests", requests.size());
        Map<Status, List<ParticipationRequestDto>> groupedRequests = requests.stream()
                .collect(Collectors.groupingBy(ParticipationRequestDto::status));
        List<ParticipationRequestDto> confirmedRequests = groupedRequests
                .getOrDefault(Status.CONFIRMED, Collections.emptyList());
        List<ParticipationRequestDto> rejectedRequests = groupedRequests
                .getOrDefault(Status.REJECTED, Collections.emptyList());
        log.info("Event request update result created: {} confirmed, {} rejected",
                confirmedRequests.size(), rejectedRequests.size());
        return new EventRequestStatusUpdateResultDto(confirmedRequests, rejectedRequests);
    }

    private <T> T fetchEntityById(Supplier<Optional<T>> entityFetcher, String errorMessage) {
        return entityFetcher.get().orElseThrow(() -> {
            log.error(errorMessage);
            return new NotFoundException(errorMessage);
        });
    }

    private PageRequest buildPageRequest(int from, int size, EventPublicSort sort, Sort.Direction defaultDirection) {
        log.debug("Building PageRequest with parameters: from={}, size={}, sort={}, defaultDirection={}",
                from, size, sort, defaultDirection);

        Sort.Direction sortDirection = defaultDirection;
        String sortField = "";

        if (sort != null) {
            switch (sort) {
                case EVENT_DATE:
                    sortField = "eventDate";
                    sortDirection = Sort.Direction.ASC;
                    log.debug("Sort type EVENT_DATE selected. sortField={}, sortDirection={}",
                            sortField, sortDirection);
                    break;
                case VIEWS:
                    sortField = "views";
                    sortDirection = Sort.Direction.ASC;
                    log.debug("Sort type VIEWS selected. sortField={}, sortDirection={}", sortField, sortDirection);
                    break;
                default:
                    log.error("Unknown sort type: {}", sort);
                    throw new IllegalArgumentException("Unknown sort type: " + sort);
            }
        }

        PageRequest pageRequest = PageRequest.of(from / size, size, Sort.by(sortDirection, sortField));
        log.debug("Created PageRequest: {}", pageRequest);

        return pageRequest;
    }
}