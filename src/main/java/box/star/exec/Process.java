package box.star.exec;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.*;

public class Process extends Thread {

    private final Shell shell;
    private java.lang.Process process;

    String[] parameters;
    private int exitCode = -1;

    public final Streams io;

    public Process(Shell shell, Streams streams) {
        super(shell,"Process");
        this.shell = shell;
        this.io = new Streams(shell);
        if (streams != null) io.map(streams);
        this.parameters = parameters;
    }

    @Override
    public void run() {

        try{
            process = Runtime.getRuntime().exec(
                    parameters,
                    shell.environment.compile(),
                    shell.getCurrentDirectory());
        } catch (Exception e){throw new RuntimeException(e);}

        InputStream inputStream = io.getInputStream(0);
        OutputStream outputStream = io.getOutputStream(1);
        OutputStream errorStream = io.getOutputStream(2);

        InputStream sourceError = process.getErrorStream();
        InputStream sourceOutput = process.getInputStream();
        OutputStream sourceInput = process.getOutputStream();

        byte[] buf = new byte[4096]; int n;

        while (process.isAlive()) {
            try {
                if (inputStream != null && inputStream.available() > 0) {
                    n = inputStream.read(buf);
                    sourceInput.write(buf, 0, n);
                    sourceInput.flush();
                }
                if (errorStream != null && sourceError.available() > 0) {
                    n = sourceError.read(buf);
                    errorStream.write(buf, 0, n);
                    errorStream.flush();
                }
                if (outputStream != null && sourceOutput.available() > 0)  {
                    n = sourceOutput.read(buf);
                    outputStream.write(buf, 0, n);
                    outputStream.flush();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        exitCode = process.exitValue();
    }

    public synchronized void start(String... parameters){
        this.parameters = parameters;
        super.start();
    }

    @Override
    public synchronized void start() {
        throw new NotImplementedException();
    }

    public int getExitCode() {
        try {
            if (isAlive()) join();
        } catch (InterruptedException e) {e.printStackTrace();}
        return exitCode;
    }

}
