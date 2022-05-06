package com.todo.Todo.controller;

import com.todo.Todo.entity.Tag;
import com.todo.Todo.entity.TagRepository;
import com.todo.Todo.entity.Todo;
import com.todo.Todo.entity.TodoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/todo/api/v1")
public class ApiController {

    @Autowired
    private TodoRepository todoRepository;

    @Autowired
    private TagRepository tagRepository;

    @GetMapping(value = "/todos", produces = MediaType.APPLICATION_JSON_VALUE)
    public Iterable<Todo> getAllTodos(){
        return todoRepository.findAll();
    }

    @GetMapping(value = "/done", produces = MediaType.APPLICATION_JSON_VALUE)
    public Iterable<Todo> getAllDoneTodos(){
        List<Todo> todos = (List<Todo>) todoRepository.findAll();
        List<Todo> filteredTodos = new ArrayList<>();
        for(Todo todo : todos){
            if(todo.getCurrentState().equals("DONE")){
                filteredTodos.add(todo);
            }
        }
        return filteredTodos;
    }

    @GetMapping(value = "/open", produces = MediaType.APPLICATION_JSON_VALUE)
    public Iterable<Todo> getAllOpenTodos(){
        List<Todo> todos = (List<Todo>) todoRepository.findAll();
        List<Todo> filteredTodos = new ArrayList<>();
        for(Todo todo : todos){
            if(todo.getCurrentState().equals("OPEN")){
                filteredTodos.add(todo);
            }
        }
        return filteredTodos;
    }

    @PostMapping(value = "/todos", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void addTodos(@RequestBody List<Todo> todos) {
        todoRepository.saveAll(todos);
    }

    @GetMapping(value = "/tags", produces = MediaType.APPLICATION_JSON_VALUE)
    public Iterable<Tag> getAllTags(){
        return tagRepository.findAll();
    }

    @PostMapping(value = "/tags", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void addTags(@RequestBody List<Tag> tags) {
        tagRepository.saveAll(tags);
    }

}
