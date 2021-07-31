package com.backend.http.controller;

import com.backend.BackendFacade;
import com.data.Memo;
import com.data.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
public class MemoController extends Controller{

    private final static String MEMOS = "/memos";
    private BackendFacade facade;
    private ObjectMapper objectMapper;

    public MemoController(BackendFacade facade, ObjectMapper objectMapper) {
        this.facade = facade;
        this.objectMapper = objectMapper;
    }

    @PostMapping(MEMOS + "/new")
    public ResponseEntity<String> newEntry(@RequestBody Map map) {
        String userId = String.valueOf(map.get("userid"));
        try {
            String userString = (String) map.get("for");
            List<User> users;
            if (userString.equals("FORALL")) {
                users = new ArrayList<>(facade.getAllowedUsers().values());
            } else {
                users = List.of(facade.getAllowedUsers().get(userId));
            }

            String memoText = (String) map.get("name");
            final Memo memo = new Memo(users, memoText, LocalDateTime.now());
            facade.insertMemo(memo);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Could not parse incoming Memo", e);
            return ResponseEntity.ok("Could not parse incoming Memo");
        }
        return ResponseEntity.ok("");

    }

    @ResponseBody
    @RequestMapping(MEMOS + "/getMemos")
    public ResponseEntity<List<Memo>> getMemos(HttpServletRequest request) {
        final User user = facade.getAllowedUsers().get(request.getHeader("userid"));
        List<Memo> memos = facade.getMemos(user);
        return ResponseEntity.ok(memos);
    }
}
