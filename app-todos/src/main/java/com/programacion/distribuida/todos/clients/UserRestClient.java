package com.programacion.distribuida.todos.clients;

import com.programacion.distribuida.todos.dto.UserDto;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RegisterRestClient(configKey = "users.api")
public interface UserRestClient {
    @GET
    @Path("/{id}")
    UserDto findById(@PathParam("id") Integer id);
}
