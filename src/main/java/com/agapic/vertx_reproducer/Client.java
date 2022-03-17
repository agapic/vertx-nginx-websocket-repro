package com.agapic.vertx_reproducer;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.MultiMap;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.WebsocketVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Client extends AbstractVerticle {
    private final Logger logger = LoggerFactory.getLogger(Client.class);

    public void start() {
        HttpClient client = vertx.createHttpClient();

        MultiMap badHeaders = MultiMap.caseInsensitiveMultiMap();
        badHeaders.add("x-rto-test-response", "403");

        MultiMap goodHeaders = MultiMap.caseInsensitiveMultiMap();
        goodHeaders.add("x-rto-test-response", "200");

        vertx.setPeriodic(1000, handler -> {
            logger.info("Attempting to connect to websocket and expecting a 403 response");
            client.webSocketAbs("ws://localhost:9090/ws",
                    badHeaders, WebsocketVersion.V13,
                    null,
                    res -> {
                        assert (res.failed());
                        logger.info("Received a 403 successfully");
                    });
        });

        vertx.setPeriodic(1000, handler -> {
            logger.info("Attempting to connect to websocket and expecting a 200 response");
            client.webSocketAbs("ws://localhost:9090/ws",
                    goodHeaders, WebsocketVersion.V13,
                    null,
                    res -> {
                        assert (res.succeeded());
                        logger.info("Received a 200 response");
                    });
        });
    }
}
