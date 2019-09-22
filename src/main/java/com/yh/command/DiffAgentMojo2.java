package com.yh.command;

import com.yh.command.constants.Constants;
import com.yh.git2.DiffCalculator;
import com.yh.git2.DiffEntryWrapper;
import lombok.Builder;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jgit.api.CreateBranchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.HistogramDiff;
import org.eclipse.jgit.lib.ProgressMonitor;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Builder
public class DiffAgentMojo2 {

   // private static final AtomicBoolean DIFF_FILTER_INJECTED = new AtomicBoolean(false);
    private GitDiffInfo diff;
    private File localPath;


    public Set<String> executeMojo() throws Exception {
        String oldRev = Constants.GIT_REF_PRE + diff.getBaseRef();
        String newRev = Constants.GIT_REF_PRE+ diff.getNewRef();

        localPath = File.createTempFile(Constants.TEMP_REPOSITORY_FILE, "");
        if(!localPath.delete()) {
            throw new IOException("Could not delete temporary file " + localPath);
        }

        // then clone
        System.out.println("Cloning from " + diff.getGitUrl() + " to " + localPath);

        try {
            if ((StringUtils.isNotBlank(oldRev) && StringUtils.isNotBlank(newRev))) {
                List<DiffEntryWrapper>  diffEntryWrappers = injectDiffFilter(oldRev,newRev);
                if(null != diffEntryWrappers || diffEntryWrappers.isEmpty()){
                    return getDiffClasses(diffEntryWrappers);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
           System.out.println("failed to inject diff filter for old rev [" + oldRev + "] and new rev [" + newRev + "]");
        }
        return null;
    }



    public List<DiffEntryWrapper> injectDiffFilter(String oldRev, String newRev) throws Exception {

        Git result = Git.cloneRepository()
                .setURI(diff.getGitUrl())
                .setBranch(newRev)
               // .setBranchesToClone(Arrays.asList(oldRev,newRev))
                .setCredentialsProvider(new UsernamePasswordCredentialsProvider(diff.getName(),diff.getPassword()))
                .setDirectory(localPath)
                .setProgressMonitor(new SimpleProgressMonitor())
                .call();
        // Note: the call() returns an opened repository already which needs to be closed to avoid file handle leaks!
        System.out.println("Having repository: " + result.getRepository().getDirectory());

        Repository repository = result.getRepository();


        if (repository.exactRef(newRev) == null) {
            // first we need to ensure that the remote branch is visible locally
            Ref ref = result.branchCreate().setName(diff.getNewRef()).setStartPoint(Constants.ORIGIN+diff.getNewRef())
                    .setUpstreamMode(CreateBranchCommand.SetupUpstreamMode.SET_UPSTREAM).call();

            System.out.println("Created local testbranch with ref: " + ref);
        }

        if (repository.exactRef(oldRev) == null) {
            // first we need to ensure that the remote branch is visible locally
            Ref ref = result.branchCreate().setName(diff.getBaseRef()).setStartPoint(Constants.ORIGIN+diff.getBaseRef()).call();

            System.out.println("Created local testbranch with ref: " + ref);
        }



        DiffCalculator calculator = DiffCalculator.builder()
                .diffAlgorithm(new HistogramDiff())
                .build();
        List<DiffEntryWrapper> diffEntryList = calculator.calculateDiff(localPath, oldRev, newRev, false)
                .stream()
                .filter(diffEntry -> !diffEntry.isDeleted())
                .collect(Collectors.toList());

        return diffEntryList;

//        for(DiffEntryWrapper diff : diffEntryList){
//            System.out.println("gitdir:"+diff.getGitDir());
//            System.out.println("newfile:"+diff.getNewFile());
//            System.out.println("newPath: "+diff.getNewPath());
//            System.out.println("absolutenewPath:"+diff.getAbsoluteNewPath());
//            System.out.println("Entry: " + diff.getDiffEntry());
//            List<Edit> editor = diff.getEditList();
//            System.out.println("edit:");
//            System.out.println(editor);
//            System.out.println("---------------------");
//
//
//        }


//        IFilter diffFilter = new DiffFilter(getProject(), localPath, diffEntryList);
//
//        FilterUtil.appendFilter(diffFilter);
//
//        if (CollectionUtils.isEmpty(diffEntryList)) {
//            return;
//        }

    }


    public Set<String > getDiffClasses(List<DiffEntryWrapper> diffEntryWrappers) throws IOException {
        List<String> diffClassesPath = new ArrayList<>(10);
        Set<String > classes = new HashSet<>(diffClassesPath.size());
        for(DiffEntryWrapper diffEntryWrapper : diffEntryWrappers){
            DiffEntry diff = diffEntryWrapper.getDiffEntry();
            if(diff.getChangeType() == DiffEntry.ChangeType.DELETE) {
                continue;
            }else{
                String newPath = File.separator + diff.getNewPath().replace("\\",File.separator).replace("/",File.separator);
                String filePath = localPath.getAbsolutePath() + File.separator + newPath;
                System.out.println("获取有改动的文件：" + newPath);
               String packageName = "";
               if(newPath.contains(Constants.SRC_JAVA)){
                    packageName =  newPath.substring(newPath.indexOf(Constants.SRC_JAVA)+Constants.SRC_JAVA.length(),newPath.lastIndexOf(File.separator)).replace(File.separator,".");
               }else if(newPath.contains(Constants.SRC_TEST)){
                   packageName =  newPath.substring(newPath.indexOf(Constants.SRC_TEST)+Constants.SRC_TEST.length(),newPath.lastIndexOf(File.separator)).replace(File.separator,".");
               }else{
                   continue;
               }

               System.out.println("获取有改动的包名：" + packageName);
                String  content = org.apache.commons.io.FileUtils.readFileToString(new File(filePath), StandardCharsets.UTF_8);
                Pattern p = Pattern.compile("class\\s+([\\w\\d$_]+)s*",Pattern.MULTILINE);
                Matcher m = p.matcher(content);
                while(m.find()){
                    System.out.println("获取类名："+m.group(1));
                    classes.add(packageName + "."+m.group(1));
                }

                //diffClassesPath.add((localPath.getAbsolutePath() + File.separator + diff.getNewPath()).replace("\\",File.separator).replace("/",File.separator));

            }
        }
//        System.out.println("--------------diffClassesPath List--------------");
//        diffClassesPath.forEach(x -> System.out.println(x));
//
//        // 测试类
//        // diffClassesPath.add("C:\\Users//123\\Desktop\\Yh_105.java".replace("\\", File.separator).replace("/",File.separator));
//
//        System.out.println("-------------------------------");
//
//        Set<String > classes = getDiffClassListFromFile(diffClassesPath);
//
//        System.out.println("-----------最终结果--------");
//        classes.forEach(c -> System.out.println(c));
        return classes;

    }

//    public  Set<String >  getDiffClassListFromFile(List<String> diffClassesPath) {
//        Set<String > classes = new HashSet<>(diffClassesPath.size());
//        diffClassesPath.forEach(path ->{
//            try {
//                String  content = org.apache.commons.io.FileUtils.readFileToString(new File(path), StandardCharsets.UTF_8);
//
//
//
//
//                Pattern p = Pattern.compile("class\\s+([\\w\\d$_]+)s*",Pattern.MULTILINE);
//                Matcher m = p.matcher(content);
//                while(m.find()){
//                    System.out.println("获取类名："+m.group(1));
//                    classes.add(m.group(1));
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        });
//        return classes;
//    }


    private static class SimpleProgressMonitor implements ProgressMonitor {
        @Override
        public void start(int totalTasks) {
            System.out.println("Starting work on " + totalTasks + " tasks");
        }

        @Override
        public void beginTask(String title, int totalWork) {
            System.out.println("Start " + title + ": " + totalWork);
        }

        @Override
        public void update(int completed) {
            System.out.print(completed + "-");
        }

        @Override
        public void endTask() {
            System.out.println("Done");
        }

        @Override
        public boolean isCancelled() {
            return false;
        }
    }

}
