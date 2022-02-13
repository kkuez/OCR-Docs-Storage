package com.backend.http.controller;

import com.backend.BackendFacade;
import com.backend.taskhandling.Task;
import com.backend.taskhandling.TaskFactory;
import com.backend.taskhandling.strategies.ExecutionStrategy;
import com.backend.taskhandling.strategies.StrategyFactory;
import com.backend.taskhandling.strategies.StrategyType;
import com.data.User;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
public class CalendarController extends Controller {
    private static final String COULD_NOT_PARSE_INCOMING_TASK = "Could not parse incoming Task";
    private static final String CALENDAR = "/calendar";
    private final BackendFacade facade;
    private final TaskFactory taskFactory;

    public CalendarController(BackendFacade facade, TaskFactory taskFactory) {
        this.facade = facade;
        this.taskFactory = taskFactory;
    }

    @PostMapping(CALENDAR + "/new")
    public ResponseEntity<String> newEntry(@RequestBody Map<String, String> map) {
        String userId = String.valueOf(map.get("userid"));
        try {
            String userString = map.get("for");
            List<User> users;
            if (userString.equals("FORALL")) {
                users = new ArrayList<>(facade.getAllowedUsers().values());
            } else {
                users = List.of(facade.getAllowedUsers().get(userId));
            }

            String taskText = map.get("name");
            Task task = taskFactory.createTask(users, taskText);
            LocalDateTime taskTime = LocalDateTime.parse(map.get("time"));
            String taskType = map.get("type");
            ExecutionStrategy strategy =
                    StrategyFactory.getStrategy(StrategyType.valueOf(taskType), taskTime, task, facade);
            task.setExecutionStrategy(strategy);
            facade.insertTask(task);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(COULD_NOT_PARSE_INCOMING_TASK, e);
            return ResponseEntity.ok(COULD_NOT_PARSE_INCOMING_TASK);
        }
        return ResponseEntity.ok("");
    }

    @PostMapping(CALENDAR + "/delete")
    public ResponseEntity<String> deleteEntry(@RequestBody Map map) {
        try {
            String eID = (String) map.get("eID");
            facade.deleteTask(UUID.fromString(eID));
        } catch (Exception e ) {
            logger.error(COULD_NOT_PARSE_INCOMING_TASK, e);
            return ResponseEntity.ok(COULD_NOT_PARSE_INCOMING_TASK);
        }
        return ResponseEntity.ok("");
    }

    @ResponseBody
    @RequestMapping(CALENDAR + "/getList")
    public ResponseEntity<List<Task>> getEntries(HttpServletRequest request) {
        List<Task> tasks = facade.getTasks(request.getHeader("userid"));
        return ResponseEntity.ok(tasks);
    }
}
