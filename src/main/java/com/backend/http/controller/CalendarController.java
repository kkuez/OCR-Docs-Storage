package com.backend.http.controller;

import com.backend.BackendFacade;
import com.backend.taskhandling.Task;
import com.backend.taskhandling.TaskFactory;
import com.backend.taskhandling.strategies.ExecutionStrategy;
import com.backend.taskhandling.strategies.StrategyFactory;
import com.backend.taskhandling.strategies.StrategyType;
import com.data.User;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

    @PostMapping(value = CALENDAR + "/news", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    //public ResponseEntity<String> newEntry(@RequestParam("productDto") String jsonString) {
    public ResponseEntity<String> newEntry(@RequestParam("userid") String userid,
                                           @RequestParam("passw") String passw,
                                           @RequestParam("for") String forIn,
                                           @RequestParam("name") String name,
                                           @RequestParam("time") String time,
                                           @RequestParam("type") String type) {
        return addNewTask(Map.of("userid", userid, "passw", passw, "for", forIn, "name", name, "time", time, "type", type));
    }
    @PostMapping(CALENDAR + "/new")
    public ResponseEntity<String> newEntry(@RequestBody Map<String, String> map) {
        return addNewTask(map);
    }

    private ResponseEntity<String> addNewTask(Map<String, String> map) {
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

    @ResponseBody
    @RequestMapping(CALENDAR + "/getListHTML")
    public ResponseEntity<String> getEntriesHTML(HttpServletRequest request) {
        String userid;
        Map<String, String[]> parameterMap = request.getParameterMap();
        if (parameterMap.size() != 2 || (!parameterMap.containsKey("userid") || ! parameterMap.containsKey("passw"))) {
                throw new RuntimeException("User ID or password not given!");
            } else {
                userid = parameterMap.get("userid")[0];
            }
        List<Task> tasks = facade.getTasks(userid);
        tasks.sort((t1, t2) -> t1.getExecutionStrategy().getTime().compareTo(t2.getExecutionStrategy().getTime()));

        StringBuilder htmlBuilder = new StringBuilder("<html><head></head><body>");
        DateTimeFormatter taskTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nowPlusSevenDays = now.plusDays(7);
        LocalDateTime nowPlusThreeDays = now.plusDays(3);
        LocalDateTime nowPlusOneDay = now.plusDays(1);
        for (Task task : tasks) {
            htmlBuilder.append("<p style=\"font-family:arial;");
            LocalDateTime taskTime = task.getExecutionStrategy().getTime();

            if(taskTime.isBefore(nowPlusSevenDays) && taskTime.isAfter(nowPlusThreeDays)) {
                htmlBuilder.append(" color:green\">");
            } else if(taskTime.isBefore(nowPlusThreeDays) && taskTime.isAfter(nowPlusOneDay)) {
                htmlBuilder.append(" color:#FF5733\">");
            } else if(taskTime.isBefore(nowPlusOneDay)) {
                htmlBuilder.append(" color:red\">");
            } else {
                htmlBuilder.append("\">");
            }

            htmlBuilder.append("<b>");
            htmlBuilder.append(task.getName());
            htmlBuilder.append("</b>");
            htmlBuilder.append("<br>");
            String timeString = taskTimeFormatter.format(task.getExecutionStrategy().getTime());
            htmlBuilder.append(timeString);
            htmlBuilder.append("<br>");
            htmlBuilder.append(task.getForWhom());
            htmlBuilder.append("<br>");
            htmlBuilder.append("<br>");
            htmlBuilder.append("</p>");
        }
        htmlBuilder.append("</p></body></html>");
        return ResponseEntity.ok(htmlBuilder.toString());
    }
}
