package com.programacion.distribuida.books.rest;

import com.programacion.distribuida.books.clients.AuthorRestClient;
import com.programacion.distribuida.books.db.Book;
import com.programacion.distribuida.books.dtos.AuthorDto;
import com.programacion.distribuida.books.dtos.BookDto;
import com.programacion.distribuida.books.repo.BooksRepository;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.stork.Stork;
import io.smallrye.stork.api.Service;
import io.smallrye.stork.api.ServiceInstance;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.modelmapper.ModelMapper;

import java.util.List;
import java.util.Map;

@Path("/books")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@ApplicationScoped
@Transactional
public class BookRest {

    @Inject
    BooksRepository booksRepository;

    @Inject
    ModelMapper mapper;

    @Inject
    @RestClient
    private AuthorRestClient authorRestClient;

    @GET
    @Path("/{isbn}")
    public Response findById(@PathParam("isbn") String isbn) {

        var stork = Stork.getInstance();

        // Listar servicios
        Map<String, Service> services = stork.getServices();

        services.entrySet()
                .stream()
                .forEach(it -> {
                    System.out.println(it.getKey());

                    Multi<ServiceInstance> instances = it.getValue()
                            .getInstances()
                            .onItem()
                            .transformToMulti(items -> Multi.createFrom().iterable(items));

                    instances.subscribe()
                            .with(item -> {
                                System.out.println("  " + item.getHost() + ":" + item.getPort());
                            });
                });

        Service service = stork.getService("authors-api");
        Uni<ServiceInstance> instance = service.selectInstance();
        instance
                .subscribe()
                .with(inst -> {
                    System.out.println("**Instancia seleccionada: " + inst.getHost() + ":" + inst.getPort());
                });

        BookDto bookDto = new BookDto();

        var obj = booksRepository.findByIdOptional(isbn);
        if (obj.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        mapper.map(obj.get(), bookDto);
        var authors = authorRestClient.findByBook(isbn)
                .stream()
                .map(AuthorDto::getName)
                .toList();

        bookDto.setAuthors(authors);
        return Response.ok(bookDto).build();
    }


    @GET
//    @Path("/findAll")
    public List<BookDto> findAll() {
        return booksRepository.streamAll()
                .map(book -> {
                    var dto = new BookDto();
                    mapper.map(book, dto);
                    return dto;
                })
                .map(book -> {
                    var authors = authorRestClient.findByBook(book.getIsbn())
                            .stream()
                            .map(AuthorDto::getName)
                            .toList();
                    book.setAuthors(authors);
                    return book;
                })
                .toList();
    }

    @POST
    public void insert(Book book) {
        booksRepository.persist(book);
    }

    @PUT
    @Path("/{isbn}")
    public void update(@PathParam("isbn") String isbn, Book book) {
        booksRepository.update(isbn, book);
    }

}
