package com.programacion.distribuida.todos.dtos;

import lombok.Data;

@Data
public class TodoDto {
    private Integer id;
    private Integer userId;
    private String title;
    private Boolean completed;
    private UserDto user;
}
