package com.Utils;

import com.Misc.Processes.BonProcess;
import com.ObjectHub;
import com.ObjectTemplates.Bon;
import com.ObjectTemplates.Document;
import com.ObjectTemplates.Image;
import com.Telegram.Bot;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.apache.commons.io.FileUtils;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.io.File;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TessUtil {

    private static Pattern datePattern;

    private static Pattern numberPattern;

    private static void compilePatterns(){
        datePattern = Pattern.compile("\\s*(3[01]|[12][0-9]|0?[1-9])\\.(1[012]|0?[1-9])\\.((?:19|20)\\d{2})\\s*");
        numberPattern = Pattern.compile("\\d*\\.\\d*|\\d");
    }


    public static void processFolder(File folder, Bot bot, TableView tableView, TableColumn[] tableColumns,
            PropertyValueFactory[] propertyValueFactories) {
        Collection<File> filesInFolder = FileUtils.listFiles(new File(ObjectHub.getInstance().getProperties().getProperty("lastInputPath")),
                new String[] { "png", "PNG", "jpg", "JPG", "jpeg", "JPEG" }, true);
        Set<String> filePathSet = DBUtil.getFilePathOfDocsContainedInDB();
        AtomicInteger counterProcessedFiles = new AtomicInteger();
        filesInFolder.forEach(file -> {
            if (!filePathSet.contains(file.getAbsolutePath())) {
                ObjectHub.getInstance().getExecutorService().submit(new Runnable() {

                    @Override
                    public void run() {
                        processFile(file, null, bot);
                        counterProcessedFiles.getAndIncrement();
                    }
                });
            }
        });

        ExecutorUtil.blockUntilExecutorIsDone(ObjectHub.getInstance().getExecutorService(), filesInFolder.size());
        ObservableList<Document> documentObservableList = ControllerUtil
                .createObservableList(ObjectHub.getInstance().getArchiver().getDocumentList());
        ControllerUtil.fillTable(tableView, documentObservableList, tableColumns, propertyValueFactories);
        System.out.println("\n" + counterProcessedFiles.get() + " Files stored.");
    }

    public static void processFile(File inputfile, String user, Bot bot) {
        Tesseract tesseract = getTesseract();
        try {
            String result = tesseract.doOCR(inputfile);
            System.out.println(result);
            String dateOfFile = null;
            float sumIfBon = 0f;
            Document document = new Image(result, inputfile, DBUtil.countDocuments() + 1);
            try {
                dateOfFile = getFirstDate(result);
                if(checkIfBon(result)){
                    float sum = getLastNumber(result);
                    Bon bon = new Bon(result, inputfile, sum, document.getId());
                    if(bot != null){
                        Bot.process = new BonProcess(bon, bot);
                        return;
                    }

                }

            } catch (Exception e) {
                e.printStackTrace();
            }


            DBUtil.executeSQL("insert into Documents (id, content, originalFile, date, user) Values (" + DBUtil.countDocuments() + 1 + ", '" +
                     result.replaceAll("'", "''") + "', '" + inputfile.getAbsolutePath() + "', '" + dateOfFile + "', '" + user + "')");

            ObjectHub.getInstance().getArchiver().getDocumentList().add(document);
        } catch (TesseractException e) {
            e.printStackTrace();
        }
    }



    private static boolean checkIfBon(String content){
        String[] bonTerms = new String[]{"netto", "mwst", "einkauf", "summe", "aldi", "netto", "penny", "rewe", "real"};
        for(String term : bonTerms){
            if(content.toLowerCase().contains(term)){
                Bot.process = new BonProcess((Bon)Bot.idleDocumentOrNull, Bot.bot);
                return true;
            }
        }


        return false;
    }

    private static String getFirstDate(String documentData) throws Exception{

        if(datePattern == null){
            compilePatterns();
        }

        Matcher matcher = datePattern.matcher(documentData);
        DateTimeFormatter germanFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(Locale.GERMAN);

        String date = null;
        while(matcher.find()){
            date = matcher.group();
            //Always take first found date, since presumably its the one in the documents header
            break;
        }
        return date;
    }

    public static float getLastNumber(String content){
        if(numberPattern == null){
            compilePatterns();
        }
        Matcher matcher = numberPattern.matcher(content);
        List<String> numberList = new ArrayList<>();

        while(matcher.find()){
            numberList.add(matcher.group());
        }

        return Float.parseFloat(numberList.get(numberList.size() - 1));
    }

    private static Tesseract getTesseract() {
        Tesseract instance = new Tesseract();
        instance.setDatapath(ObjectHub.getInstance().getProperties().getProperty("tessData"));
        instance.setLanguage(ObjectHub.getInstance().getProperties().getProperty("tessLang="));
        instance.setHocr(true);
        return instance;
    }
}
