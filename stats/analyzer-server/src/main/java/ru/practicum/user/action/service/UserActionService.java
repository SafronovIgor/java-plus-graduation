package ru.practicum.user.action.service;

import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.util.List;

public interface UserActionService {
    void saveUserActions(List<UserActionAvro> userActionsAvro);
}