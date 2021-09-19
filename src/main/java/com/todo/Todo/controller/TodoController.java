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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;

@Controller
public class TodoController {

    @Autowired
    private TodoRepository repo;
    @Autowired
    private TagRepository tagRepo;

    private Todo lastEditedTodo;
    private boolean listHasBeenAltered = false;
    private List<Todo> allTodos;
    private List<Todo> filteredList;

    private int currentSort = 1;
    private int currentFilter = 1;
    private int currentTag = 0;

    @GetMapping({"/", "index.html"})
    public String index(Model model) {
        model.addAttribute("todo", new Todo());
        if(!listHasBeenAltered) {
            allTodos = getAllOpenTodos();
        }

        if(currentFilter == 1) {
            filteredList = getAllOpenTodos();
            model.addAttribute("todos", filteredList);
            if(currentTag != 0){
                model.addAttribute("todos", filterTodosByTag(filteredList, currentTag));
            }
        } else if (currentFilter == 2){
            filteredList = filterByCompleted();
            if(currentTag == 0) {
                model.addAttribute("todos", filteredList);
            }
            else {
                model.addAttribute("todos", filterTodosByTag(filteredList, currentTag));
            }
        } else {
            filteredList = filterByIsDue(allTodos);
            if(currentTag == 0) {
                model.addAttribute("todos", filteredList);
            }
            else {
                model.addAttribute("todos", filterTodosByTag(filteredList, currentTag));
            }
        }

        if(currentSort == 1) {
            sortByCreatedAt((List<Todo>) model.getAttribute("todos"));
        } else {
            sortByDueDate((List<Todo>) model.getAttribute("todos"));
        }

        model.addAttribute("sort" , currentSort);
        model.addAttribute("filter" , currentFilter);
        model.addAttribute("currentTag" , currentTag);
        model.addAttribute("tags", tagRepo.findAll());
        return "index";
    }

    @GetMapping("/editTag")
    public String editTag(Model model){
        model.addAttribute("tag", new Tag());
        return "editTag";
    }

    @PostMapping("/editTag")
    public String editTagPost(Model model, Tag tag){
        tagRepo.save(tag);
        return "redirect:/";
    }

    // Filter endpoints
    @GetMapping("/filterByActive")
    public String getFilterByActive() {
        currentFilter = 1;
        return "redirect:/";
    }

    @GetMapping("/filterByCompleted")
    public String getFilterByCompleted() {
        currentFilter = 2;
        return "redirect:/";
    }

    @GetMapping("/filterByIsDue")
    public String getFilterByIsDue() {
        currentFilter = 3;
        return "redirect:/";
    }

    @GetMapping("/tag/{id}")
    public String filterByIsDue(Model model, @PathVariable long id) {
        filteredList = filterTodosByTag(allTodos, id);
        model.addAttribute("todo", new Todo());
        model.addAttribute("todos", filteredList);
        currentTag = (int) id;
        return "redirect:/";
    }


    // Sorting endpoint
    @GetMapping("/sortByDueDate")
    public String sortByDueDate(Model model) {
        model.addAttribute("todo", new Todo());
        allTodos = sortByDueDate(allTodos);
        model.addAttribute("todos", allTodos);
        listHasBeenAltered = true;
        currentSort = 2;
        return "redirect:/";
    }

    @GetMapping("/sortByCreationDate")
    public String sortByCreationDate(Model model) {
        model.addAttribute("todo", new Todo());
        allTodos = sortByCreatedAt(allTodos);
        model.addAttribute("todos", allTodos);
        listHasBeenAltered = true;
        currentSort = 1;
        return "redirect:/";
    }


    // CRUD endpoints
    @PostMapping("/addTodo")
    public String addTodo(Todo todo) {
        Todo cloneTodo = new Todo();
        cloneTodo.setTitle(todo.getTitle());
        cloneTodo.setDescription(todo.getDescription());
        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        Date date = new Date();
        cloneTodo.setCreatedAt(formatter.format(date));
        cloneTodo.setCurrentState(Todo.State.OPEN.toString());
        cloneTodo.setDueAt(todo.getDueAt());
        Todo savedTodo = repo.save(cloneTodo);
        listHasBeenAltered = false;
        return "redirect:/";
    }

