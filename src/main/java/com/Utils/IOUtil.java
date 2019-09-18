package com.Utils;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class IOUtil {

    public static File createFileOrNull(String content, String absolutePathAndName){
        File result = new File(absolutePathAndName);
        try {
            if(result.exists()){
                FileUtils.writeStringToFile(result, content, "UTF-8");
            }
        } catch (IOException e) {
            e.printStackTrace();
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
