package com.yh;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class GitDiffService {
    private String oldPath;
    private String newPath;


//    private List<AnalyzeRequest> findDiffClasses(IcovRequest request) throws GitAPIException, IOException {
//        String gitAppName = DiffService.extractAppNameFrom(request.getRepoURL());
//        String gitDir = workDirFor(localRepoDir,request) + File.separator + gitAppName;
//        DiffService.cloneBranch(request.getRepoURL(),gitDir,branchName);
//        String masterCommit = DiffService.getCommitId(gitDir);
//        List<DiffEntry> diffs = diffService.diffList(request.getRepoURL(),gitDir,request.getNowCommit(),masterCommit);
//        List<AnalyzeRequest> diffClasses = new ArrayList<>(10);
//        String classPath;
//        for (DiffEntry diff : diffs) {
//            if(diff.getChangeType() == DiffEntry.ChangeType.DELETE){
//                continue;
//            }
//            AnalyzeRequest analyzeRequest = new AnalyzeRequest();
//            if(diff.getChangeType() == DiffEntry.ChangeType.ADD){
//                //todo
//            }else {
//                HashSet<String> changedMethods = MethodDiff.methodDiffInClass(oldPath, newPath);
//                analyzeRequest.setMethodnames(changedMethods);
//            }
//            classPath = gitDir + File.separator + diff.getNewPath().replace("src/main/java","target/classes").replace(".java",".class");
//            analyzeRequest.setClassesPath(classPath);
//            diffClasses.add(analyzeRequest);
//        }
//        return diffClasses;
//    }
}
