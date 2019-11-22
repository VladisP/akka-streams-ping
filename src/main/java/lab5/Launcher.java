package lab5;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.http.javadsl.ConnectHttp;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Flow;

import java.io.IOException;
import java.util.concurrent.CompletionStage;

public class Launcher {

    private static final String ACTOR_SYSTEM_NAME = "ping";
    private static final String HOST_NAME = "localhost";
    private static final int PORT = 8080;

    private static final String URL_PARAM_NAME = "testUrl";
    private static final String COUNT_PARAM_NAME = "count";

    public static void main(String[] args) throws IOException {
        ActorSystem system = ActorSystem.create(ACTOR_SYSTEM_NAME);
        final Http http = Http.get(system);
        final ActorMaterializer materializer = ActorMaterializer.create(system);
        final Flow<HttpRequest, HttpResponse, NotUsed> httpFlow = Flow.of(HttpRequest.class).map((request) -> {
            //распарсить
            String testUrl = request.getUri().query().getOrElse()
        })
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
