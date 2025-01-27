package ru.practicum.user.action.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.user.action.mapper.UserActionMapper;
import ru.practicum.user.action.model.UserAction;
import ru.practicum.user.action.repository.UserActionRepository;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserActionServiceImpl implements UserActionService {
    private final UserActionMapper userActionMapper;
    private final UserActionRepository userActionRepository;

    @Override
    @Transactional
    public void saveUserActions(List<UserActionAvro> userActionsAvro) {
        List<UserAction> userActions = userActionMapper.listUserActionAvroToListUserAction(userActionsAvro);
        log.info("Successfully mapped {} UserActionAvro objects to UserAction entities.", userActionsAvro.size());

        userActionRepository.saveAll(userActions);
        log.info("Successfully saved {} user actions to the database.", userActions.size());
    }
}