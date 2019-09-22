package com.yh.git;

//import com.yh.MethodDiff;
import com.yh.command.DiffAgentMojo2;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.*;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.ProgressMonitor;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.patch.FileHeader;
import org.eclipse.jgit.patch.HunkHeader;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class GitTest {
    private static final String REMOTE_URL = "http://gitlab.yonghuivip.com/QA/yh-qa-auto.git";

    public static void main(String[] args) throws IOException, GitAPIException {
        // prepare a new folder for the cloned repository
        File localPath = File.createTempFile("TestGitRepository", "");
        if(!localPath.delete()) {
            throw new IOException("Could not delete temporary file " + localPath);
        }

        // then clone
        System.out.println("Cloning from " + REMOTE_URL + " to " + localPath);
        Git result = Git.cloneRepository()
                .setURI(REMOTE_URL)
                .setBranch("refs/heads/develop")
                // .setBranchesToClone(Arrays.asList("refs/heads/release","refs/heads/develop"))
                .setCredentialsProvider(new UsernamePasswordCredentialsProvider("gongjianfei","P@ssword1"))
                .setDirectory(localPath)
                .setProgressMonitor(new SimpleProgressMonitor())
                .call();
            // Note: the call() returns an opened repository already which needs to be closed to avoid file handle leaks!
            System.out.println("Having repository: " + result.getRepository().getDirectory());


            // clean up here to not keep using more and more disk-space for these samples
            // FileUtils.deleteDirectory(localPath);

//        FileRepositoryBuilder repositoryBuilder = new FileRepositoryBuilder();
//        Repository repository = repositoryBuilder.setGitDir(localPath)
//                .readEnvironment() // scan environment GIT_* variables
//                .findGitDir() // scan up the file system tree
//                .setMustExist(true)
//                .build();
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

            // the diff works on TreeIterators, we prepare two for the two branches
            AbstractTreeIterator oldTreeParser = prepareTreeParser(repository, "refs/heads/develop");
            AbstractTreeIterator newTreeParser = prepareTreeParser(repository, "refs/heads/release");

            // then the procelain diff-command returns a list of diff entries
            List<DiffEntry> diffs = result.diff().setOldTree(oldTreeParser).setNewTree(newTreeParser).call();

//            ByteArrayOutputStream out = new ByteArrayOutputStream();
//            DiffFormatter df = new DiffFormatter(out);
//            //设置比较器为忽略空白字符对比（Ignores all whitespace）
//            df.setDiffComparator(RawTextComparator.WS_IGNORE_ALL);
//            df.setRepository(result.getRepository());
//            System.out.println("------------------------------start-----------------------------");
//            //每一个diffEntry都是第个文件版本之间的变动差异
//            for (DiffEntry diffEntry : diff) {
//                if(diffEntry.getChangeType() == DiffEntry.ChangeType.DELETE){
//                    continue;
//                }else{
//                    System.out.println("Entry:"+diffEntry);
//                    //打印文件差异具体内容
//                    df.format(diffEntry);
//                    String diffText = out.toString("UTF-8");
//                    System.out.println(diffText);
//
//                    //获取文件差异位置，从而统计差异的行数，如增加行数，减少行数
//                    FileHeader fileHeader = df.toFileHeader(diffEntry);
//                    List<HunkHeader> hunks = (List<HunkHeader>) fileHeader.getHunks();
//                    int addSize = 0;
//                    int subSize = 0;
//                    for (HunkHeader hunkHeader : hunks) {
//                        EditList editList = hunkHeader.toEditList();
//                        for (Edit edit : editList) {
//                            subSize += edit.getEndA() - edit.getBeginA();
//                            addSize += edit.getEndB() - edit.getBeginB();
//
//                        }
//                    }
//                    System.out.println("addSize=" + addSize);
//                    System.out.println("subSize=" + subSize);
//                    System.out.println("------------------------------end-----------------------------");
//                    out.reset();
//                }
//            }


                for (DiffEntry entry : diffs) {
                    System.out.println("Entry: " + entry);
                }

                System.out.println("-----------------");
                List<String> diffClassesPath = new ArrayList<>(10);
                for(DiffEntry diff : diffs){
                    if(diff.getChangeType() == DiffEntry.ChangeType.DELETE) {
                        continue;
                    }else{
                        //System.out.println(diff.getNewPath());
                        diffClassesPath.add((localPath.getAbsolutePath() + File.separator + diff.getNewPath()).replace("\\",File.separator).replace("/",File.separator));
                    }
                }
            System.out.println("--------------diffClassesPath List--------------");
               diffClassesPath.forEach(x -> System.out.println(x));

               // 测试类
           // diffClassesPath.add("C:\\Users//123\\Desktop\\Yh_105.java".replace("\\", File.separator).replace("/",File.separator));

            System.out.println("-------------------------------");

            Set<String > classes = new HashSet<>(diffClassesPath.size());

        diffClassesPath.forEach(path ->{
            try {
                String  content = org.apache.commons.io.FileUtils.readFileToString(new File(path), StandardCharsets.UTF_8);

                Pattern p = Pattern.compile("class\\s+([\\w\\d$_]+)s*",Pattern.MULTILINE);
                Matcher m = p.matcher(content);
                while(m.find()){
                    System.out.println("获取类名："+m.group(1));
                    classes.add(m.group(1));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });


        System.out.println("-----------最终结果--------");
            classes.forEach(c -> System.out.println(c));


        }



    private static AbstractTreeIterator prepareTreeParser(Repository repository, String ref) throws IOException {
        // from the commit we can build the tree which allows us to construct the TreeParser
        Ref head = repository.exactRef(ref);
        try (RevWalk walk = new RevWalk(repository)) {
            RevCommit commit = walk.parseCommit(head.getObjectId());
            RevTree tree = walk.parseTree(commit.getTree().getId());

            CanonicalTreeParser treeParser = new CanonicalTreeParser();
            try (ObjectReader reader = repository.newObjectReader()) {
                treeParser.reset(reader, tree.getId());
            }

            walk.dispose();

            return treeParser;
        }
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


