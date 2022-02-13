package com.backend.http.controller;

import com.backend.BackendFacade;
import com.data.Memo;
import com.data.User;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
public class MemoController extends Controller{

    private static final String MEMOS = "/memos";
    private final BackendFacade facade;

    public MemoController(BackendFacade facade) {
        this.facade = facade;
    }

    @PostMapping(MEMOS + "/new")
    public ResponseEntity<String> newEntry(@RequestBody Map map) {
        try {
            String userString = (String) map.get("for");
            List<String> userNames;
            if (userString.equals("FORALL")) {
                userNames = facade.getAllowedUsers().values().stream().map(User::getName).collect(Collectors.toList());
            } else {
                userNames = List.of(userString);
            }

            String memoText = (String) map.get("name");
            final Memo memo = new Memo(userNames, memoText, LocalDateTime.now());
            facade.insertMemo(memo);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Could not parse incoming Memo", e);
            return ResponseEntity.ok("Could not parse incoming Memo");
        }
        return ResponseEntity.ok("");

    }

    @PostMapping(MEMOS + "/delete")
    public ResponseEntity.HeadersBuilder<?> delete(@RequestBody Map map, HttpServletRequest request) {
        final User user = facade.getAllowedUsers().get(request.getHeader("userid"));
        String userString = (String) map.get("for");
        String memoText = (String) map.get("name");
        final List<Memo> memosToDelete = new ArrayList<>();
        for (Memo memo : facade.getMemos(user)) {
            if(memo.getMemoText().equals(memoText)) {
                memosToDelete.add(memo);
            }
        }

        if(memosToDelete.isEmpty()) {
            return ResponseEntity.notFound();
        }

        List<User> users = new ArrayList<>();
        if(userString.equals("FORALL")) {
            users.addAll(facade.getAllowedUsers().values());
        } else {
            users.add(user);
        }

        facade.deleteMemo(memosToDelete, users);
        return ResponseEntity.ok();
    }

    @ResponseBody
    @RequestMapping(MEMOS + "/getMemos")
    public ResponseEntity<List<Memo>> getMemos(HttpServletRequest request) {
        final User user = facade.getAllowedUsers().get(request.getHeader("userid"));
        List<Memo> memos = facade.getMemos(user);
        return ResponseEntity.ok(memos);
    }
}
