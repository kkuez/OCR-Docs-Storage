package com.backend.http.controller;

import com.backend.BackendFacade;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@RestController
public class ShoppingListController extends Controller {

    private static final String SHOPPINGLIST = "/shoppinglist";
    private final BackendFacade facade;
    private final ObjectMapper objectMapper;
    private final static String LIST_ITEM_SEPERATOR = "´#§";

    public ShoppingListController(BackendFacade facade, ObjectMapper objectMapper) {
        this.facade = facade;
        this.objectMapper = objectMapper;
    }

    // TODO return ResponseBody!
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

    // TODO return ResponseBody!
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

    // TODO return ResponseBody!
    @GetMapping(SHOPPINGLIST + "/get")
    public String getList() throws JsonProcessingException {
        List<String> shoppingList = facade.getShoppingList().stream()
                .map(itemString -> itemString.replace(",", LIST_ITEM_SEPERATOR))
                .collect(Collectors.toList());

        return objectMapper.writeValueAsString(shoppingList);
    }

    // TODO return ResponseBody!
    @PostMapping(SHOPPINGLIST + "/deleteAll")
    public String deleteAll() {
        facade.getShoppingList().forEach(facade::deleteFromShoppingList);
        return "Ok";
    }
}
