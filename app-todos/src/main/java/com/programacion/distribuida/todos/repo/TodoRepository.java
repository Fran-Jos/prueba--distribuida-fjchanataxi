package com.programacion.distribuida.todos.repo;

import com.programacion.distribuida.todos.db.Todo;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

@ApplicationScoped
@Transactional
public class TodoRepository implements PanacheRepositoryBase<Todo, Integer> {
}
