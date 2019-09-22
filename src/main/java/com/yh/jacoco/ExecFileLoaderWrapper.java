package com.yh.jacoco;

import org.jacoco.core.data.ExecutionDataReader;
import org.jacoco.core.tools.ExecFileLoader;

import java.io.*;
import java.util.List;

public class ExecFileLoaderWrapper {
    private ExecFileLoader execFileLoader;
    private List<String> classes;


    public ExecFileLoaderWrapper(ExecFileLoader execFileLoader, List<String> classes) {
        this.execFileLoader = execFileLoader;
        this.classes = classes;
    }


    /**
     * Reads all data from given input stream.
     *
     * @param stream
     *            Stream to read data from
     * @throws IOException
     *             in case of problems while reading from the stream
     */
    public void load(final InputStream stream) throws IOException {
        final ExecutionDataReader reader = new ExecutionDataReader2(
                new BufferedInputStream(stream),this.classes);
        reader.setExecutionDataVisitor(execFileLoader.getExecutionDataStore());
        reader.setSessionInfoVisitor(execFileLoader.getSessionInfoStore());
        reader.read();
    }

    /**
     * Reads all data from given input stream.
     *
     * @param file
     *            file to read data from
     * @throws IOException
     *             in case of problems while reading from the stream
     */
    public void load(final File file) throws IOException {
        final InputStream stream = new FileInputStream(file);
        try {
            load(stream);
        } finally {
            stream.close();
        }
    }


    public ExecFileLoader getExecFileLoader() {
        return execFileLoader;
    }

    public void setExecFileLoader(ExecFileLoader execFileLoader) {
        this.execFileLoader = execFileLoader;
    }

    public List<String> getClasses() {
        return classes;
    }

    public void setClasses(List<String> classes) {
        this.classes = classes;
    }
}
