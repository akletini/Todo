package com.todo.Todo.controller;

import com.todo.Todo.entity.Tag;
import com.todo.Todo.entity.TagRepository;
import com.todo.Todo.entity.Todo;
import com.todo.Todo.entity.TodoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

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

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("todo", new Todo());
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

    @PostMapping("/addTag")
    public String addTag(Tag tag){
        tagRepo.save(tag);
        return "redirect:/";
    }

    /* Queries */

    public List<Todo> getAllTodos(){
        return (ArrayList <Todo>) repo.findAll();
    }

    public List<Todo> filterTodosByTag(List<Todo> todos, Tag tag){
        List<Todo> filteredList = new ArrayList<>();
        for(Todo todo : todos){
            if(todo.getTagId() == tag.getId()){
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
