/*
 * Copyright (c) 2018, 2021 Oracle and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.helidon.benchmark;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import com.jsoniter.output.JsonStream;
import com.jsoniter.output.JsonStreamPool;
import com.jsoniter.spi.JsonException;
import io.helidon.common.LogConfig;
import io.helidon.common.http.Http;
import io.helidon.nima.webserver.WebServer;
import io.helidon.nima.webserver.http.Handler;
import io.helidon.nima.webserver.http.HttpRules;
import io.helidon.nima.webserver.http.ServerRequest;
import io.helidon.nima.webserver.http.ServerResponse;

/**
 * Simple Hello World rest application.
 */
public final class Main {

    /**
     * Start the server.
     *
     * @param args ignored
     */
    public static void main(String[] args) {
        // logging and config
        LogConfig.configureRuntime();

        WebServer.builder()
                .routing(Main::routing)
                .start();
    }

    // exposed for tests
    static void routing(HttpRules rules) {
        rules.get("/plaintext", new PlaintextHandler())
                .get("/json", new JsonHandler())
                .get("/10k", new JsonKHandler(10));
    }

    private static byte[] serializeMsg(Message obj) {
        JsonStream stream = JsonStreamPool.borrowJsonStream();
        try {
            stream.reset(null);
            stream.writeVal(Message.class, obj);
            return Arrays.copyOfRange(stream.buffer().data(), 0, stream.buffer().tail());
        } catch (IOException e) {
            throw new JsonException(e);
        } finally {
            JsonStreamPool.returnJsonStream(stream);
        }
    }

    static class PlaintextHandler implements Handler {
        static final Http.HeaderValue CONTENT_TYPE = Http.HeaderValue.createCached(Http.Header.CONTENT_TYPE,
                "text/plain; charset=UTF-8");
        static final Http.HeaderValue CONTENT_LENGTH = Http.HeaderValue.createCached(Http.Header.CONTENT_LENGTH, "13");
        static final Http.HeaderValue SERVER = Http.HeaderValue.createCached(Http.Header.SERVER, "Nima");

        private static final byte[] RESPONSE_BYTES = "Hello, World!".getBytes(StandardCharsets.UTF_8);

        @Override
        public void handle(ServerRequest req, ServerResponse res) {
            res.header(CONTENT_LENGTH);
            res.header(CONTENT_TYPE);
            res.header(SERVER);
            res.send(RESPONSE_BYTES);
        }
    }

    static class JsonHandler implements Handler {
        static final Http.HeaderValue SERVER = Http.HeaderValue.createCached(Http.Header.SERVER, "Nima");
        private static final String MESSAGE = "Hello, World!";
        private static final int JSON_LENGTH = serializeMsg(new Message(MESSAGE)).length;
        static final Http.HeaderValue CONTENT_LENGTH = Http.HeaderValue.createCached(Http.Header.CONTENT_LENGTH,
                String.valueOf(JSON_LENGTH));

        @Override
        public void handle(ServerRequest req, ServerResponse res) {
            res.header(CONTENT_LENGTH);
            res.header(Http.HeaderValues.CONTENT_TYPE_JSON);
            res.header(SERVER);
            res.send(serializeMsg(newMsg()));
        }

        private static Message newMsg() {
            return new Message("Hello, World!");
        }
    }

    static class JsonKHandler implements Handler {
        static final Http.HeaderValue SERVER = Http.HeaderValue.createCached(Http.Header.SERVER, "Nima");
        private final Http.HeaderValue contentLength;
        private final String message;

        JsonKHandler(int kilobytes) {
            this.message = "a".repeat(1024 * kilobytes);
            int length = serializeMsg(new Message(message)).length;
            this.contentLength = Http.HeaderValue.createCached(Http.Header.CONTENT_LENGTH,
                    String.valueOf(length));
        }

        @Override
        public void handle(ServerRequest req, ServerResponse res) {
            res.header(contentLength);
            res.header(Http.HeaderValues.CONTENT_TYPE_JSON);
            res.header(SERVER);
            res.send(serializeMsg(newMsg()));
        }

        private Message newMsg() {
            return new Message(message);
        }
    }

    /**
     * Message to be serialized as JSON.
     */
    public static final class Message {

        private final String message;

        /**
         * Construct a new message.
         *
         * @param message message string
         */
        public Message(String message) {
            super();
            this.message = message;
        }

        /**
         * Get message string.
         *
         * @return message string
         */
        public String getMessage() {
            return message;
        }
    }
}
