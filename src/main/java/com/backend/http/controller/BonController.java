package com.backend.http.controller;

import com.backend.BackendFacadeImpl;
import org.apache.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
public class BonController {

    private static Logger logger = Logger.getLogger(BonController.class);
    private final static String BON = "/bon";
    private BackendFacadeImpl backendFacade = null;

    public BonController(BackendFacadeImpl backendFacade) {
        this.backendFacade = backendFacade;
    }

    @GetMapping(BON + "/get")
    public ResponseEntity<Map<String, Float>> getMe(HttpServletRequest request)  {
        Float sumMe = backendFacade.getSum(Integer.parseInt(request.getHeader("userid")));
        Float sumAll = backendFacade.getSum();
        return ResponseEntity.ok(Map.of("me", sumMe, "all", sumAll));
    }
}
