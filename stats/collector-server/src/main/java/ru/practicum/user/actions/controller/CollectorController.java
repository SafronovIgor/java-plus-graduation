package ru.practicum.user.actions.controller;

import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.practicum.ewm.stats.proto.UserActionControllerGrpc;
import ru.practicum.ewm.stats.proto.UserActionProto;
import ru.practicum.user.actions.producer.UserActionProducer;

/**
 * Класс gRPC-сервиса для обработки действий пользователей.
 * Реализует серверную часть gRPC (определённую в protobuf-файле).
 */

@GrpcService
@RequiredArgsConstructor
public class CollectorController extends UserActionControllerGrpc.UserActionControllerImplBase {
    private final UserActionProducer userActionProducer;

    @Override
    public void collectUserAction(UserActionProto userActionProto, StreamObserver<Empty> responseObserver) {
        //Отправка в Kafka
        userActionProducer.collectUserAction(userActionProto);

        //Ответ клиенту.
        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }
}