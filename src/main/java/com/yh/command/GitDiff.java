package com.yh.command;

import org.apache.commons.cli.*;
import org.apache.commons.lang.StringUtils;

public class GitDiff {
    public static final char UNDERLINE = '-';
    public static void main(String[] args) {
        GitDiffInfo info =   GitDiffInfo.builder().baseRef("master").newRef("gongjianfei").gitUrl("http://gitlab.yonghuivip.com/QA/Ims-tools").name("gongjianfei").password("P@ssword1").build();
        DiffAgentMojo2 agent = DiffAgentMojo2.builder().diff(info).build();
                try {
                    System.out.println("获取所有改动的类名:"+StringUtils.join(agent.executeMojo(),":"));
                } catch (Exception e) {
                    e.printStackTrace();
                }

//        GitDiffInfo info = new GitDiff().parseOptions(args);
//        if(info!=null){
//                DiffAgentMojo2 agent = DiffAgentMojo2.builder().diff(info).build();
//                try {
//                    System.out.println("获取所有改动的类名:"+StringUtils.join(agent.executeMojo(),":"));
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//        }
// else{
//     //http://gitlab.yonghuivip.com/QA/Ims-tools
//          //  info = GitDiffInfo.builder().baseRef("release").newRef("develop").gitUrl("http://gitlab.yonghuivip.com/QA/yh-qa-auto.git").name("gongjianfei").password("P@ssword1").build();
//            info = GitDiffInfo.builder().baseRef("master").newRef("gongjianfei").gitUrl("http://gitlab.yonghuivip.com/QA/Ims-tools").name("gongjianfei").password("P@ssword1").build();
//
//        }



    }

    //java [-options] class [args...]
    private GitDiffInfo parseOptions(String[] args) {
        Options options = new Options();

        //第一个参数是选项名称的缩写，第二个参数是选项名称的全称，第三个参数表示是否需要额外的输入，第四个参数表示对选项的描述信息
        Option opt_help = new Option("h", "help", false, "print help message");
        opt_help.setRequired(false);
        options.addOption(opt_help);

        Option opt_version = new Option("v", "version", false, "print version");
        opt_version.setRequired(false);
        options.addOption(opt_version);

        Option optBaseRev = new Option("b", "baseRef", true, "gif分支比较的基础分支， 如， master、release");
        optBaseRev.setArgName("baseRef");
        options.addOption(optBaseRev);

        Option optNewRev = new Option("n", "newRef", true, "gif分支比较的新分支，如，develop");
        optNewRev.setArgName("newRef");
        options.addOption(optNewRev);

        Option optGitUrl = new Option("u", "gitUrl", true, "仓库地址链接");
        optGitUrl.setArgName("gitUrl");
        options.addOption(optGitUrl);

        Option optName = new Option("name", "name", true, "登录git仓库账号");
        optName.setArgName("name");
        options.addOption(optName);

        Option optPassword = new Option("p", "password", true, "登录git仓库密码");
        optPassword.setArgName("password");
        options.addOption(optPassword);

        //用来打印帮助信息
        HelpFormatter hf = new HelpFormatter();
        hf.setWidth(110);

        CommandLineParser parser = new DefaultParser();

        try {
            CommandLine commandLine = parser.parse(options, args);

            if (commandLine.hasOption("h")) {
                hf.printHelp(camelToUnderline(this.getClass().getSimpleName()), options, true);
                return null;
            }else if (commandLine.hasOption("v")) {
                System.out.println("version 0.0.1");
                return null;
            } else {
                GitDiffInfo.GitDiffInfoBuilder builder = GitDiffInfo.builder();
                if (commandLine.hasOption("b") && commandLine.hasOption("n") && commandLine.hasOption("u") && commandLine.hasOption("name") && commandLine.hasOption("p")) {
                    builder.baseRef(commandLine.getOptionValue("b"))
                            .newRef(commandLine.getOptionValue("n"))
                            .gitUrl(commandLine.getOptionValue("u"))
                            .name(commandLine.getOptionValue("name"))
                            .password(commandLine.getOptionValue("p"));
                    return builder.build();
                }else{
                    throw new RuntimeException("b、n、u、name、p等命令必须一起传, 如需帮助请使用-h查看帮助");
                }


            }

        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 驼峰格式字符串转换为下划线格式字符串
     *
     * @param param
     * @return
     */
    public static String camelToUnderline(String param) {
        if (param == null || "".equals(param.trim())) {
            return "";
        }
        int len = param.length();
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            char c = param.charAt(i);
            if (Character.isUpperCase(c)) {
                if(i!=0) {
                    sb.append(UNDERLINE);
                }
                sb.append(Character.toLowerCase(c));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}
