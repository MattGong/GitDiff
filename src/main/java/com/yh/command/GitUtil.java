package com.yh.command;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class GitUtil {

    /**
     *
     * <p>
     * Description:判断本地分支名是否存在
     * </p>
     *
     * @param git
     * @param branchName
     * @return
     * @throws GitAPIException
     * @author matt
     * @date 2019年7月21日 下午2:49:46
     *
     */
    public static boolean branchNameExist(Git git, String branchName) throws GitAPIException {
        List<Ref> refs = git.branchList().call();
        for (Ref ref : refs) {
            if (ref.getName().contains(branchName)) {
                return true;
            }
        }
        return false;
    }

    /**
     *
     * <p>Description:切换分支，并拉取到最新 </p>
     * @param repoDir
     * @param branchName
     * @author matt
     * @date  2019年7月21日 下午4:11:45
     *
     */
    public void checkoutAndPull(String repoDir, String branchName) {
        try {
            Repository existingRepo = new FileRepositoryBuilder().setGitDir(new File(repoDir)).build();
            Git git = new Git(existingRepo);
            try {
                if (this.branchNameExist(git, branchName)) {//如果分支在本地已存在，直接checkout即可。
                    git.checkout().setCreateBranch(false).setName(branchName).call();
                } else {//如果分支在本地不存在，需要创建这个分支，并追踪到远程分支上面。
                    git.checkout().setCreateBranch(true).setName(branchName).setStartPoint("origin/" + branchName).call();
                }
                git.pull().call();//拉取最新的提交
            } finally {
                git.close();
            }
        } catch (IOException | GitAPIException e) {
            e.printStackTrace();
        }
    }

    public void checkoutAndPull(Repository repository, String branchName){

    }


}
