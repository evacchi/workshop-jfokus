///usr/bin/env jbang "$0" "$@" ; exit $?
//JAVA 19
//COMPILE_OPTIONS --enable-preview --release 19
//RUNTIME_OPTIONS --enable-preview 
//SOURCES Actor.java
import static java.lang.System.out;

import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

interface Playground {

    record Pong(Actor.Address sender){}
    record Ping(Actor.Address sender){}
    record DeadlyPing(Actor.Address sender){}


    static void main(String... args) {
        var sys = new Actor.System(Executors.newFixedThreadPool(4));

        var ponger = sys.actorOf(self -> msg -> {
            switch (msg) {
                case Ping(var sender) -> {
                    sender.tell(new Pong(self));
                    out.println("PING!");
                }
                case DeadlyPing(var sender) -> {
                    out.println("OH NOES!");
                    sender.tell(new Pong(self));
                    return Actor.Die;
                }
                default -> out.println("Unknown msg: "+msg);
            }
            return Actor.Stay;
        });

        var pinger = sys.actorOf(self -> msg -> pingerBehavior(self, msg, 0));

        ponger.tell(new Ping(pinger));
    }

    static Actor.Effect pingerBehavior(Actor.Address self, Object msg, int counter) {
        switch (msg) {
            case Pong(var sender) when counter<=10 -> {
                sender.tell(new Ping(self));
                out.println("PONG!");
                return Actor.Become(m -> pingerBehavior(self, m, counter+1));
            }
            case Pong(var sender) -> {
                sender.tell(new DeadlyPing(self));
            }
            default -> out.println("Unknown msg: "+msg);
        }
        return Actor.Stay;
    }
}
