package com.backend.http.controller;

import com.backend.BackendFacade;
import com.backend.ObjectHub;
import com.backend.taskhandling.Task;
import com.backend.taskhandling.TaskFactory;
import com.backend.taskhandling.strategies.ExecutionStrategy;
import com.backend.taskhandling.strategies.StrategyFactory;
import com.backend.taskhandling.strategies.StrategyType;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.objectTemplates.User;
import org.apache.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
public class CalendarController {

    private static Logger logger = Logger.getLogger(CalendarController.class);
    private final static String CALENDAR = "/calendar";
    private BackendFacade facade;
    private ObjectHub objectHub;
    private ObjectMapper objectMapper;
    private TaskFactory taskFactory;

    public CalendarController(BackendFacade facade, ObjectHub objectHub, ObjectMapper objectMapper,
                              TaskFactory taskFactory){
        this.facade = facade;
        this.objectHub = objectHub;
        this.objectMapper = objectMapper;
        this.taskFactory = taskFactory;
        objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        objectMapper.getSerializationConfig().getDefaultVisibilityChecker()
                .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
                .withGetterVisibility(JsonAutoDetect.Visibility.NONE);
    }

/*
    @PostMapping(CALENDAR + "/delete")
    public String deleteEntry(HttpServletRequest request) {
        //TODO
    }*/

    @PostMapping(CALENDAR + "/new")
    public ResponseEntity<String> newEntry(@RequestBody Map map) {
        try {
            String userString = (String) map.get("UserID");
            List<User> users;
            if (userString.equals("ALL")) {
                users = new ArrayList<>(facade.getAllowedUsers().values());
            } else {
                users = List.of(facade.getAllowedUsers().get(Integer.parseInt(userString)));
            }

            String taskText = (String) map.get("Name");
            Task task = taskFactory.createTask(users, taskText);
            String taskType = (String) map.get("Type");
            LocalDateTime taskTime = LocalDateTime.parse((CharSequence) map.get("Time"));
            ExecutionStrategy strategy =
                    StrategyFactory.getStrategy(StrategyType.valueOf(taskType), taskTime, task, facade);
            task.setExecutionStrategy(strategy);
            facade.insertTask(task);
        } catch (Exception e) {
            logger.error("Could not parse incoming Task", e);
            return ResponseEntity.ok("Could not parse incoming Task");
        }
        return ResponseEntity.ok("");
    }

    @PostMapping(CALENDAR + "/delete")
    public ResponseEntity<String> deleteEntry(@RequestBody Map map) {
        try {
            String eID = (String) map.get("eID");
            facade.deleteTask(UUID.fromString(eID));
        } catch (Exception e ) {
            logger.error("Could not parse incoming Task", e);
            return ResponseEntity.ok("Could not parse incoming Task");
        }
        return ResponseEntity.ok("");
    }

    @ResponseBody
    @RequestMapping(CALENDAR + "/getList")
    public ResponseEntity<Map<String, List<Task>>> getEntries(HttpServletRequest request) throws JsonProcessingException {
        List<Task> tasks = facade.getTasks(Integer.parseInt(request.getHeader("userid")));

        return ResponseEntity.ok(Map.of("Tasks", tasks));
    }
}
