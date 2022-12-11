package io.mohashi.resource;

import java.util.Optional;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.jboss.resteasy.reactive.RestPath;
import org.jboss.resteasy.reactive.RestQuery;

import io.mohashi.model.User;
import io.mohashi.service.ImmutableUserSearch;
import io.mohashi.service.UserSearch;
import io.mohashi.service.UserService;
import io.smallrye.mutiny.Uni;

@Path("/api/v1/")
public class UserResource {

    @Inject
    UserService userService;

    @GET
    @Path("user")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Response> list(@RestQuery("name") String name, @RestQuery("email") String email) {
        UserSearch userSearch = ImmutableUserSearch.builder()
            .name(Optional.ofNullable(name))
            .email(Optional.ofNullable(email)).build();
        
        return userService.list(userSearch)
            .flatMap(res -> 
                Uni.createFrom().item(Response.ok(res).build()));
    }

    @GET
    @Path("user/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Response> get(@RestPath("id") Long id) {
        return userService.get(id)
            .flatMap(res -> 
                Uni.createFrom().item(Response.ok(res).build()));
    }

    @POST
    @Path("user")
    @Consumes(MediaType.APPLICATION_JSON)
    public Uni<Response> post(@RequestBody User user) {
        return userService.persist(user)
            .flatMap(res -> 
                Uni.createFrom().item(Response.ok(res).build()));
    }

    @PUT
    @Path("user")
    @Consumes(MediaType.APPLICATION_JSON)
    public Uni<Response> put(@RequestBody User user) {
        return userService.persist(user)
            .flatMap(res -> 
                Uni.createFrom().item(Response.ok(res).build()));
    }

    @DELETE
    @Path("user/{id}")
    public Uni<Response> delete(Long id) {
        return userService.delete(id)
            .flatMap(res -> Uni.createFrom().item(Response.ok().build()));
    }


}