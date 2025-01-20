package com.backend.http.controller;

import com.backend.BackendFacadeImpl;
import com.data.Bon;
import com.lowagie.text.pdf.codec.Base64;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

    @RequestMapping(value = BON + "/send", method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> send(@RequestParam Map<String, String> map, @RequestParam ("file") MultipartFile[] submissions, HttpServletRequest request) {
        MultipartFile uploadedFile = submissions[0];
        String originalFilename = uploadedFile.getOriginalFilename();
        try(InputStream inputOfStream = uploadedFile.getInputStream()) {
            byte[] bytesOfUploadedFile = inputOfStream.readAllBytes();

            File targetFolder = new File("../../Müll");
            File targetFile = new File(targetFolder, originalFilename);

            if (!targetFile.exists()) {
                targetFile.createNewFile();
            }
            Path path = Files.write(targetFile.toPath(), bytesOfUploadedFile);

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

    @RequestMapping(value = BON + "/addBonsHTML", method = RequestMethod.GET)
    public ResponseEntity<String> addBonsHTML(HttpServletRequest request) {
        String userid;
        String passw;
        Map<String, String[]> parameterMap = request.getParameterMap();
        if (parameterMap.size() != 2 || (!parameterMap.containsKey("userid") || ! parameterMap.containsKey("passw"))) {
            throw new RuntimeException("User ID or password not given!");
        } else {
            userid = parameterMap.get("userid")[0];
            passw = parameterMap.get("passw")[0];
        }

        Float sum = backendFacade.getSum(userid);
        List<Bon> lastBons = backendFacade.getLastBons(userid, Integer.valueOf(10));

        DateTimeFormatter taskTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nowPlusSevenDays = now.plusDays(7);
        LocalDateTime nowPlusThreeDays = now.plusDays(3);
        LocalDateTime nowPlusOneDay = now.plusDays(1);

        StringBuilder htmlBuilder = new StringBuilder("<html><body>");


        //htmlBuilder.append("<form action=\"\\bon\\send\" name=\"myForm\" enctype=\"text\" method=\"post\">");
        htmlBuilder.append("<form action=\"\\bon\\send\" name=\"myForm\" enctype=\"multipart/form-data\" method=\"post\">");
        htmlBuilder.append("<input type=\"hidden\" name=\"userid\" id=\"userid\" value=\"" + userid + "\"></p>");
        htmlBuilder.append("<input type=\"hidden\" name=\"passw\" id=\"passw\" value=\"" + passw + "\"></p>");

        htmlBuilder.append("<p><label for=\"first_name\">First Name:</label>");
        htmlBuilder.append("<input type=\"text\" name=\"first_name\" id=\"fname\"></p>");
        htmlBuilder.append("<p><label for=\"last_name\">Last Name:</label>");
        htmlBuilder.append("<input type=\"text\" name=\"last_name\" id=\"lname\"></p>");

        htmlBuilder.append("<label for=\"file\">File</label>");
        htmlBuilder.append("<input type=\"file\" id=\"file\" accept=\"image/*\" name=\"file\">");
        htmlBuilder.append("<button>Upload</button>");
        htmlBuilder.append("</label>");
        htmlBuilder.append("<br><input value=\"Submit\" type=\"submit\" onclick=\"send()\">");
        htmlBuilder.append("</form>");

                htmlBuilder.append("<b>sum<b><br>");
        htmlBuilder.append(" color:red\">");
        htmlBuilder.append(" color:green\">");
        //htmlBuilder.append(" color:orange\">");
        for (Bon bon : lastBons) {
            htmlBuilder.append("<p style=\"font-family:arial;");

            htmlBuilder.append(bon.getSum()).append("<br>");
        }

        htmlBuilder.append("</p></body></html>");
        return ResponseEntity.ok(htmlBuilder.toString());
    }
}
