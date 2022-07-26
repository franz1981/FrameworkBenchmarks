package io.quarkus.benchmark.resource;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import io.quarkus.benchmark.model.World;
import io.quarkus.benchmark.repository.WorldRepository;
import io.smallrye.context.api.CurrentThreadContext;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.context.ThreadContext;

@Produces(MediaType.APPLICATION_JSON)
@Path("/")
public class DbResource {

    @Inject
    WorldRepository worldRepository;

    @GET
    @Path("db")
    @CurrentThreadContext(propagated = {}, cleared = {}, unchanged = ThreadContext.ALL_REMAINING)
    public Uni<World> db() {
        return worldRepository.findStateless();
    }

    @GET
    @Path("createData")
    @CurrentThreadContext(propagated = {}, cleared = {}, unchanged = ThreadContext.ALL_REMAINING)
    public Uni<Void> createData() {
        return worldRepository.createData();
    }

}