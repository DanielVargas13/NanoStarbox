package box.star.util;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

public class Process extends Thread {

    private final Shell shell;
    private java.lang.Process process;

    String[] parameters;
    private int exitCode = -1;

    InputStream inputStream;
    OutputStream outputStream;
    OutputStream errorStream;

    public Process(Shell shell, String... parameters) {
        this(shell, shell.streams, parameters);
    }

    public Process(Shell shell, Map<Integer, Closeable> streams, String... parameters) {
        this.shell = shell;
        inputStream = (InputStream) streams.get(0);
        outputStream = (OutputStream) streams.get(1);
        errorStream = (OutputStream) streams.get(2);
        this.parameters = parameters;
    }

    @Override
    public void run() {

        try{
            process = Runtime.getRuntime().exec(
                    parameters,
                    shell.getCompiledEnvironment(),
                    shell.getCurrentDirectory());
        } catch (Exception e){throw new RuntimeException(e);}

        byte[] buf = new byte[4096]; int n;

        while (process.isAlive()) {
            try {
                if (errorStream != null) if (process.getErrorStream().available() > 0){
                    n = process.getErrorStream().read(buf);
                    errorStream.write(buf, 0, n);
                }
                if (inputStream != null) if (inputStream.available() > 0) {
                    n =  inputStream.read(buf);
                    process.getOutputStream().write(buf, 0, n);
                }
                if (outputStream != null) if (process.getInputStream().available() > 0) {
                    n = process.getInputStream().read(buf);
                    outputStream.write(buf, 0, n);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        exitCode = process.exitValue();
    }

    public int getExitCode() {
        try {
            if (isAlive()) join();
        } catch (InterruptedException e) {e.printStackTrace();}
        return exitCode;
    }

    @Override
    public synchronized void start() {
        shell.jobs.push(this);
        super.start();
    }
}

