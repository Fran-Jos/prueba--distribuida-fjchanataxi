package com.programacion.distribuida.todos.rest;

import com.programacion.distribuida.todos.clients.UserRestClient;
import com.programacion.distribuida.todos.db.Todo;
import com.programacion.distribuida.todos.dtos.TodoDto;
import com.programacion.distribuida.todos.repo.TodoRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.modelmapper.ModelMapper;

import java.util.List;

@Path("/todos")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@ApplicationScoped
@Transactional
public class TodoRest {

    @Inject
    TodoRepository repository;

    @Inject
    ModelMapper mapper;

    @Inject
    @RestClient
    UserRestClient userClient;

    @GET
    public List<TodoDto> listAll() {
        return repository.streamAll()
                .map(todo -> {
                    TodoDto dto = mapper.map(todo, TodoDto.class);
                    dto.setUser(userClient.findById(todo.getUserId()));
                    return dto;
                })
                .toList();
    }

    @GET
    @Path("/{id}")
    public Response getById(@PathParam("id") Integer id) {
        return repository.findByIdOptional(id)
                .map(todo -> {
                    TodoDto dto = mapper.map(todo, TodoDto.class);
                    dto.setUser(userClient.findById(todo.getUserId()));
                    return Response.ok(dto).build();
                })
                .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }

    @POST
    public void create(Todo todo) {
        repository.persist(todo);
    }

    @PUT
    @Path("/{id}")
    public void update(@PathParam("id") Integer id, Todo todo) {
        repository.findByIdOptional(id).ifPresent(existing -> {
            existing.setTitle(todo.getTitle());
            existing.setCompleted(todo.getCompleted());
            existing.setUserId(todo.getUserId());
            existing.setVersion(existing.getVersion() + 1);
        });
    }
}