    @GetMapping("/completeTodo/{id}")
    public String completeTodo(@PathVariable long id) {
        Todo todo = repo.findById(id).get();
        todo.setCurrentState(Todo.State.DONE.toString());
        repo.save(todo);
        listHasBeenAltered = false;
        return "redirect:/";
    }

    @GetMapping("/deleteTodo/{id}")
    public String deleteTodo(@PathVariable long id) {
        repo.deleteById(id);
        listHasBeenAltered = false;
        return "redirect:/";
    }

    @PostMapping("/addTag")
    public String addTag(Tag tag) {
        tagRepo.save(tag);
        return "redirect:/";
    }

    @GetMapping("/editTodo/{id}")
    public String editTodo(Model model, @PathVariable Long id) {
        Optional<Todo> todoOpt = repo.findById(id);
        Todo todo = todoOpt.get();
        model.addAttribute("todo", todo);
        model.addAttribute("tags", tagRepo.findAll());
        lastEditedTodo = todo;
        return "editTodo";
    }

    @PostMapping("/editTodo")
    public String editTodo(Todo todo, @RequestParam("file") MultipartFile image) {
        todo.setId(lastEditedTodo.getId());
        todo.setCreatedAt(lastEditedTodo.getCreatedAt());
        todo.setCurrentState(lastEditedTodo.getCurrentState());
        replaceEditedItem((List<Todo>) repo.findAll(), todo);
        repo.save(todo);
        return "redirect:/";
    }

    /* Queries */

    public List<Todo> getAllOpenTodos() {
        List<Todo> allTodos = (ArrayList<Todo>) repo.findAll();
        this.allTodos = allTodos;
        List<Todo> openTodos = new ArrayList<>();
        for(Todo todo : allTodos){
            if(todo.getCurrentState().equals("OPEN")){
                openTodos.add(todo);
            }
        }
        return openTodos;
    }

    public List<Todo> filterTodosByTag(List<Todo> todos, long tagId) {
        List<Todo> filteredList = new ArrayList<>();
        for (Todo todo : todos) {
            if (todo.getTag() != null && todo.getTag().getId().equals(tagId)) {
                filteredList.add(todo);
            }
        }
        return filteredList;
    }

    public List<Todo> sortByCreatedAt(List<Todo> todos) {
        todos.sort(new Comparator<Todo>() {
            @Override
            public int compare(Todo o1, Todo o2) {
                Date date1 = null, date2 = null;
                try {
                    SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
                    date1 = formatter.parse(o1.getCreatedAt());
                    date2 = formatter.parse(o2.getCreatedAt());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                return (date1.compareTo(date2));
            }
        });
        return todos;
    }

    public List<Todo> sortByDueDate(List<Todo> todos) {
        todos.sort(new Comparator<Todo>() {
            @Override
            public int compare(Todo o1, Todo o2) {
                LocalDate localDate1 = null, localDate2 = null;
                localDate1 = LocalDate.parse(o1.getDueAt());
                localDate2 = LocalDate.parse(o2.getDueAt());
                return localDate1.compareTo(localDate2);
            }
        });
        return todos;
    }

    public List<Todo> filterByActive(){
        List<Todo> todos = getAllOpenTodos();
        List<Todo> returnList = new ArrayList<>();
        for(Todo t : todos){
            if(t.getCurrentState().equals("OPEN")){
                returnList.add(t);
            }
        }
        return returnList;
    }

    public List<Todo> filterByCompleted(){
        List<Todo> allTodos = (ArrayList<Todo>) repo.findAll();
        List<Todo> returnList = new ArrayList<>();
        for(Todo t : allTodos){
            if(t.getCurrentState().equals("DONE")){
                returnList.add(t);
            }
        }
        return returnList;
    }

    public List<Todo> filterByIsDue(List<Todo> todos){
        List<Todo> returnList = new ArrayList<>();
        for(Todo t : todos){
            LocalDate dueDate = LocalDate.parse(t.getDueAt());
            LocalDate currentDate = LocalDate.now();
            if(dueDate.compareTo(currentDate) <= 0){
                returnList.add(t);
            }
        }
        return returnList;
    }

    public void replaceEditedItem(List<Todo> currentTodos, Todo editedTodo){
        int todoIndex = 0;
        for (Todo todo: currentTodos) {
            if(todo.getId().equals(editedTodo.getId())){
                break;
            }
            todoIndex++;
        }
        currentTodos.set(todoIndex, editedTodo);
    }
}
