package com.utils;

import com.Main;
import com.misc.OperatingSys;
import com.ObjectHub;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.*;

public class IOUtil {
    private static Logger logger = Main.getLogger();

    public static final String LOCAL_OS = System.getProperty("os.name").startsWith("Linux") ? "Linux" : "Windows";

    public static final String alternativePathToArchive = ObjectHub.getInstance().getProperties().getProperty("alternativePathToArchive");

    public static final String projectFolderOnHost = ObjectHub.getInstance().getProperties().getProperty("projectFolderOnHost");

    public static String convertFilePathOSDependent(String filePath, OperatingSys targetOS){

        if(targetOS == OperatingSys.Linux && !filePath.startsWith("/")){
            filePath = filePath.replace("\\", "/").replace("//", "/");
        }else{
            if(targetOS == OperatingSys.Windows && !filePath.startsWith("\\\\")){
                filePath = filePath.replace("/", "\\");
                while(!filePath.startsWith("\\\\")) {
                    filePath = filePath = "\\" + filePath;
                }
            }
            
        }
        return filePath;
    }

    public static String makePathHostRelative(String originFilePath){
        if(!IOUtil.alternativePathToArchive.equals("")){
            originFilePath = originFilePath.replace(IOUtil.alternativePathToArchive, IOUtil.projectFolderOnHost + "/Archiv");
        }
        return originFilePath;
    }


    public static Collection<File> createFileSetBySize(Collection<File> inputFiles){
        //Method to make sure only absolute different files in size will be processed
        Map<Long, File> fileMap = new HashMap<>();
        inputFiles.forEach(file -> fileMap.putIfAbsent(FileUtils.sizeOf(file), file));
        return fileMap.values();
    }


}

