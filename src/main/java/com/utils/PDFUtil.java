package com.utils;

import com.backend.BackendFacade;
import com.data.User;
import org.apache.commons.io.FileUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class PDFUtil {

    private static Logger logger = LoggerFactory.getLogger(PDFUtil.class);

    public static File createPDF(BackendFacade facade, LocalDate beginDate, LocalDate endDate) {
        // ChooseTimeReporter chooseTimeReporter = (beginDate, endDate) -> {
        File resultPdf = null;
        // TODO ganze methode nochmal sauber machen
        try (PDDocument document = new PDDocument()) {
            PDPage firstPage = new PDPage();
            document.addPage(firstPage);
            PDPageContentStream pdPageContentStream = preparePageStream(document, firstPage);
            pdPageContentStream.showText("Zusammenfassung " + beginDate.toString() + " - " + endDate.toString());
            List<LocalDate> relatedMonth = new LinkedList<>();
            relatedMonth.add(beginDate);
            int index = -1;
            LocalDate nextLocalDate = beginDate.withDayOfMonth(1);
            Map<User, Float> userSumMap = new HashMap<>();
            facade.getAllowedUsers().values().forEach(user -> userSumMap.put(user, 0f));
            pdPageContentStream.newLine();
            float sumOfAll = 0f;
            do {
                if (index % 8 == 0) {
                    PDPage nextPage = new PDPage();
                    document.addPage(nextPage);
                    pdPageContentStream.endText();
                    pdPageContentStream.close();
                    pdPageContentStream = preparePageStream(document, nextPage);
                }
                index++;
                nextLocalDate = beginDate.plusMonths(index).withDayOfMonth(TimeUtil.getdaysOfMonthCount(
                        beginDate.plusMonths(index).getYear(), beginDate.plusMonths(index).getMonth().getValue()));
                pdPageContentStream.newLine();
                pdPageContentStream.newLine();
                pdPageContentStream.setFont(PDType1Font.COURIER_BOLD, 16);
                pdPageContentStream.showText(nextLocalDate.toString());
                pdPageContentStream.setFont(PDType1Font.COURIER, 16);
                float sumForMonth = 0f;
                for (Map.Entry<User, Float> entry : userSumMap.entrySet()) {

                    pdPageContentStream.newLine();
                    float sumForUser = facade.getSumMonth(nextLocalDate, entry.getKey());
                    sumForMonth += sumForUser;
                    sumOfAll += sumForUser;
                    userSumMap.put(entry.getKey(), entry.getValue() + sumForUser);
                    pdPageContentStream.showText(entry.getKey().getName() + ": " + sumForUser);
                }

                pdPageContentStream.newLine();
                pdPageContentStream.showText("Gesamt: " + sumForMonth);
            } while (!nextLocalDate.withDayOfMonth(1).toString().equals(endDate.withDayOfMonth(1).toString()));

            pdPageContentStream.newLine();
            pdPageContentStream.newLine();
            pdPageContentStream.showText("Alles in allem: " + sumOfAll);
            pdPageContentStream.newLine();
            for (User user : userSumMap.keySet()) {
                pdPageContentStream.showText(user.getName() + ": " + userSumMap.get(user));
                pdPageContentStream.newLine();
            }
            pdPageContentStream.endText();
            pdPageContentStream.close();
            File tempDir = FileUtils.getTempDirectory();
            resultPdf = new File(tempDir,
                    beginDate.toString().replace('\'', '_') + " - " + endDate.toString().replace('\'', '_') + ".pdf");
            document.save(resultPdf);
            // Desktop.getDesktop().open(resultPdf);
        } catch (IOException e) {
            logger.error("Failed activating bot", e);
        }
        // };

        // ControllerStrategy pdfControllerStrategy = new ChooseTimeStrategy(chooseTimeReporter);
        // ControllerUtil.createNewWindow(pdfControllerStrategy);
        return resultPdf;
    }

    private static PDPageContentStream preparePageStream(PDDocument document, PDPage page) throws IOException {
        final PDPageContentStream pdPageContentStream = new PDPageContentStream(document, page);
        pdPageContentStream.beginText();
        pdPageContentStream.setFont(PDType1Font.COURIER, 16);
        pdPageContentStream.setLeading(14.5f);
        pdPageContentStream.newLineAtOffset(25, 725);
        return pdPageContentStream;
    }
}
