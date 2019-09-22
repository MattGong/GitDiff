package com.yh.git;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MatchClass {

    public static void main(String args[]) throws IOException {
       String filePath =  "C:\\Users//123\\Desktop\\Yh_105.java".replace("\\", File.separator).replace("/",File.separator);

        String  content = org.apache.commons.io.FileUtils.readFileToString(new File(filePath), StandardCharsets.UTF_8);
        System.out.println(content);

        Set<String > classes = new HashSet<>();
        Pattern p = Pattern.compile("class\\s+([\\w\\d$_]+)s*",Pattern.MULTILINE);
        Matcher m = p.matcher(content);
        while(m.find()){
            System.out.println(m.group(1));
        }
//        boolean match = m.find();
//        System.out.println(m.groupCount());
//        if (match && m.groupCount()>=1){
//            for(int i=1;i<=m.groupCount();i++){
//                System.out.println(m.group(i));
//                classes.add(m.group(i));
//            }
//
//        }

    }
}
