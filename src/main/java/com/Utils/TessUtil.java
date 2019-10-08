package com.Utils;

import com.Controller.Reporter.ProgressReporter;
import com.ObjectHub;
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

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TessUtil {

    private static Pattern datePattern;

    private static Pattern germanDatePattern;

    private static Pattern numberPattern;

    private static void compilePatterns(){
        datePattern = Pattern.compile("\\s*(3[01]|[12][0-9]|0?[1-9])\\.(1[012]|0?[1-9])\\.((?:19|20)\\d{2})\\s*");
        germanDatePattern = Pattern.compile("\\d\\.\\d\\.\\d\\d\\d\\d");
        numberPattern = Pattern.compile("\\d*\\.\\d*|\\d");
    }

    public static Set<Document> processFolder(File folder, Bot bot, TableView tableView, TableColumn[] tableColumns,
                                              PropertyValueFactory[] propertyValueFactories, ProgressReporter progressReporter) {
        Collection<File> filesInFolder = FileUtils.listFiles(new File(ObjectHub.getInstance().getProperties().getProperty("lastInputPath")),
                new String[] { "pdf", "PDF", "png", "PNG", "jpg", "JPG", "jpeg", "JPEG" }, false);
        Collection<File> absoluteDifferentFilesSet = IOUtil.createFileSetBySize(filesInFolder);
        Set<String> filePathSet = DBUtil.getFilePathOfDocsContainedInDB();
        AtomicInteger counterProcessedFiles = new AtomicInteger();

        progressReporter.setTotalSteps(absoluteDifferentFilesSet.size(), null);

        Set<Document> documentSet = new HashSet<>();
        absoluteDifferentFilesSet.forEach(file -> {
            if (!filePathSet.contains(file.getAbsolutePath())) {
                ObjectHub.getInstance().getExecutorService().submit(new Runnable() {

                    @Override
                    public void run() {
                        if(!DBUtil.isFilePresent(file)) {
                            Document document = processFile(file, 0, null);
                            documentSet.add(document);
                        }
                        progressReporter.addStep(null);
                        counterProcessedFiles.getAndIncrement();
                    }
                });
            }
        });
        ExecutorUtil.blockUntilExecutorIsDone(ObjectHub.getInstance().getExecutorService(), filesInFolder.size());
        ObservableList<Document> documentObservableList = ControllerUtil
                .createObservableList(ObjectHub.getInstance().getArchiver().getDocumentList());
        ControllerUtil.fillTable(tableView, documentObservableList, tableColumns, propertyValueFactories);
        LogUtil.log(counterProcessedFiles.get() + " Files stored.");
//FIXME Tags werden nach dem verarbeiten nicht in der tableview angezeigt
        return documentSet;
    }

    public static Document processFile(File inputfile, int userID, Set<String> tagSet) {
        LogUtil.log("Processing " + inputfile.getAbsolutePath());
        Tesseract tesseract = getTesseract();
        Document document = null;
        try {
            String result = tesseract.doOCR(inputfile);
            document = new Image(result, inputfile, DBUtil.countDocuments("Documents", "") );
            String date = getFirstDate(result);
            date = date == null ? LocalDate.now().toString() : date;
            document.setDate(date);
            document.setUser(userID);
            File newOriginalFilePath = new File(ObjectHub.getInstance().getArchiver().getDocumentFolder(), document.getOriginalFileName());
            if(!newOriginalFilePath.exists()) {
                FileUtils.copyFile(document.getOriginFile(), newOriginalFilePath);
            }
            document.setOriginFile(newOriginalFilePath);
            document.setDate(getFirstDate(document.getContent()));

            DBUtil.insertDocumentToDB(document);

            if(tagSet != null){
                for(String tag : tagSet){
                        DBUtil.executeSQL("insert into Tags (belongsToDocument, Tag) Values (" + document.getId() + ", '" + tag + "');" );
                }
                document.setTagSet(tagSet);
            }

            ObjectHub.getInstance().getArchiver().getDocumentList().add(document);
        } catch (TesseractException e) {
            LogUtil.logError(null, e);
        } catch (Exception e) {
            LogUtil.logError(null, e);
        }
        return document;
    }



    public static boolean checkIfBon(String content){
        String[] bonTerms = new String[]{"netto", "mwst", "einkauf", "summe", "aldi", "netto", "penny", "rewe", "real", "lidl"};
        for(String term : bonTerms){
            if(content.toLowerCase().contains(term)){
                return true;
            }
        }
        return false;
    }

    public static String getFirstDate(String documentData) throws Exception{

        if(datePattern == null || germanDatePattern == null){
            compilePatterns();
        }
        String date = getDateWithPattern(datePattern, documentData);
        if(date == null){
            date = getDateWithPattern(germanDatePattern, documentData);
        }
        if(date == null){
            DateTimeFormatter germanFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(Locale.GERMAN);
            date = LocalDate.now().format(germanFormatter);
        }

        return date;
    }

    private static String getDateWithPattern(Pattern pattern, String string){
        Matcher matcher = pattern.matcher(string);

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
        float lastNumer = 0;
        try{
            lastNumer = Float.parseFloat(numberList.get(numberList.size() - 1).replace(",","."));
        }catch (Exception e){
            LogUtil.logError(numberList.get(numberList.size() - 1).replace(",","."), e);
        }

        return lastNumer;
    }

    private static Tesseract getTesseract() {
        Tesseract instance = new Tesseract();
        String datapath = "";
        if(ObjectHub.getInstance().getInputArgs().containsKey("tessdata")){
            datapath = ObjectHub.getInstance().getInputArgs().get("tessdata");
            LogUtil.log("Alternative datapath set.");
        }else{
            datapath = ObjectHub.getInstance().getProperties().getProperty("tessData");
        }

        instance.setDatapath(datapath);

        instance.setLanguage(ObjectHub.getInstance().getProperties().getProperty("tessLang"));
        instance.setHocr(true);
        return instance;
    }
}
