package com.backend.http.controller;

import com.backend.BackendFacadeImpl;
import com.data.Bon;
import com.data.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
public class BonController extends Controller {
    private static final String COULD_NOT_PARSE_INCOMING_BON = "Could not parse incoming Bon";
    private static final String USERID = "userid";
    private static final String BON = "/bon";
    private BackendFacadeImpl backendFacade;

    @Autowired
    public BonController(BackendFacadeImpl backendFacade) {
        this.backendFacade = backendFacade;
    }

    @GetMapping(BON + "/get")
    public ResponseEntity<Map<String, Float>> get(HttpServletRequest request) {
        final String userid = request.getHeader(USERID);
        logger.info("{}{}/get from {}", getLogPrefrix(), BON, userid);
        Float sumMe = backendFacade.getSum(userid);
        Float sumAll = backendFacade.getSum("");
        return ResponseEntity.ok(Map.of("me", sumMe, "all", sumAll));
    }

    @GetMapping(BON + "/getLastBons")
    public ResponseEntity<List<Bon>> getLastBons(HttpServletRequest request) {
        final String userid = request.getHeader(USERID);
        final Integer lastMany = Integer.parseInt(request.getHeader("lastMany"));
        logger.info("{}{}/get from {}", getLogPrefrix(), BON, userid);
        List<Bon> lastSums = backendFacade.getLastBons(userid, lastMany);
        return ResponseEntity.ok(lastSums);
    }


    @PostMapping(BON + "/delete")
    public ResponseEntity<String> delete(@RequestBody Map map) {
        try {
            final String userid = (String) map.get(USERID);
            final UUID uuid = UUID.fromString((String) map.get("uuid"));
            backendFacade.delete(userid, uuid);
        } catch (Exception e) {
            logger.error(COULD_NOT_PARSE_INCOMING_BON, e);
            return ResponseEntity.ok(COULD_NOT_PARSE_INCOMING_BON);
        }
        return ResponseEntity.ok("");
    }

    @PostMapping(BON + "/sendWithPath")
    public ResponseEntity<String> sendWithPath(@RequestBody Map map) {
        try {
            final String userid = (String) map.get(USERID);
            float sum = Float.parseFloat(String.valueOf(map.get("sum")));

            File archivedPic = new File((String) map.get("pathToPic"));
            Bon bon = new Bon(backendFacade.getIdForNextDocument(), backendFacade.getAllowedUsers().get(userid),
                    archivedPic, sum, UUID.randomUUID());
            backendFacade.insertBon(bon);
        } catch (Exception e) {
            logger.error(COULD_NOT_PARSE_INCOMING_BON, e);
            return ResponseEntity.ok(COULD_NOT_PARSE_INCOMING_BON);
        }
        return ResponseEntity.ok("");
    }

