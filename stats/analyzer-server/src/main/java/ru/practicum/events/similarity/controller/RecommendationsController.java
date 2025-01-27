package ru.practicum.events.similarity.controller;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.practicum.events.similarity.service.EventSimilarityService;
import ru.practicum.ewm.stats.proto.*;

import java.util.List;
import java.util.function.Supplier;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class RecommendationsController extends RecommendationsControllerGrpc.RecommendationsControllerImplBase {

    private final EventSimilarityService eventSimilarityService;

    @Override
    public void getRecommendationsForUser(UserPredictionsRequestProto request,
                                          StreamObserver<RecommendedEventProto> responseObserver) {
        handleStreamResponse(() -> eventSimilarityService.getRecommendationsForUser(request), responseObserver);
    }

    @Override
    public void getSimilarEvents(SimilarEventsRequestProto request,
                                 StreamObserver<RecommendedEventProto> responseObserver) {
        handleStreamResponse(() -> eventSimilarityService.getSimilarEvents(request), responseObserver);
    }

    @Override
    public void getInteractionsCount(InteractionsCountRequestProto request,
                                     StreamObserver<RecommendedEventProto> responseObserver) {
        handleStreamResponse(() -> eventSimilarityService.getInteractionsCount(request), responseObserver);
    }

    private void handleStreamResponse(Supplier<List<RecommendedEventProto>> responseSupplier,
                                      StreamObserver<RecommendedEventProto> responseObserver) {
        try {
            List<RecommendedEventProto> recommendations = responseSupplier.get();
            recommendations.forEach(responseObserver::onNext);
        } catch (Exception e) {
            log.error("Error while processing request: {}", e.getMessage(), e);
            responseObserver.onError(e);
        } finally {
            responseObserver.onCompleted();
        }
    }
}