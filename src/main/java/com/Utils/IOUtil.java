package com.Utils;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

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
}
