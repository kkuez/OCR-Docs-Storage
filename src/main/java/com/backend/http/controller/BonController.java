package com.backend.http.controller;

import com.backend.BackendFacadeImpl;
import com.lowagie.text.pdf.codec.Base64;
import com.data.Bon;
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
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
public class BonController extends Controller{
    private static final String COULD_NOT_PARSE_INCOMING_BON = "Could not parse incoming Bon";
    private static final String USERID = "userid";
    private static final String BON = "/bon";
    private BackendFacadeImpl backendFacade;

    @Autowired
    public BonController(BackendFacadeImpl backendFacade) {
        this.backendFacade = backendFacade;
    }

    @GetMapping(BON + "/get")
    public ResponseEntity<Map<String, Float>> get(HttpServletRequest request)  {
        final String userid = request.getHeader(USERID);
        logger.info("{}{}/get from {}", getLogPrefrix(), BON, userid);
        Float sumMe = backendFacade.getSum(userid);
        Float sumAll = backendFacade.getSum("");
        return ResponseEntity.ok(Map.of("me", sumMe, "all", sumAll));
    }

    @GetMapping(BON + "/getLastBons")
    public ResponseEntity<List<Bon>> getLastBons(HttpServletRequest request)  {
        final String userid = request.getHeader(USERID);
        final Integer lastMany = Integer.parseInt(request.getHeader("lastMany"));
        logger.info("{}{}/get from {}", getLogPrefrix(), BON, userid);
        List<Bon> lastSums = backendFacade.getLastBons(userid, lastMany);
        return ResponseEntity.ok(lastSums);
    }

    @PostMapping(BON + "/send")
    public ResponseEntity<String> send(@RequestBody Map map) {
        try {
            final String userid = (String)map.get(USERID);
            float sum = Float.parseFloat(String.valueOf(map.get("sum")));
            byte[] fileBytes = Base64.decode(String.valueOf(map.get("file")));
            logger.info("{}{}/send from {}", getLogPrefrix(), BON, userid);
            File newPic = new File(FileUtils.getTempDirectory(),  userid + "_" + LocalDateTime.now().toString().replace(".", "-").replace(":", "-") + ".jpg");
            newPic.createNewFile();

            try (FileOutputStream fos = new FileOutputStream(newPic)) {
                fos.write(fileBytes);
                fos.flush();
            }

            File archivedPic = backendFacade.copyToArchive(newPic, true);
            Bon bon = new Bon(backendFacade.getIdForNextDocument(), backendFacade.getAllowedUsers().get(userid),
                    archivedPic, sum, UUID.randomUUID());
            backendFacade.insertBon(bon);
        } catch (Exception e ) {
            logger.error(COULD_NOT_PARSE_INCOMING_BON, e);
            return ResponseEntity.ok(COULD_NOT_PARSE_INCOMING_BON);
        }
        return ResponseEntity.ok("");
    }

    @PostMapping(BON + "/sendWithPath")
    public ResponseEntity<String> sendWithPath(@RequestBody Map map) {
        try {
            final String userid = (String)map.get(USERID);
            float sum = Float.parseFloat(String.valueOf(map.get("sum")));

            File archivedPic = new File((String) map.get("pathToPic"));
            Bon bon = new Bon(backendFacade.getIdForNextDocument(), backendFacade.getAllowedUsers().get(userid),
                    archivedPic, sum, UUID.randomUUID());
            backendFacade.insertBon(bon);
        } catch (Exception e ) {
            logger.error(COULD_NOT_PARSE_INCOMING_BON, e);
            return ResponseEntity.ok(COULD_NOT_PARSE_INCOMING_BON);
        }
        return ResponseEntity.ok("");
    }

    @PostMapping(BON + "/delete")
    public ResponseEntity<String> delete(@RequestBody Map map) {
        try {
            final String userid = (String)map.get(USERID);
            final UUID uuid = UUID.fromString((String) map.get("uuid"));
            backendFacade.delete(userid, uuid);
        } catch (Exception e ) {
            logger.error(COULD_NOT_PARSE_INCOMING_BON, e);
            return ResponseEntity.ok(COULD_NOT_PARSE_INCOMING_BON);
        }
        return ResponseEntity.ok("");
    }
}
