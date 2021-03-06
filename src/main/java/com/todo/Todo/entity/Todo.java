package com.todo.Todo.entity;

import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.io.File;

@DynamicUpdate
@Entity
@Table(name = "todos")
public class Todo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String createdAt;
    private String dueAt;
    @Column(columnDefinition = "LONGTEXT")
    private String description;
    private File attachedFile;
    @OneToOne
    @JoinColumns({
            @JoinColumn(name="tag_id", referencedColumnName = "ID"),
            @JoinColumn(name="tag_name", referencedColumnName = "NAME"),
            @JoinColumn(name="tag_color", referencedColumnName = "COLOR")
    })
    private Tag tag;
    private String currentState;
    public enum State {OPEN, DONE};

    public Todo() {

    }
    public Todo(String title, String createdAt, String dueAt, String description, File attachedFile, Tag tag, String currentState) {
        super();
        this.title = title;
        this.createdAt = createdAt;
        this.dueAt = dueAt;
        this.description = description;
        this.attachedFile = attachedFile;
        this.tag = tag;
        this.currentState = currentState;
    }



    public String getCurrentState() {
        return currentState;
    }

    public void setCurrentState(String currentState) {
        this.currentState = currentState;
    }

    public Long getId() {
        return id;
    }

    public void setId(long id) { this.id = id;}

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getDueAt() {
        return dueAt;
    }

    public void setDueAt(String dueAt) {
        this.dueAt = dueAt;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public File getAttachedFile() {
        return attachedFile;
    }

    public void setAttachedFile(File attachedFile) {
        this.attachedFile = attachedFile;
    }

    public Tag getTag() {
        return tag;
    }

    public void setTag(Tag tag) {
        this.tag = tag;
    }
}
