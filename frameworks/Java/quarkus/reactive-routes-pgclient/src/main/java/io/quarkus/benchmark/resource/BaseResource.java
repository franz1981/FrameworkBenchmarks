package io.quarkus.benchmark.resource;

import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.Json;
import io.vertx.ext.web.RoutingContext;

public abstract class BaseResource {
    private static final CharSequence CONTENT_TYPE_HEADER_VALUE = HttpHeaders.createOptimized("application/json");

    void sendJson(RoutingContext rc, Object value) {
        rc.response()
                .putHeader(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_HEADER_VALUE)
                .end(Json.encodeToBuffer(value));
    }

    Void handleFail(RoutingContext rc, Throwable t) {
        rc.response().setStatusCode(500).end(t.toString());
        return null;
    }

}
