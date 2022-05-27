package com.todo.Todo.service;

import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.tasks.Tasks;
import com.google.api.services.tasks.TasksScopes;
import com.google.api.services.tasks.model.Task;
import com.todo.Todo.entity.Todo;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.oauth2.client.*;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class GoogleCalendarService {

    private static final String APPLICATION_NAME = "Todo app";
    private static final String TODO_TASK_ID = "c2pXT0NCREp0eFB6ME5HaA";
    private static final String TOKENS_DIRECTORY_PATH = "tokens";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final List<String> SCOPES = Collections.singletonList(TasksScopes.TASKS);
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";

    @Async
    public void createTask(Todo todo, @RegisteredOAuth2AuthorizedClient("google")OAuth2AuthorizedClient client) {
        try {
            final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();

            Tasks service = new Tasks.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(client))
                    .setApplicationName(APPLICATION_NAME)
                    .build();

            Task task = new Task();
            task.setTitle(todo.getTitle());
            task.setKind("tasks#task");
            Date date = new SimpleDateFormat("yyyy-MM-dd").parse(todo.getDueAt());
            task.setDue(new DateTime(date).toStringRfc3339());

            service.tasks().insert(TODO_TASK_ID, task).execute();
        } catch (IOException | GeneralSecurityException e) {
            System.err.println(e.getMessage());
        } catch (ParseException e) {
            System.err.println("Invalid date format");
        }
    }


    public List<Task> getTasksFromGoogleCalendar(@RegisteredOAuth2AuthorizedClient("google") OAuth2AuthorizedClient client) {
        List<Task> items = new ArrayList<>();
        try {
            final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();

            Tasks service = new Tasks.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(client))
                    .setApplicationName(APPLICATION_NAME)
                    .build();

            com.google.api.services.tasks.model.Tasks tasks = service.tasks().list(TODO_TASK_ID).execute();
            items = tasks.getItems();

        } catch (IOException | GeneralSecurityException e) {
            System.err.println(e.getMessage());
        }
        return items;
    }
    @Async
    public void updateTaskInCalendar(Todo todo, List<Task> tasks, @RegisteredOAuth2AuthorizedClient("google") OAuth2AuthorizedClient client) {
        List<Task> items = new ArrayList<>();
        try {
            final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();

            Tasks service = new Tasks.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(client))
                    .setApplicationName(APPLICATION_NAME)
                    .build();

            Task currentTask = new Task();
            for (Task t : tasks) {
                if (t.getTitle().equals(todo.getTitle())) {
                    currentTask = t;
                    break;
                }
            }
            if (currentTask.isEmpty()) {
                return;
            }
            Task task = service.tasks().get(TODO_TASK_ID, currentTask.getId()).execute();
            task.setStatus("OPEN".equals(todo.getCurrentState()) ? "needsAction" : "completed");
            task.setNotes(buildDescription(todo));
            service.tasks().update(TODO_TASK_ID, currentTask.getId(), task).execute();

        } catch (IOException | GeneralSecurityException e) {
            System.err.println(e.getMessage());
        }
    }

    @Async
    public void deleteTaskInCalendar(Todo todo,  List<Task> tasks, @RegisteredOAuth2AuthorizedClient("google") OAuth2AuthorizedClient client) {
        try {
            final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();

            Tasks service = new Tasks.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(client))
                    .setApplicationName(APPLICATION_NAME)
                    .build();

            Task currentTask = new Task();
            for (Task t : tasks) {
                if (t.getTitle().equals(todo.getTitle())) {
                    currentTask = t;
                    break;
                }
            }
            if (currentTask.isEmpty()) {
                return;
            }

            service.tasks().delete(TODO_TASK_ID, currentTask.getId()).execute();
        } catch (IOException | GeneralSecurityException e) {
            System.err.println(e.getMessage());
        }
    }

    private String buildDescription(Todo todo) {
        String description = "";
        if (todo.getTag() != null) {
            description += "TAG: " + todo.getTag().getName() + "\n\n";
        }
        if (todo.getDescription() != null) {
            description += todo.getDescription();
        }
        return description;
    }

    private Credential getCredentials(OAuth2AuthorizedClient client) {
        Credential credential = new Credential(BearerToken.authorizationHeaderAccessMethod());
        credential.setAccessToken(client.getAccessToken().getTokenValue());
        if (client.getRefreshToken() != null) {
            credential.setRefreshToken(client.getRefreshToken().getTokenValue());
        }
        return credential;
    }

}
