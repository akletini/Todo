package com.todo.Todo.controller;

import com.todo.Todo.entity.TodoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TodoController {

    @Autowired
    private TodoRepository repo;

    @GetMapping("/")
    public String index() {
        return "index";
    }
}
