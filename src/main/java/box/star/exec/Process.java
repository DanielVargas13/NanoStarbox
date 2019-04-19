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

        byte[] buf = new byte[4096]; int n;

        while (process.isAlive()) {
            try {
                if (inputStream != null && inputStream.available() > 0) {
                    n = inputStream.read(buf);
                    process.getOutputStream().write(buf, 0, n);
                    process.getOutputStream().flush();
                }
                if (errorStream != null && process.getErrorStream().available() > 0) {
                    n = process.getErrorStream().read(buf);
                    errorStream.write(buf, 0, n);
                    errorStream.flush();
                }
                if (outputStream != null && process.getInputStream().available() > 0)  {
                    n = process.getInputStream().read(buf);
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
