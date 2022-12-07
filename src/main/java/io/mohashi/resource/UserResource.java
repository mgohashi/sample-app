package io.mohashi.resource;

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import io.mohashi.model.User;
import io.mohashi.service.UserService;
import io.smallrye.mutiny.Uni;

@Path("/api/v1/")
public class UserResource {

    @Inject
    UserService userService;

    @GET
    @Path("user")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<List<User>> get() {
        return userService.list();
    }
}