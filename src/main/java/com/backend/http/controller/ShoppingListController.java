package com.backend.http.controller;

import com.backend.BackendFacade;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.Logger;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;


@RestController
public class ShoppingListController {

    private static final String SHOPPINGLIST = "shoppinglist/";
    private static Logger logger = Logger.getLogger(ShoppingListController.class);

    private BackendFacade facade;
    private ObjectMapper objectMapper;

    public ShoppingListController(BackendFacade facade, ObjectMapper objectMapper) {
        this.facade = facade;
        this.objectMapper = objectMapper;
    }

    @PostMapping(SHOPPINGLIST + "/new")
    public String newEntry(HttpServletRequest request) {
        try {
            Map<String, String[]> parameterMap = request.getParameterMap();
            String userString = parameterMap.get("user")[0];


        } catch (Exception e ) {
            logger.error("Could not parse incoming Task", e);
            return "Failed";
        }
        return "Ok";
    }

    @GetMapping(SHOPPINGLIST + "/get")
    public String getList() throws JsonProcessingException {
        return objectMapper.writeValueAsString(facade.getShoppingList());
    }

    @PostMapping(SHOPPINGLIST + "/delete")
    public String deleteEntry(HttpServletRequest request) {
        try {
            Map<String, String[]> parameterMap = request.getParameterMap();
            String itemString = parameterMap.get("item")[0];
            facade.deleteFromShoppingList(itemString);
        } catch (Exception e ) {
            logger.error("Could not parse incoming item", e);
            return "Failed";
        }
        return "Ok";
    }

    @PostMapping(SHOPPINGLIST + "/deleteAll")
    public String deleteAll() {
        facade.getShoppingList().forEach(facade::deleteFromShoppingList);
        return "Ok";
    }
}
