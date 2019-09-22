package com.yh.apidiff;

import apidiff.APIDiff;
import apidiff.Change;
import apidiff.Result;
import apidiff.enums.Classifier;

import java.io.File;
import java.io.IOException;

public class ApiDiffTest {

    public static void main(String args[]) throws Exception {
     //   File localPath = File.createTempFile("TestGitRepository", "");
//        if(!localPath.delete()) {
//            throw new IOException("Could not delete temporary file " + localPath);
//        }

        APIDiff diff = new APIDiff("ims-tools", "http://gitlab.yonghuivip.com/QA/Ims-tools.git");
        //System.out.println(localPath.getAbsolutePath());
        diff.setPath("/var/folders/0y/cdj1dj993c12v7zw5py_3gfc0000gp/T/TestGitRepository7082037540473725481");

        Result result = diff.detectChangeAllHistory("master", Classifier.NON_API);
        System.out.println("输出结果：");
        for(Change changeMethod : result.getChangeMethod()){
            System.out.println("\n" + changeMethod.getCategory().getDisplayName() + " - " + changeMethod.getDescription());
        }
    }

}
