package com.atguigu.commons;

import java.io.File;

public class Utils {

    /**
     * add path separator for path which is not endswith File.pathSeparator
     * @param path
     * @return
     */
    public static String getFullPath(String path){
        if(!path.endsWith(File.pathSeparator)){
            path = path + File.pathSeparator;
        }
        return path;
    }

}
