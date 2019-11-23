package lab5.actors;

import akka.actor.AbstractActor;
import akka.japi.pf.ReceiveBuilder;
import lab5.messages.PingRequest;
import lab5.messages.PingResult;

import java.util.HashMap;
import java.util.Map;

public class CacheActor extends AbstractActor {

    private Map<String, Long> cache = new HashMap<>();

    @Override
    public Receive createReceive() {
        return ReceiveBuilder.create()
                .match(PingRequest.class, (pingRequest) -> {
                    Long result = cache.getOrDefault(pingRequest.getTestUrl(), -1L);
                    sender().tell(new PingResult(pingRequest.getTestUrl(), result), self());
                })
                .match(PingResult.class, (pingResult) ->
                        cache.put(pingResult.getTestUrl(), pingResult.getAverageResponseTime())
                )
                .build();
    }
}
