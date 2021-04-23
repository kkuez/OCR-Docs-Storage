package com.utils;

import com.StartUp;
import com.backend.BackendFacade;
import com.backend.ObjectHub;
import com.objectTemplates.Document;
import com.objectTemplates.Image;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TessUtil {

    private static Logger logger = StartUp.getLogger();

    private static Pattern datePattern = Pattern
            .compile("\\s*(3[01]|[12][0-9]|0?[1-9])\\.(1[012]|0?[1-9])\\.((?:19|20)\\d{2})\\s*");

    private static Pattern germanDatePattern = Pattern.compile("\\d\\.\\d\\.\\d\\d\\d\\d");

    private static Pattern numberPattern = Pattern.compile("((\\d\\d|\\d)(,|\\.)\\d\\d)");

    private TessUtil() {
    }

    public static Document processFile(File inputfile, String userName, Set<String> tagSet, BackendFacade facade,
                                       ObjectHub objectHub) {
        logger.info("Processing " + inputfile.getAbsolutePath());
        Tesseract tesseract = getTesseract(objectHub);
        Document document = null;
        try {
            String result = tesseract.doOCR(inputfile);
            document = new Image(result, inputfile, facade.getIdForNextDocument(), userName);

            File newOriginalFilePath = new File(objectHub.getArchiver().getDocumentFolder(),
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

            objectHub.getArchiver().getDocumentList().add(document);
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

    private static Tesseract getTesseract(ObjectHub objectHub) {
        Tesseract instance = new Tesseract();
        String datapath = objectHub.getProperties().getProperty("tessData");
        instance.setHocr(Boolean.parseBoolean(objectHub.getProperties().getProperty("tessHTML")));
        instance.setDatapath(datapath);
        instance.setLanguage(objectHub.getProperties().getProperty("tessLang"));
        return instance;
    }
}
