package lab5.actors;

import akka.actor.AbstractActor;
import akka.japi.pf.ReceiveBuilder;
import lab5.messages.PingRequest;

import java.util.HashMap;
import java.util.Map;

public class CacheActor extends AbstractActor {

    private Map<String, Long> cache = new HashMap<>();

    @Override
    public Receive createReceive() {
        return ReceiveBuilder.create()
                .match(PingRequest.class, (pingRequest) -> {
                    
                })
                .match()
                .build();
    }
}
