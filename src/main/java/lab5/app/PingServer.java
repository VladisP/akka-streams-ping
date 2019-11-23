package lab5.app;

import akka.NotUsed;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.http.javadsl.model.*;
import akka.pattern.Patterns;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.Keep;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import lab5.actors.CacheActor;
import lab5.messages.PingRequest;
import lab5.messages.PingResult;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.Dsl;

import java.time.Duration;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class PingServer {

    private static final String URL_PARAM_NAME = "testUrl";
    private static final String COUNT_PARAM_NAME = "count";
    private static final int PARALLELISM = 6;
    private static final Duration TIMEOUT_MILLIS = Duration.ofMillis(3000);
    private static final long NANO_TO_MS_FACTOR = 1_000_000L;

    private AsyncHttpClient httpClient = Dsl.asyncHttpClient();
    private ActorRef cacheActor;

    PingServer(ActorSystem system) {
        cacheActor = system.actorOf(Props.create(CacheActor.class));
    }

    Flow<HttpRequest, HttpResponse, NotUsed> getHttpFlow(ActorMaterializer materializer) {
        return Flow
                .of(HttpRequest.class).map((request) -> {
                    Query requestQuery = request.getUri().query();
                    String testUrl = requestQuery.getOrElse(URL_PARAM_NAME, "");
                    int count = Integer.parseInt(requestQuery.getOrElse(COUNT_PARAM_NAME, "-1"));

                    return new PingRequest(testUrl, count);
                })
                .mapAsync(PARALLELISM, (pingRequest) -> Patterns.ask(cacheActor, pingRequest, TIMEOUT_MILLIS)
                        .thenCompose((result) -> {
                            PingResult cachePingResult = (PingResult) result;
                            return cachePingResult.getAverageResponseTime() == -1
                                    ? pingExecute(pingRequest, materializer)
                                    : CompletableFuture.completedFuture(cachePingResult);
                        }))
                .map((result) -> {
                    cacheActor.tell(result, ActorRef.noSender());

                    return HttpResponse
                            .create()
                            .withStatus(StatusCodes.OK)
                            .withEntity(
                                    HttpEntities.create(
                                            result.getTestUrl() + " " + result.getAverageResponseTime()
                                    )
                            );
                });
    }

    private CompletionStage<PingResult> pingExecute(PingRequest request, ActorMaterializer materializer) {
        return Source
                .from(Collections.singletonList(request))
                .toMat(pingSink(), Keep.right())
                .run(materializer)
                .thenCompose((sumTime) -> CompletableFuture.completedFuture(
                        new PingResult(
                                request.getTestUrl(),
                                sumTime / request.getCount() / NANO_TO_MS_FACTOR
                        )
                ));
    }

    private Sink<PingRequest, CompletionStage<Long>> pingSink() {
        return Flow.<PingRequest>create()
                .mapConcat((pingRequest) -> Collections.nCopies(pingRequest.getCount(), pingRequest.getTestUrl()))
                .mapAsync(PARALLELISM, (url) -> {
                    long startTime = System.nanoTime();
                    return httpClient
                            .prepareGet(url)
                            .execute()
                            .toCompletableFuture()
                            .thenCompose((response) -> CompletableFuture.completedFuture(System.nanoTime() - startTime));
                })
                .toMat(Sink.fold(0L, Long::sum), Keep.right());
    }
}