    @RequestMapping(value = BON + "/send", method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseBody
    public ResponseEntity<String> send(@RequestParam Map<String, String> map, @RequestParam("file") MultipartFile[] submissions, HttpServletRequest request) {
        String betrag = map.get("betrag").replace(",", ".");

        MultipartFile uploadedFile = submissions[0];
        String originalFilename = uploadedFile.getOriginalFilename();
        boolean betragGiven = !betrag.isEmpty();
        boolean fileUploaded = !originalFilename.isEmpty();

        if(!betragGiven || ! fileUploaded) {
            return ResponseEntity.ok("Da fehlt wohl was (Betrag ohne Buchstaben!).");
        }

        float betragFloat = Float.parseFloat(betrag);
        try (InputStream inputOfStream = uploadedFile.getInputStream()) {
            byte[] bytesOfUploadedFile = inputOfStream.readAllBytes();

            File targetFolder = new File("../../Müll");
            File targetFile = new File(targetFolder, originalFilename);

            if (!targetFile.exists()) {
                targetFile.createNewFile();
            }
            File createdFile = Files.write(targetFile.toPath(), bytesOfUploadedFile).toFile();
            File copiedToArchiveFile = backendFacade.copyToArchive(createdFile, true);

            Bon bon = new Bon(backendFacade.getIdForNextDocument(), backendFacade.getAllowedUsers().get(map.get("userid")), copiedToArchiveFile, betragFloat, UUID.randomUUID());
            backendFacade.getDBDAO().insertBon(bon);
        } catch (Exception e) {
            logger.error(COULD_NOT_PARSE_INCOMING_BON, e);
            return ResponseEntity.ok(COULD_NOT_PARSE_INCOMING_BON);
        }

        Map<String, String[]> parameterMap = request.getParameterMap();
        String userid = parameterMap.get("userid")[0];
        String passw = parameterMap.get("passw")[0];
       /* String returnString = "<form action=\"\\bons\\addBonsHTML\\?userid=" + userid + "&passw=" + passw + "\" method=\"get\">" +
                "<input type=\"submit\" value=\"Go to my link location\"" +
                "name=\"Submit\" id=\"frm1_submit\" />" +
                "</form>";*/
        String returnString = getAddBonStringBuilder(false, false, userid, passw).toString();

        return ResponseEntity.ok("Neuer Bon hingezugefügt!" + returnString);
    }

    @RequestMapping(value = BON + "/addBonsHTML", method = RequestMethod.GET)
    public ResponseEntity<String> addBonsHTML(HttpServletRequest request, boolean betragFilledFloat, boolean pictureFilled) {
        Map<String, String[]> parameterMap = request.getParameterMap();
        String userid = parameterMap.get("userid")[0];
        String passw = parameterMap.get("passw")[0];
        betragFilledFloat = !(String.valueOf(parameterMap.get("betrag")).isEmpty());
        pictureFilled = parameterMap.containsKey("file") && !parameterMap.get("file")[0].isEmpty();
        if (betragFilledFloat && pictureFilled) {
            //send(request.getParameterMap(), );
            return ResponseEntity.ok("");
        }

        StringBuilder htmlBuilder = getAddBonStringBuilder(betragFilledFloat, pictureFilled, userid, passw);
        return ResponseEntity.ok(htmlBuilder.toString());
    }

    private StringBuilder getAddBonStringBuilder(boolean betragFilledFloat, boolean pictureFilled, String userid, String passw) {
        StringBuilder htmlBuilder = new StringBuilder("<html><body>");


        //htmlBuilder.append("<form action=\"\\bon\\send\" name=\"myForm\" enctype=\"text\" method=\"post\">");


        htmlBuilder.append("<form action=\"\\bon\\send?userid=" + userid + "&passw=" + passw + "\" onsubmit=\"this.submit();this.reset();return false;\" id=\"myForm\" enctype=\"multipart/form-data\" method=\"post\">");
        htmlBuilder.append("<p style=\"font-family:arial;\">");

        htmlBuilder.append("<input type=\"hidden\" name=\"userid\" id=\"userid\" value=\"" + userid + "\">");
        htmlBuilder.append("<input type=\"hidden\" name=\"passw\" id=\"passw\" value=\"" + passw + "\">");
        htmlBuilder.append("<label for=\"betrag\">Betrag:</label>");
        htmlBuilder.append("<input autocomplete=\"one-time-code\" type=\"text\" name=\"betrag\" id=\"betrag\">");

        htmlBuilder.append("<br><br><label for=\"file\">Bon-Bild</label><br>");
        htmlBuilder.append("<input autocomplete=\"one-time-code\" type=\"file\" id=\"file\" accept=\"image/*\" name=\"file\">");
        htmlBuilder.append("</label>");

        if (!pictureFilled || !betragFilledFloat) {
            htmlBuilder.append("Da fehlt noch was...");
        }

        htmlBuilder.append("<br><br><input autocomplete=\"one-time-code\" value=\"Abschicken\" type=\"submit\" name=\"submit\" onclick=\"send()\">");
        htmlBuilder.append("</form>");

        htmlBuilder.append("<br><br><label for=\"Bisher\">Bisher:</label>");

        Float sum = backendFacade.getSum(userid);
        Map<String, User> allowedUsers = backendFacade.getAllowedUsers();
        List<String> userIds = allowedUsers.values().stream().map(u -> u.getName()).collect(Collectors.toList());
        Float sumOfAllUsers = 0F;
        for (String userId : userIds) {
            Float sumOfUserId = backendFacade.getSum(userId);
            sumOfAllUsers += sumOfUserId;
        }

        htmlBuilder.append("</p><p style=\"font-family:arial; color:");
        if (sum > sumOfAllUsers) {
            htmlBuilder.append("red");
            htmlBuilder.append(";\">");
            htmlBuilder.append("-");
        } else {
            htmlBuilder.append("green");
            htmlBuilder.append(";\">");
            htmlBuilder.append("+");
        }


        float midSum = sumOfAllUsers / allowedUsers.size();

        float leftSum = Math.abs(sum - midSum);

        htmlBuilder.append(leftSum + "");

        if (!betragFilledFloat) {
            htmlBuilder.append("</p><p style=\"font-family:arial; color:red;\"><h1>Welcher Betrag? Format muss XX.YY sein</h1>");
        }
        if (!pictureFilled) {
            htmlBuilder.append("</p><p style=\"font-family:arial; color:red;\"><h1>Bild benötigt</h1>");
        }


        htmlBuilder.append("</p></body></html>");
        return htmlBuilder;
    }
}
