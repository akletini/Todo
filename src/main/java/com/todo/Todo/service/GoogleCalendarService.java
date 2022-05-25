package com.todo.Todo.service;

import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.tasks.Tasks;
import com.google.api.services.tasks.TasksScopes;
import com.google.api.services.tasks.model.Task;
import com.todo.Todo.entity.Todo;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Service
public class GoogleCalendarService {

    private static final String APPLICATION_NAME = "Todo app";
    private static final String TODO_TASK_ID = "c2pXT0NCREp0eFB6ME5HaA";
    private static final String TOKENS_DIRECTORY_PATH = "tokens";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final List<String> SCOPES = Collections.singletonList(TasksScopes.TASKS);
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";

    public void createTask(Todo todo, @RegisteredOAuth2AuthorizedClient("google")OAuth2AuthorizedClient client) {
        try {
            final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();

            Credential credential = new Credential(BearerToken.authorizationHeaderAccessMethod());
            credential.setAccessToken(client.getAccessToken().getTokenValue());
            Tasks service = new Tasks.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                    .setApplicationName(APPLICATION_NAME)
                    .build();

            Task task = new Task();
            task.setTitle(todo.getTitle());
            task.setKind("tasks#task");
            Date date = new SimpleDateFormat("yyyy-MM-dd").parse(todo.getDueAt());
            java.util.Calendar c = java.util.Calendar.getInstance();
            c.add(java.util.Calendar.HOUR, -1);
            date = c.getTime();
            task.setDue(new DateTime(date).toStringRfc3339());

            service.tasks().insert(TODO_TASK_ID, task).execute();
        } catch (IOException | GeneralSecurityException e) {
            System.err.println(e.getMessage());
        } catch (ParseException e) {
            System.err.println("Invalid date format");
        }
    }

    public List<Task> getTasksFromGoogleCalendar(@RegisteredOAuth2AuthorizedClient("google")OAuth2AuthorizedClient client) {
        List<Task> items = new ArrayList<>();
        try {
            final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            Credential credential = new Credential(BearerToken.authorizationHeaderAccessMethod());
            credential.setAccessToken(client.getAccessToken().getTokenValue());
            Tasks service = new Tasks.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                    .setApplicationName(APPLICATION_NAME)
                    .build();

            com.google.api.services.tasks.model.Tasks tasks = service.tasks().list(TODO_TASK_ID).execute();
            items = tasks.getItems();

        } catch (IOException | GeneralSecurityException e) {
            System.err.println(e.getMessage());
        }
        return items;
    }

    public void updateTaskInCalendar(Todo todo, List<Task> tasks, @RegisteredOAuth2AuthorizedClient("google")OAuth2AuthorizedClient client) {
        List<Task> items = new ArrayList<>();
        try {
            final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            Credential credential = new Credential(BearerToken.authorizationHeaderAccessMethod());
            credential.setAccessToken(client.getAccessToken().getTokenValue());
            Tasks service = new Tasks.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
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

    public void deleteTaskInCalendar(Todo todo,  List<Task> tasks, @RegisteredOAuth2AuthorizedClient("google")OAuth2AuthorizedClient client) {
        try {
            final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();

            Credential credential = new Credential(BearerToken.authorizationHeaderAccessMethod());
            credential.setAccessToken(client.getAccessToken().getTokenValue());
            Tasks service = new Tasks.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
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

    private Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        // Load client secrets.
        InputStream in = GoogleCalendarService.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .setApprovalPrompt("force")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        Credential userCredential = new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
        userCredential.refreshToken();
        return userCredential;
    }
}
