package ru.practicum.client;

import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.proto.*;

import java.util.Iterator;
import java.util.List;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
public class RecommendationsClient {

    @GrpcClient("analyzer")
    private RecommendationsControllerGrpc.RecommendationsControllerBlockingStub recommendationsClient;

    public List<RecommendedEventProto> getSimilarEvents(long eventId, long userId, int maxResults) {
        SimilarEventsRequestProto request = buildSimilarEventsRequest(eventId, userId, maxResults);
        return executeGrpcCall(request, recommendationsClient::getSimilarEvents);
    }

    public List<RecommendedEventProto> getRecommendationsForUser(long userId, int maxResults) {
        UserPredictionsRequestProto request = buildUserPredictionsRequest(userId, maxResults);
        return executeGrpcCall(request, recommendationsClient::getRecommendationsForUser);
    }

    public List<RecommendedEventProto> getInteractionsCount(List<Long> eventIds) {
        InteractionsCountRequestProto request = buildInteractionsCountRequest(eventIds);
        return executeGrpcCall(request, recommendationsClient::getInteractionsCount);
    }

    private <T> List<RecommendedEventProto> executeGrpcCall(T request,
                                                            java.util.function.Function<T, Iterator<RecommendedEventProto>> grpcCall) {
        return StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(grpcCall.apply(request), 0),
                false
        ).collect(Collectors.toList());
    }

    private SimilarEventsRequestProto buildSimilarEventsRequest(long eventId, long userId, int maxResults) {
        return SimilarEventsRequestProto.newBuilder()
                .setEventId(eventId)
                .setUserId(userId)
                .setMaxResults(maxResults)
                .build();
    }

    private UserPredictionsRequestProto buildUserPredictionsRequest(long userId, int maxResults) {
        return UserPredictionsRequestProto.newBuilder()
                .setUserId(userId)
                .setMaxResults(maxResults)
                .build();
    }

    private InteractionsCountRequestProto buildInteractionsCountRequest(List<Long> eventIds) {
        return InteractionsCountRequestProto.newBuilder()
                .addAllEventId(eventIds)
                .build();
    }
}