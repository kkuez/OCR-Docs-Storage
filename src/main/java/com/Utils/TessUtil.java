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
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class TessUtil {

    public static void processFolder(File folder) {
        Collection<File> filesInFolder = FileUtils.listFiles(folder,
                new String[] { "png", "PNG", "jpg", "JPG", "jpeg", "JPEG" }, true);
        filesInFolder.forEach(file -> processFile(file));
    }

    public static void processFolder(File folder, TableView tableView, TableColumn[] tableColumns,
            PropertyValueFactory[] propertyValueFactories) {
        Collection<File> filesInFolder = FileUtils.listFiles(folder,
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

            DBUtil.executeSQL("insert into Documents (id, content, originalFile) Values (1, '"
                    + result.replaceAll("'", "''") + "', '" + inputfile.getAbsolutePath() + "')");
            Document document = new Image(result, inputfile);
            ObjectHub.getInstance().getArchiver().getDocumentList().add(document);
        } catch (TesseractException e) {
            e.printStackTrace();
        }
    }

    private static Tesseract getTesseract() {
        Tesseract instance = new Tesseract();
        instance.setDatapath("C:\\Program Files\\Tesseract-OCR\\tessdata");
        instance.setLanguage("deu");
        instance.setHocr(true);
        return instance;
    }
}
