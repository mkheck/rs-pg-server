package com.thehecklers.thing1;

import org.springframework.stereotype.Component;

@Component
public class PongFactory {
    static int pongCounter = 0;

    public Pong createPong(String text) {
        return new Pong(text + " " + ++pongCounter);
    }
}
