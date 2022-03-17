package com.agapic.vertx_reproducer;

import io.vertx.core.Vertx;

public class Main {

    public static void main(String[] args) {
        Vertx.vertx().deployVerticle(new Client());
        Vertx.vertx().deployVerticle(new Server());
    }
}
