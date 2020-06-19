package com.utils;

import com.Main;
import com.backend.OperatingSys;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.*;

/**
 * Util responsible for File IO
 * */

public class IOUtil {
    private static Logger logger = Main.getLogger();

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

    public static Collection<File> createFileSetBySize(Collection<File> inputFiles){
        //Method to make sure only absolute different files in size will be processed
        Map<Long, File> fileMap = new HashMap<>();
        inputFiles.forEach(file -> fileMap.putIfAbsent(FileUtils.sizeOf(file), file));
        return fileMap.values();
    }



}

