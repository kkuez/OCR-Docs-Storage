package com.backend.http.controller;

import com.backend.BackendFacadeImpl;
import com.lowagie.text.pdf.codec.Base64;
import com.objectTemplates.Bon;
import com.objectTemplates.User;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
public class BonController extends Controller{

    private final static String BON = "/bon";
    private BackendFacadeImpl backendFacade = null;

    @Autowired
    public BonController(BackendFacadeImpl backendFacade) {
        this.backendFacade = backendFacade;
    }

    @GetMapping(BON + "/get")
    public ResponseEntity<Map<String, Float>> getMe(HttpServletRequest request)  {
        final int userid = Integer.parseInt(request.getHeader("userid"));
        logger.info(getLogPrefrix() + BON + "/get from " + userid);
        Float sumMe = backendFacade.getSum(userid);
        Float sumAll = backendFacade.getSum();
        return ResponseEntity.ok(Map.of("me", sumMe, "all", sumAll));
    }

    @PostMapping(BON + "/send")
    public ResponseEntity<String> send(@RequestBody Map map) {
        try {
            float sum = Float.parseFloat(String.valueOf(map.get("sum")));
            byte[] fileBytes = Base64.decode(String.valueOf(map.get("file")));
            User user = backendFacade.getAllowedUsers().get(Integer.parseInt(String.valueOf(map.get("userid"))));
            logger.info(getLogPrefrix() + BON + "/send from " + user.getId());
            File newPic = new File(FileUtils.getTempDirectory(), user.getName() + "_" + LocalDateTime.now().toString().replace(".", "-").replace(":", "-") + ".jpg");
            newPic.createNewFile();
            FileOutputStream fos = new FileOutputStream(newPic);
            fos.write(fileBytes);
            fos.flush();

            File archivedPic = backendFacade.copyToArchive(newPic, true);
            Bon bon = new Bon(backendFacade.getIdForNextDocument(), user, archivedPic, sum);
            backendFacade.insertBon(bon);
        } catch (Exception e ) {
            logger.error("Could not parse incoming Bon", e);
            return ResponseEntity.ok("Could not parse incoming Bon");
        }
        return ResponseEntity.ok("");
    }
}
