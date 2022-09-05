package io.quarkus.benchmark.resource;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import io.smallrye.common.annotation.NonBlocking;
import io.smallrye.context.api.CurrentThreadContext;
import org.eclipse.microprofile.context.ThreadContext;

@Path("/json")
public class JsonResource  {

    private static final String HELLO = "Hello, World!";

    @Produces(MediaType.APPLICATION_JSON)
    @GET
    @CurrentThreadContext(propagated = {}, cleared = {}, unchanged = ThreadContext.ALL_REMAINING)
    @NonBlocking
    public Message json() {
        // https://github.com/TechEmpower/FrameworkBenchmarks/wiki/Project-Information-Framework-Tests-Overview#json-serialization
        return new Message(HELLO);
    }
}

