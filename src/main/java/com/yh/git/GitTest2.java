package com.yh.git;

import apidiff.internal.util.UtilTools;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GitTest2 {
    private static final String REMOTE_URL = "http://gitlab.yonghuivip.com/QA/Ims-tools.git";
    private static final String REMOTE_REFS_PREFIX = "refs/remotes/origin/";
    private static final String  REFS_HEADS_PREXFIX = "refs/heads/";
    private static final String ORIGIN_PREXFIX = "origin/";

    public static void main(String[] args) throws Exception {
        // prepare a new folder for the cloned repository
//        File localPath = File.createTempFile("TestGitRepository", "");
////        if(!localPath.delete()) {
////            throw new IOException("Could not delete temporary file " + localPath);
////        }

        String localPath = "/var/folders/0y/cdj1dj993c12v7zw5py_3gfc0000gp/T/TestGitRepository2567948300592421810";

        // then clone
        System.out.println("Cloning from " + REMOTE_URL + " to " + localPath);
        Git result = new Git(openRepositoryAndCloneIfNotExists(localPath,"ims-tools",REMOTE_URL));
        System.out.println("Having repository: " + result.getRepository().getDirectory());

        Repository repository = result.getRepository();

        Ref oldBranch =  checkoutBranch(repository,"master");
       Ref newBranch =  checkoutBranch(repository,"gongjianfei");


            // the diff works on TreeIterators, we prepare two for the two branches
        AbstractTreeIterator oldTreeParser = prepareTreeParser(repository, oldBranch.getName());
        AbstractTreeIterator newTreeParser = prepareTreeParser(repository, newBranch.getName());


            // then the procelain diff-command returns a list of diff entries
        List<DiffEntry> diffs = result.diff()
                    .setOldTree(oldTreeParser)
                    .setNewTree(newTreeParser)
                    .setShowNameAndStatusOnly(true)
                    .call();

        for (DiffEntry entry : diffs) {
            System.out.println("Entry: " + entry);
        }

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




//                System.out.println("-----------------");
//                List<String> diffClassesPath = new ArrayList<>(10);
//                for(DiffEntry diff : diffs){
//                    if(diff.getChangeType() == DiffEntry.ChangeType.DELETE) {
//                        continue;
//                    }else{
//                        //System.out.println(diff.getNewPath());
//                        diffClassesPath.add((localPath.getAbsolutePath() + File.separator + diff.getNewPath()).replace("\\",File.separator).replace("/",File.separator));
//                    }
//                }
//            System.out.println("--------------diffClassesPath List--------------");
//               diffClassesPath.forEach(x -> System.out.println(x));
//
//               // 测试类
//           // diffClassesPath.add("C:\\Users//123\\Desktop\\Yh_105.java".replace("\\", File.separator).replace("/",File.separator));
//
//            System.out.println("-------------------------------");
//
//            Set<String > classes = new HashSet<>(diffClassesPath.size());
//
//        diffClassesPath.forEach(path ->{
//            try {
//                String  content = org.apache.commons.io.FileUtils.readFileToString(new File(path), StandardCharsets.UTF_8);
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
//
//
//        System.out.println("-----------最终结果--------");
//            classes.forEach(c -> System.out.println(c));


        }


    public static Repository openRepositoryAndCloneIfNotExists(String path, String projectName, String cloneUrl) throws Exception {
        File folder = new File(UtilTools.getPathProject(path , projectName));
        Repository repository = null;

        if (folder.exists()) {
            System.out.println(projectName + " exists. Reading properties ... (wait)");
            RepositoryBuilder builder = new RepositoryBuilder();
            repository = builder
                    .setGitDir(new File(folder, ".git"))
                    .readEnvironment()
                    .findGitDir()
                    .build();

        } else {
            System.out.println("Cloning " + cloneUrl  + " in " + path + " ... (wait)");
            Git git = Git.cloneRepository()
                    .setDirectory(folder)
                    .setURI(cloneUrl)
                    .setCredentialsProvider(new UsernamePasswordCredentialsProvider("gongjianfei","P@ssword1"))
                    .setCloneAllBranches(true)
                    .setProgressMonitor(new SimpleProgressMonitor())
                    .call();
            repository = git.getRepository();
        }
        //System.out.println("Process " + projectName  + " finish.");
        return repository;
    }



    public static Ref checkoutBranch(Repository repository, String branch) throws Exception {
        System.out.println("Checking out "+repository.getDirectory().getParent().toString()+" - "+ REFS_HEADS_PREXFIX + branch);
//        boolean createBranch = !ObjectId.isId(branch);
//        if (createBranch) {
//            Ref ref = repository.exactRef(REFS_HEADS_PREXFIX + branch);
//            if (ref != null) {
//                createBranch = false;
//            }
//        }
        try (Git git = new Git(repository)) {
            if(repository.exactRef(REFS_HEADS_PREXFIX + branch)==null){
                Ref ref = git
                        .checkout().setCreateBranch(true)
                        .setProgressMonitor(new SimpleProgressMonitor())
                        //.branchCreate()
                        .setName(branch)
                        .setStartPoint(ORIGIN_PREXFIX+branch)
                        .call();
                return ref;
            }else{
                Ref ref = git.checkout()
                    .setCreateBranch(false)
                    .setName(branch)
                        .setProgressMonitor(new SimpleProgressMonitor())
                   // .setStartPoint(ORIGIN_PREXFIX+branch)
                    .call();
                return ref;
            }

//            Ref ref = git.checkout()
//                    .setCreateBranch(createBranch)
//                    .setName(branch)
//                    .setStartPoint(ORIGIN_PREXFIX+branch)
//                    .call();

        }
        //return repository.exactRef(REFS_HEADS_PREXFIX + branch);
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


