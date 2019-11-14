package lab5;

import akka.actor.ActorSystem;

public class Launcher {

    private static final String ACTOR_SYSTEM_NAME = "ping"

    public static void main(String[] args) {
        ActorSystem system = ActorSystem.create();
    }
}
