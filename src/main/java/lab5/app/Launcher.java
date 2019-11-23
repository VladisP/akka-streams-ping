package lab5.app;

import akka.NotUsed;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.http.javadsl.ConnectHttp;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
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

import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class Launcher {

    private static final String ACTOR_SYSTEM_NAME = "ping";
    private static final String HOST_NAME = "localhost";
    private static final int PORT = 8080;

    public static void main(String[] args) throws IOException {
        ActorSystem system = ActorSystem.create(ACTOR_SYSTEM_NAME);
        final Http http = Http.get(system);
        final ActorMaterializer materializer = ActorMaterializer.create(system);

        final Flow<HttpRequest, HttpResponse, NotUsed> httpFlow = Flow
                .of(HttpRequest.class).map((request) -> {
                    //распарсить
                    Query requestQuery = request.getUri().query();
                    String testUrl = requestQuery.getOrElse(URL_PARAM_NAME, "");
                    int count = Integer.parseInt(requestQuery.getOrElse(COUNT_PARAM_NAME, "-1"));

                    if (testUrl.equals("") || count == -1) {
                        //TODO: error msg
                    }

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
        final CompletionStage<ServerBinding> binding = http.bindAndHandle(
                httpFlow,
                ConnectHttp.toHost(HOST_NAME, PORT),
                materializer
        );
        System.out.println("Server online at " + HOST_NAME + ":" + PORT);
        System.out.println("Press RETURN to stop...");
        System.in.read();
        binding
                .thenCompose(ServerBinding::unbind)
                .thenAccept(unbound -> system.terminate());
    }
}
