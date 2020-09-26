package com.utils;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import com.Main;
import com.backend.BackendFacade;
import com.backend.ObjectHub;
import com.gui.controller.reporter.ProgressReporter;
import com.objectTemplates.Document;
import com.objectTemplates.Image;

import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

public class TessUtil {

    private static Logger logger = Main.getLogger();

    private static Pattern datePattern = Pattern
            .compile("\\s*(3[01]|[12][0-9]|0?[1-9])\\.(1[012]|0?[1-9])\\.((?:19|20)\\d{2})\\s*");

    private static Pattern germanDatePattern = Pattern.compile("\\d\\.\\d\\.\\d\\d\\d\\d");

    private static Pattern numberPattern = Pattern.compile("((\\d\\d|\\d)(,|\\.)\\d\\d)");

    private TessUtil() {
    }

    public static Set<Document> processFolder(TableView tableView, TableColumn[] tableColumns,
            PropertyValueFactory[] propertyValueFactories, ProgressReporter progressReporter, BackendFacade facade) {
        Collection<File> filesInFolder = FileUtils.listFiles(
                new File(ObjectHub.getInstance().getProperties().getProperty("lastInputPath")),
                new String[] { "pdf", "PDF", "png", "PNG", "jpg", "JPG", "jpeg", "JPEG" }, false);
        Collection<File> absoluteDifferentFilesSet = IOUtil.createFileSetBySize(filesInFolder);
        Set<String> filePathSet = facade.getFilePathOfDocsContainedInDB();
        AtomicInteger counterProcessedFiles = new AtomicInteger();

        progressReporter.setTotalSteps(absoluteDifferentFilesSet.size(), null);

        Set<Document> documentSet = new HashSet<>();
        absoluteDifferentFilesSet.forEach(file -> {
            if (!filePathSet.contains(file.getAbsolutePath())) {
                ObjectHub.getInstance().getExecutorService().submit(() -> {
                    if (!facade.isFilePresent(file)) {
                        Document document = processFile(file, 0, null, facade);
                        documentSet.add(document);
                    }
                    progressReporter.addStep(null);
                    counterProcessedFiles.getAndIncrement();
                });
            }
        });
        try {
            ExecutorUtil.blockUntilExecutorIsDone(ObjectHub.getInstance().getExecutorService(), filesInFolder.size());
        } catch (InterruptedException e) {
            logger.error(e);
            Thread.currentThread().interrupt();
            System.exit(2);
        }
        ObservableList<Document> documentObservableList = ControllerUtil
                .createObservableList(ObjectHub.getInstance().getArchiver().getDocumentList());
        ControllerUtil.fillTable(tableView, documentObservableList, tableColumns, propertyValueFactories);
        logger.info(counterProcessedFiles.get() + " Files stored.");
        // FIXME Tags werden nach dem verarbeiten nicht in der tableview angezeigt
        return documentSet;
    }

    public static Document processFile(File inputfile, int userID, Set<String> tagSet, BackendFacade facade) {
        logger.info("Processing " + inputfile.getAbsolutePath());
        Tesseract tesseract = getTesseract();
        Document document = null;
        try {
            String result = tesseract.doOCR(inputfile);
            document = new Image(result, inputfile, facade.getIdForNextDocument(), userID);

            File newOriginalFilePath = new File(ObjectHub.getInstance().getArchiver().getDocumentFolder(),
                    document.getOriginalFileName());
            if (!newOriginalFilePath.exists()) {
                copyFile(document, newOriginalFilePath);
            }
            document.setOriginFile(newOriginalFilePath);

            facade.insertDocument(document);

            if (tagSet != null) {
                for (String tag : tagSet) {
                    facade.insertTag(document.getId(), tag);
                }
                document.setTagSet(tagSet);
            }

            ObjectHub.getInstance().getArchiver().getDocumentList().add(document);
        } catch (TesseractException e) {
            logger.error(null, e);
        }
        return document;
    }

    private static void copyFile(Document document, File newOriginalFilePath) {
        try {
            File originFilePath = document.getOriginFile();
            FileUtils.copyFile(originFilePath, newOriginalFilePath);
        } catch (IOException e) {
            logger.error("File couldnt be copied (" + newOriginalFilePath + " -> " + newOriginalFilePath + ")", e);
        }
    }

    public static boolean checkIfBon(String content) {
        String[] bonTerms = new String[] { "netto", "mwst", "einkauf", "summe", "aldi", "netto", "penny", "rewe",
                "real", "lidl" };
        for (String term : bonTerms) {
            if (content.toLowerCase().contains(term)) {
                return true;
            }
        }
        return false;
    }

    public static String getFirstDate(String documentData) throws Exception {

        String date = getDateWithPatternOrNull(datePattern, documentData);
        if (date == null) {
            date = getDateWithPatternOrNull(germanDatePattern, documentData);
        }
        if (date == null) {
            DateTimeFormatter germanFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
                    .withLocale(Locale.GERMAN);
            date = LocalDate.now().format(germanFormatter);
        }

        return date;
    }

    private static String getDateWithPatternOrNull(Pattern pattern, String string) {
        Matcher matcher = pattern.matcher(string);

        String date = null;
        if (matcher.find()) {
            date = matcher.group();
            // Always take first found date, since presumably its the one in the documents header
        }
        return date;
    }

    public static float getLastNumber(String content) {

        Matcher matcher = numberPattern.matcher(content);
        List<String> numberList = new ArrayList<>();

        while (matcher.find()) {
            numberList.add(matcher.group());
        }
        float lastNumber = 0;
        if (!numberList.isEmpty()) {
            String numberString = numberList.get(numberList.size() - 1);
            try {
                lastNumber = Float.parseFloat(numberString.replace(',', '.'));
            } catch (Exception e) {
                logger.error(numberString.replace(',', '.'), e);
            }
        }
        return lastNumber;
    }

    private static Tesseract getTesseract() {
        Tesseract instance = new Tesseract();
        String datapath = ObjectHub.getInstance().getProperties().getProperty("tessData");
        instance.setHocr(Boolean.parseBoolean(ObjectHub.getInstance().getProperties().getProperty("tessHTML")));
        instance.setDatapath(datapath);
        instance.setLanguage(ObjectHub.getInstance().getProperties().getProperty("tessLang"));
        return instance;
    }
}
