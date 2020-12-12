package com.backend.http.controller;

import com.backend.BackendFacade;
import com.backend.encryption.XORCrypt;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.Logger;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;


@RestController
public class ShoppingListController extends Controller {

    private static final String SHOPPINGLIST = "/shoppinglist";
    private static Logger logger = Logger.getLogger(ShoppingListController.class);

    private BackendFacade facade;
    private ObjectMapper objectMapper;

    public ShoppingListController(BackendFacade facade, ObjectMapper objectMapper, XORCrypt xorCrypt) {
        super(xorCrypt);
        this.facade = facade;
        this.objectMapper = objectMapper;
    }

    @PostMapping(SHOPPINGLIST + "/delete")
    public String deleteEntry(@RequestBody Map map) {
        try {
            String item = String.valueOf(map.get("item"));
            facade.deleteFromShoppingList(item);
        } catch (Exception e ) {
            logger.error("Could not parse incoming Task", e);
            return "Failed";
        }
        return "Ok";
    }

    @PostMapping(SHOPPINGLIST + "/send")
    public String newEntry(@RequestBody Map map) {
        try {
            String userString = String.valueOf(map.get("userid"));
            String item = String.valueOf(map.get("item"));
            facade.insertShoppingItem(item);

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

    @PostMapping(SHOPPINGLIST + "/deleteAll")
    public String deleteAll() {
        facade.getShoppingList().forEach(facade::deleteFromShoppingList);
        return "Ok";
    }
}
