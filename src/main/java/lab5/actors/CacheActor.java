package lab5.actors;

import akka.actor.AbstractActor;

import java.util.HashMap;
import java.util.Map;

public class CacheActor extends AbstractActor {

    private Map<String, Long> cache = new HashMap<>();

    @Override
    public Receive createReceive() {
        return null;
    }
}
