package com.Utils;

import com.ObjectHub;
import com.ObjectTemplates.Document;
import com.ObjectTemplates.Image;
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

    private static void compileDatePattern(){
        datePattern = Pattern.compile("\\s*(3[01]|[12][0-9]|0?[1-9])\\.(1[012]|0?[1-9])\\.((?:19|20)\\d{2})\\s*");
    }

    public static void processFolder(File folder) {
        Collection<File> filesInFolder = FileUtils.listFiles(folder,
                new String[] { "png", "PNG", "jpg", "JPG", "jpeg", "JPEG" }, true);
        filesInFolder.forEach(file -> processFile(file));
    }

    public static void processFolder(File folder, TableView tableView, TableColumn[] tableColumns,
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
                        processFile(file);
                        counterProcessedFiles.getAndIncrement();
                    }
                });
            }
        });

        ExecutorUtil.blockUntilExecutorIsDone(ObjectHub.getInstance().getExecutorService());
        ObservableList<Document> documentObservableList = ControllerUtil
                .createObservableList(ObjectHub.getInstance().getArchiver().getDocumentList());
        ControllerUtil.fillTable(tableView, documentObservableList, tableColumns, propertyValueFactories);
        System.out.println("\n" + counterProcessedFiles.get() + " Files stored.");
    }

    public static void processFile(File inputfile) {
        Tesseract tesseract = getTesseract();
        try {
            String result = tesseract.doOCR(inputfile);
            System.out.println(result);
            String dateOfFile = null;
            try {
                dateOfFile = getFirstDate(result);
            } catch (Exception e) {
                e.printStackTrace();
            }

            DBUtil.executeSQL("insert into Documents (id, content, originalFile, date) Values (1, '"
                    + result.replaceAll("'", "''") + "', '" + inputfile.getAbsolutePath() + "', '" + dateOfFile + "')");
            Document document = new Image(result, inputfile);
            ObjectHub.getInstance().getArchiver().getDocumentList().add(document);
        } catch (TesseractException e) {
            e.printStackTrace();
        }
    }

    private static String getFirstDate(String documentData) throws Exception{

        if(datePattern == null){
            compileDatePattern();
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

    private static Tesseract getTesseract() {
        Tesseract instance = new Tesseract();
        instance.setDatapath(ObjectHub.getInstance().getProperties().getProperty("tessData"));
        instance.setLanguage(ObjectHub.getInstance().getProperties().getProperty("tessLang="));
        instance.setHocr(true);
        return instance;
    }
}
