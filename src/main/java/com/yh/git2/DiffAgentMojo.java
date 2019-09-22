package com.yh.git2;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.diff.HistogramDiff;
import org.eclipse.jgit.lib.ProgressMonitor;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class DiffAgentMojo {

    private static final AtomicBoolean DIFF_FILTER_INJECTED = new AtomicBoolean(false);
    private static final String REMOTE_URL = "http://gitlab.yonghuivip.com/QA/yh-qa-auto.git";

    private String oldRev = "refs/heads/release";


    private String newRev ="refs/heads/develop";

    public static void main(String args[]){
        DiffAgentMojo diff = new DiffAgentMojo();
        diff.executeMojo();
    }


    public void executeMojo() {
        try {
            if ((StringUtils.isNotBlank(oldRev) && StringUtils.isNotBlank(newRev))) {
                injectDiffFilter();
            }
        } catch (Exception e) {
           System.out.println("failed to inject diff filter for old rev [" + oldRev + "] and new rev [" + newRev + "]");
        }
       // super.executeMojo();
    }

    private void injectDiffFilter() throws Exception {

        if (DIFF_FILTER_INJECTED.getAndSet(true)) {
            return;
        }



        File localPath = File.createTempFile("TestGitRepository", "");
        if(!localPath.delete()) {
            throw new IOException("Could not delete temporary file " + localPath);
        }

        // then clone
        System.out.println("Cloning from " + REMOTE_URL + " to " + localPath);
        Git result = Git.cloneRepository()
                .setURI(REMOTE_URL)
                .setBranch("refs/heads/develop")
                .setBranchesToClone(Arrays.asList("refs/heads/release","refs/heads/develop"))
                .setCredentialsProvider(new UsernamePasswordCredentialsProvider("gongjianfei","P@ssword1"))
                .setDirectory(localPath)
                .setProgressMonitor(new SimpleProgressMonitor())
                .call();
        // Note: the call() returns an opened repository already which needs to be closed to avoid file handle leaks!
        System.out.println("Having repository: " + result.getRepository().getDirectory());


        Repository repository = result.getRepository();


        if (repository.exactRef("refs/heads/release") == null) {
            // first we need to ensure that the remote branch is visible locally
            Ref ref = result.branchCreate().setName("release").setStartPoint("origin/release").call();

            System.out.println("Created local testbranch with ref: " + ref);
        }

        if (repository.exactRef("refs/heads/develop") == null) {
            // first we need to ensure that the remote branch is visible locally
            Ref ref = result.branchCreate().setName("develop").setStartPoint("origin/develop").call();

            System.out.println("Created local testbranch with ref: " + ref);
        }



        DiffCalculator calculator = DiffCalculator.builder()
                .diffAlgorithm(new HistogramDiff())
                .build();
        List<DiffEntryWrapper> diffEntryList = calculator.calculateDiff(localPath, oldRev, newRev, false)
                .stream()
                .filter(diffEntry -> !diffEntry.isDeleted())
                .collect(Collectors.toList());

        for(DiffEntryWrapper diff : diffEntryList){
            System.out.println("gitdir:"+diff.getGitDir());
            System.out.println("newfile:"+diff.getNewFile());
            System.out.println("newPath: "+diff.getNewPath());
            System.out.println("absolutenewPath:"+diff.getAbsoluteNewPath());
            System.out.println("Entry: " + diff.getDiffEntry());
            List<Edit> editor = diff.getEditList();
            System.out.println("edit:");
            System.out.println(editor);
            System.out.println("---------------------");


        }


//        IFilter diffFilter = new DiffFilter(getProject(), localPath, diffEntryList);
//
//        FilterUtil.appendFilter(diffFilter);
//
//        if (CollectionUtils.isEmpty(diffEntryList)) {
//            return;
//        }

    }




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
