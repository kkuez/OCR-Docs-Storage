package com.utils;

import com.backend.OperatingSys;

/**
 * Util responsible for File IO
 * */

public class IOUtil {
   private IOUtil() {}

   public static String convertFilePathOSDependent(String filePath, OperatingSys targetOS){

        if(targetOS == OperatingSys.Linux && !filePath.startsWith("/")){
            filePath = filePath.replace("\\", "/").replace("//", "/");
        }else{
            if(targetOS == OperatingSys.Windows && !filePath.startsWith("\\\\")){
                filePath = filePath.replace("/", "\\");
                while(!filePath.startsWith("\\\\")) {
                    filePath = "\\" + filePath;
                }
            }
            
        }
        return filePath;
    }
}

