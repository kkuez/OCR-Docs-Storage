package com.Utils;

import com.ObjectHub;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class IOUtil {

    public static final String OS = System.getProperty("os.name").startsWith("Linux") ? "Linux" : "Windows";

    public static final String alternativePathToArchive = ObjectHub.getInstance().getProperties().getProperty("alternativePathToArchive");

    public static final String projectFolderOnHost = ObjectHub.getInstance().getProperties().getProperty("projectFolderOnHost");

    public static String convertFilePathOSDependent(String originFilePath){
        if(!IOUtil.OS.equals("Linux")){
            return originFilePath.replace("\\", "/").replace("//", "/");
        }
        return originFilePath;
    }

    public static String makePathHostRelative(String originFilePath){
        if(!IOUtil.alternativePathToArchive.equals("")){
            originFilePath = originFilePath.replace(IOUtil.alternativePathToArchive, IOUtil.projectFolderOnHost + "/Archiv");
        }
        return originFilePath;
    }

    public static File createFileOrNull(String content, String absolutePathAndName){
        File result = new File(absolutePathAndName);
        try {
            if(result.exists()){
                FileUtils.writeStringToFile(result, content, "UTF-8");
            }
        } catch (IOException e) {
            LogUtil.logError(result.getAbsolutePath(), e);
        }
        return result.exists() ? result : null;
    }

    public static Collection<File> createFileSetBySize(Collection<File> inputFiles){
        //Method to make sure only absolute different files in size will be processed
        Map<Long, File> fileMap = new HashMap<>();
        inputFiles.forEach(file -> fileMap.putIfAbsent(FileUtils.sizeOf(file), file));
        return fileMap.values();
    }



}
