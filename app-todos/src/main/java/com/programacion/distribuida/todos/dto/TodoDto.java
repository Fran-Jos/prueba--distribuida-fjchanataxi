package com.programacion.distribuida.todos.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TodoDto {
    private Integer id;
    private String title;
    private Boolean completed;
    private String userName;
}
