package com.todo.Todo.controller;

import com.todo.Todo.entity.Tag;
import com.todo.Todo.entity.TagRepository;
import com.todo.Todo.entity.Todo;
import com.todo.Todo.entity.TodoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Controller
public class TodoController {

    @Autowired
    private TodoRepository repo;
    @Autowired
    private TagRepository tagRepo;

    private Todo lastEditedTodo;

    @GetMapping({"/", "index.html"})
    public String index(Model model) {
        model.addAttribute("todo", new Todo());
        model.addAttribute("todos", getAllTodos());
        return "index";
    }

    @PostMapping("/addTodo")
    public String addTodo(Todo todo){
        Todo cloneTodo = new Todo();
        cloneTodo.setTitle(todo.getTitle());
        cloneTodo.setDescription(todo.getDescription());
        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        Date date = new Date();
        cloneTodo.setCreatedAt(formatter.format(date));
        cloneTodo.setCurrentState(Todo.State.OPEN.toString());
        cloneTodo.setDueAt(todo.getDueAt());
        Todo savedTodo = repo.save(cloneTodo);
        return "redirect:/";
    }


    @GetMapping("/deleteTodo/{id}")
    public String deleteTodo(@PathVariable long id){
        repo.deleteById(id);
        return "redirect:/";
    }

    @PostMapping("/addTag")
    public String addTag(Tag tag){
        tagRepo.save(tag);
        return "redirect:/";
    }

    @GetMapping("/editTodo/{id}")
    public String editTodo(Model model, @PathVariable Long id){
        Optional<Todo> todoOpt = repo.findById(id);
        Todo todo = todoOpt.get();
        model.addAttribute("todo",todo );
        lastEditedTodo = todo;
        return "editTodo";
    }

    @PostMapping("/editTodo")
    public String editTodo(Todo todo, @RequestParam("file") MultipartFile image){
        todo.setId(lastEditedTodo.getId());
        todo.setCreatedAt(lastEditedTodo.getCreatedAt());
        repo.save(todo);
        return "redirect:/";
    }

    /* Queries */

    public List<Todo> getAllTodos(){
        return (ArrayList <Todo>) repo.findAll();
    }

    public List<Todo> filterTodosByTag(List<Todo> todos, Tag tag){
        List<Todo> filteredList = new ArrayList<>();
        for(Todo todo : todos){
            if(todo.getTagId().equals(tag.getId())){
                filteredList.add(todo);
            }
        }
        return filteredList;
    }

    public List<Todo> sortByCreatedAt(List<Todo> todos){
        todos.sort(new Comparator<Todo>() {
            @Override
            public int compare(Todo o1, Todo o2) {
                Date date1 = null, date2 = null;
                try {
                    date1 = DateFormat.getDateInstance().parse(o1.getCreatedAt());
                    date2 = DateFormat.getDateInstance().parse(o2.getCreatedAt());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                return (date1.compareTo(date2));
            }
        });
        return todos;
    }

    public List<Todo> sortByDueDate(List<Todo> todos){
        todos.sort(new Comparator<Todo>() {
            @Override
            public int compare(Todo o1, Todo o2) {
                Date date1 = null, date2 = null;
                try {
                    date1 = DateFormat.getDateInstance().parse(o1.getDueAt());
                    date2 = DateFormat.getDateInstance().parse(o2.getDueAt());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                return (date1.compareTo(date2));
            }
        });
        return todos;
    }
}
