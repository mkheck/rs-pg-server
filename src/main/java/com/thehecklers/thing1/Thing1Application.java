package com.thehecklers.thing1;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.annotation.ConnectMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
public class Thing1Application {

    public static void main(String[] args) {
        SpringApplication.run(Thing1Application.class, args);
    }

}

@Controller
@RequiredArgsConstructor
class RSocketController {
    @NonNull
    private final PongFactory pongFactory;

/*
    private final List<RSocketRequester> rsClients = new ArrayList<>();
    private RSocketRequester requester;

    @ConnectMapping("thing2")
    void connectAndRequestStream
            (RSocketRequester requester, @Payload String clientId) {
        this.requester = requester;

        requester.rsocket()
                .onClose()
                .doFirst(() -> {
                    System.out.println("Client connected: " + clientId);
                    rsClients.add(requester);
                })
                .doOnError(error -> {
                    System.out.println("Channel closed to client: " + clientId);
                })
                .doFinally(consumer -> {
                    rsClients.remove(requester);
                    System.out.println("Client disconnected: " + clientId);
                })
                .subscribe();

        requester.route("client")
                .data(Mono.just(new Pong(" <> Requesting data <>")))
                .retrieveFlux(Ping.class)
                .log()
                .subscribe();
    }
*/

    @MessageMapping("reqresp")
    Pong reqResp(Ping ping) {
        // "Imperative" version
        return new Pong("Message received from ping is " + ping.getText());
    }

    @MessageMapping("reqstream")
    Flux<Pong> reqStream(Ping ping) {
        return Flux.interval(Duration.ofMillis(100))
                .map(l -> new Pong(ping.getText() + " " + l.toString()))
                .onBackpressureDrop()
                .log();
    }

    @MessageMapping("reactivereqstream")
    Flux<Pong> reqStream(Mono<Ping> pingMono) {
        return pingMono.flatMapMany(ping ->
                Flux.interval(Duration.ofSeconds(1))
                        .map(l -> new Pong(" >>> Pong from Thing 1: " + l.toString())))
                .onBackpressureDrop();
    }

    @MessageMapping("fireforget")
    Mono<Void> fireAndForget(Mono<Ping> pingMono) {
        pingMono.log().subscribe();
        return Mono.empty();
    }

    @MessageMapping("bidirectional")
    Flux<Pong> bidirectional(Flux<Ping> pingFlux) {
        return pingFlux.doOnSubscribe(ping -> System.out.println("Subscribed"))
                .doOnNext(ping -> System.out.println("Inbound Ping: " + ping.toString()))
                .switchMap(ping -> Flux.interval(Duration.ofMillis(250))
                        .map(l -> pongFactory.createPong("Sending Pong")));
    }

    @MessageMapping("datafromclient")
    Flux<?> bidirectionalButNotReally(Flux<Ping> pingFlux) {
        return pingFlux.doOnSubscribe(ping -> System.out.println("Subscribed"))
                .doOnNext(ping -> System.out.println(ping.toString()))
                .switchMap(ping -> Flux.interval(Duration.ofMillis(250))
                        .map(l -> Flux.empty()));
    }
}

