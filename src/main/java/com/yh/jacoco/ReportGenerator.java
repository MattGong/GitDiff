package com.yh.jacoco;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jacoco.core.analysis.*;
import org.jacoco.core.tools.ExecFileLoader;
import org.jacoco.report.DirectorySourceFileLocator;
import org.jacoco.report.FileMultiReportOutput;
import org.jacoco.report.IReportVisitor;
import org.jacoco.report.csv.CSVFormatter;
import org.jacoco.report.html.HTMLFormatter;

public class ReportGenerator {

    private final String title;

    private final File executionDataFile;
    private final File classesDirectory;
    private final File sourceDirectory;
    private final File reportDirectory;

    private ExecFileLoader execFileLoader;
    private List<String> classes;
    public ReportGenerator(final File projectDirectory ) {
        this.title = projectDirectory.getName();
        this.executionDataFile = new File(projectDirectory, "target/jacocotest.exec");//覆盖率的exec文件地址
        this.classesDirectory = new File(projectDirectory, "target/classes");//目录下必须包含源码编译过的class文件,用来统计覆盖率。所以这里用server打出的jar包地址即可
//      this.sourceDirectory =null;
        this.sourceDirectory = new File(projectDirectory, "src/main/java");  //源码的/src/main/java,只有写了源码地址覆盖率报告才能打开到代码层。使用jar只有数据结果
        this.reportDirectory = new File(projectDirectory, "target/coveragereport");//要保存报告的地址
        //this.classes = Arrays.asList("com/ims/test/controller/test/JacocoTestController","com/ims/test/controller/article/document/ArticleDocumentController");
        System.out.println(sourceDirectory.getAbsolutePath());
    }


    public void create() throws IOException {
        loadExecutionData();

        final IBundleCoverage bundleCoverage = analyzeStructure();

        printInfo(bundleCoverage);
        createReport(bundleCoverage);

    }

    private void printInfo( IBundleCoverage bundleCoverage){
        System.out.println(bundleCoverage.getName());
        ICounter instructionCounter = bundleCoverage.getInstructionCounter();
        System.out.println("Total Missed Instructions: " + instructionCounter.getMissedCount() + " of " + instructionCounter.getTotalCount() + " " + instructionCounter.getMissedRatio());
        ICounter branchesCounter = bundleCoverage.getBranchCounter();
        System.out.println("Total Missed Branches: " + branchesCounter.getMissedCount() + " of "+ branchesCounter.getTotalCount() + " " + branchesCounter.getMissedRatio());
        ICounter complexityCounter = bundleCoverage.getComplexityCounter();
        System.out.println("Total Missed Cxty: " + complexityCounter.getMissedCount() + " "+ complexityCounter.getTotalCount());
        ICounter lineCounter = bundleCoverage.getLineCounter();
        System.out.println("Total Missed Lines: " + lineCounter.getMissedCount() + " " + lineCounter.getTotalCount());
        ICounter methodsCounter = bundleCoverage.getMethodCounter();
        System.out.println("Total Missed Lines: " + methodsCounter.getMissedCount() + " " + methodsCounter.getTotalCount());
        ICounter classesCounter = bundleCoverage.getClassCounter();
        System.out.println("Total Missed Classed: " + classesCounter.getTotalCount() + " " + classesCounter.getTotalCount());


//        ICounter bCounter=bundleCoverage.getCounter(ICoverageNode.CounterEntity.INSTRUCTION);
//        System.out.println("bundle total:  "+bCounter.getTotalCount());
//        System.out.println("bundle covered:  "+bCounter.getCoveredCount());
        for(IPackageCoverage p : bundleCoverage.getPackages()){
            ICounter pCounter=p.getCounter(ICoverageNode.CounterEntity.INSTRUCTION);
            System.out.println(p.getName());
            System.out.println("package total:  "+pCounter.getTotalCount());
            System.out.println("package covered:  "+pCounter.getCoveredCount());

            for(IClassCoverage c :p.getClasses()){
                ICounter cCounter=c.getCounter(ICoverageNode.CounterEntity.INSTRUCTION);
                System.out.println(c.getName());
                System.out.println("Class total:  "+cCounter.getTotalCount());
                System.out.println("Class covered:  "+cCounter.getCoveredCount());
                for(IMethodCoverage m :c.getMethods()){
                    System.out.println(m.getName());
                    ICounter mCounter=m.getCounter(ICoverageNode.CounterEntity.INSTRUCTION);
                    System.out.println("Method total:  "+mCounter.getTotalCount());
                    System.out.println("Method covered:  "+mCounter.getCoveredCount());
                }

            }
        }

    }

    private void createReportForCSV(IBundleCoverage bundleCoverage) throws Exception {
        List<IReportVisitor> visitors = new ArrayList<IReportVisitor>();

        final CSVFormatter csvFormatter = new CSVFormatter();
        csvFormatter.setOutputEncoding("utf-8");
        IReportVisitor visitor = csvFormatter.createVisitor(new FileOutputStream(new File(reportDirectory, "jacoco.csv")));

        // Initialize the report with all of the execution and session
        // information. At this point the report doesn't know about the
        // structure of the report being created
        visitor.visitInfo(execFileLoader.getSessionInfoStore().getInfos(),
                execFileLoader.getExecutionDataStore().getContents());

        // Populate the report structure with the bundle coverage information.
        // sCall visitGroup if you need groups in your report.
        visitor.visitBundle(bundleCoverage, new DirectorySourceFileLocator(
                sourceDirectory, "utf-8", 4));

        // Signal end of structure information to allow report to write all
        // information out
        visitor.visitEnd();
    }

    private void createReport(final IBundleCoverage bundleCoverage)
            throws IOException {
        System.out.println("开始生成");
        final HTMLFormatter htmlFormatter = new HTMLFormatter();
        final IReportVisitor visitor = htmlFormatter
                .createVisitor(new FileMultiReportOutput(reportDirectory));

        visitor.visitInfo(execFileLoader.getSessionInfoStore().getInfos(),
                execFileLoader.getExecutionDataStore().getContents());
        visitor.visitBundle(bundleCoverage, new DirectorySourceFileLocator(
                sourceDirectory, "utf-8", 4));
        visitor.visitEnd();
        System.out.println("生成完成");
    }

    private void loadExecutionData() throws IOException {
        execFileLoader = new ExecFileLoader();
        if(classes!=null && !classes.isEmpty()) {
            ExecFileLoaderWrapper execFileLoaderWrapper = new ExecFileLoaderWrapper(execFileLoader, classes);
            execFileLoaderWrapper.load(executionDataFile);
        }else{
            execFileLoader.load(executionDataFile);
        }
    }

    private IBundleCoverage analyzeStructure() throws IOException {
        final CoverageBuilder coverageBuilder = new CoverageBuilder();
        //
        final Analyzer analyzer = new Analyzer(
                execFileLoader.getExecutionDataStore(), coverageBuilder);

        if(classes!=null&&!classes.isEmpty()){

            for(String cls : classes){
                    File clsFile = new File(classesDirectory,cls+".class");
                    analyzer.analyzeAll(clsFile);
                }
        }else{
           analyzer.analyzeAll(classesDirectory);
        }

        return coverageBuilder.getBundle(title);
    }


    public static void main(final String[] args) throws IOException {
        ReportGenerator generator = new ReportGenerator(new File("C:\\Users\\123\\Documents\\git\\Ims-tools"));//传递工程目录
        generator.create();
    }
}