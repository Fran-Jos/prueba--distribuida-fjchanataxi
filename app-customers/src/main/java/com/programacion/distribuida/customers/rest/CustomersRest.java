package com.programacion.distribuida.customers.rest;

import com.programacion.distribuida.customers.db.Customer;
import com.programacion.distribuida.customers.repo.CustomerRepository;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;

import java.net.URI;
import java.util.List;
import java.util.Optional;

@Path("/customers")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CustomersRest {

    @Inject
    CustomerRepository repository;

    @GET
    public List<Customer> listAll() {
        return repository.listAll();
    }

    @GET
    @Path("/{id}")
    public Response getById(@PathParam("id") Integer id) {
        Optional<Customer> found = repository.findByIdOptional(id);
        return found
                .map(Response::ok)
                .orElse(Response.status(Response.Status.NOT_FOUND))
                .build();
    }

    @POST
    @Transactional
    public Response create(Customer customer) {
        repository.persist(customer);
        if (repository.isPersistent(customer)) {
            URI uri = UriBuilder.fromResource(CustomersRest.class)
                               .path(String.valueOf(customer.getId()))
                               .build();
            return Response.created(uri)
                           .entity(customer)
                           .build();
        }
        return Response.status(Response.Status.BAD_REQUEST).build();
    }

    @PUT
    @Path("/{id}")
    @Transactional
    public Response update(@PathParam("id") Integer id, Customer customer) {
        Optional<Customer> existing = repository.findByIdOptional(id);
        if (existing.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        Customer toUpdate = existing.get();
        toUpdate.setName(customer.getName());
        toUpdate.setEmail(customer.getEmail());
        return Response.ok(toUpdate).build();
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    public Response delete(@PathParam("id") Integer id) {
        boolean deleted = repository.deleteById(id);
        if (deleted) {
            return Response.noContent().build();
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }
}
