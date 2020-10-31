package com.backend.http.controller;

import com.backend.BackendFacade;
import com.backend.taskhandling.Task;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpRequest;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class CalendarController {
    private final static String CALENDAR = "/calendar";
    private BackendFacade facade;
    private ObjectMapper objectMapper;

    public CalendarController(BackendFacade facade, ObjectMapper objectMapper){
        this.facade = facade;
        this.objectMapper = objectMapper;
        objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        objectMapper.getSerializationConfig().getDefaultVisibilityChecker()
                .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
                .withGetterVisibility(JsonAutoDetect.Visibility.NONE);
    }

    @RequestMapping(CALENDAR + "/new")
    public String newEntry(HttpRequest request) {
        request.getHeaders().get
        return "";
    }

    @ResponseBody
    @RequestMapping(CALENDAR)
    public String getEntries() throws JsonProcessingException {
        List<Task> tasks = facade.getTasks();

        return objectMapper.writeValueAsString(Map.of("Tasks", tasks));
    }


}
