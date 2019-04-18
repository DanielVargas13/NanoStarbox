package box.star;

import java.io.*;
import java.util.Hashtable;
import java.util.Map;
import java.util.Stack;

/**
 * Thread based shell implementation.
 */
public class Shell extends Thread {

    public final static int STDIN = 0;
    public final static int STDOUT = 1;
    public final static int STDERR = 2;

    protected void main(String[] parameters) {}
    protected int exitStatus() {return exitCode;}

    private Shell parent;

    private final int shellNumber;
    private int exitCode = 0;

    protected Stack<Shell> children = new Stack<>();

    private String currentDirectory;
    private Map<Integer, Closeable> streamCollection = new Hashtable<>(3);

    protected Hashtable<String, String> environment;

    public final File getFile(String file){
        boolean local = file == null || file.equals("") ||
                file.equals(".") || file.startsWith("./") || file.startsWith(".\\") ||
                (! file.startsWith("/") && ! file.matches("^[a-zA-Z]:"));
        return ((local)?new File(currentDirectory, file):new File(file));
    }

    private void mapAllStreams(Map<Integer, Closeable> data){
        if (data == null) return;
        for(Integer stream:data.keySet()) setStream(stream, data.get(stream));
    }

    public Shell(Map<Integer, Closeable> streamCollection){
        super(Shell.class.getName());
        setStream(STDIN, System.in);
        setStream(STDOUT, System.out);
        setStream(STDERR, System.err);
        this.mapAllStreams(streamCollection);
        this.shellNumber = -1;
        this.environment = new Hashtable<>(System.getenv());
        this.setCurrentDirectory(System.getProperty("user.dir"));
    }

    public Shell(Shell main, Map<Integer, Closeable> streams) {
        super(Shell.class.getName());
        this.parent = main;
        this.mapAllStreams(main.streamCollection); // get base...
        this.mapAllStreams(streams); // get layer...
        this.environment = (Hashtable)main.environment.clone(); // get copy...
        this.currentDirectory = main.currentDirectory;
        synchronized (main.children) {
            this.shellNumber = main.children.size();
            main.children.push(this);
        }
    }

    private String[] parameters = new String[0];

    public final void exec(String... parameters){
        Thread caller = Thread.currentThread();
        if (caller.equals(this)) {
            throw new IllegalThreadStateException("cannot call class method from within shell");
        }
        if (running) {
            throw new IllegalThreadStateException("Shell is already running");
        }
        this.parameters = parameters;
        this.start();
    }

    public final int getExitStatus() {
        Thread caller = Thread.currentThread();
        if (caller.equals(this)) {
            throw new IllegalThreadStateException("cannot call own class method from within shell");
        }
        if (this.isAlive()) {
            try { this.join();
            } catch (InterruptedException e) {}
        }
        return exitCode;
    }

    private boolean running;
    @Override
    public final void run() {
        if (running) {
            throw new IllegalThreadStateException("Shell is already running");
        }
        running = true;
        try {
            main(parameters);
        } catch (Exception e){throw new RuntimeException(e);}
        finally {
            running = false;
            exitCode = exitStatus();
        }
    }

    public final void setCurrentDirectory(String currentDirectory) {
        if (environment.containsKey("PWD")) {
            environment.put("PWD", currentDirectory);
        }
        this.currentDirectory = currentDirectory;
    }

    public final String getCurrentDirectory() { return currentDirectory; }

    public boolean haveStream(int stream) {
        return streamCollection.containsKey(stream);
    }

    public boolean haveStream(int stream, Class<?> type){
        Closeable wanted = getStream(stream);
        return (wanted != null && type.isAssignableFrom(wanted.getClass()));
    }

    public final Closeable getStream(int stream) { return streamCollection.get(stream); }

    public final void setStream(int stream, Closeable source) {
        if (stream == STDERR) {
            if (!(source instanceof OutputStream)) throw new RuntimeException(new IllegalArgumentException("Wrong parameter type for standard error"));
        } else if (stream == STDOUT) {
            if (!(source instanceof OutputStream)) throw new RuntimeException(new IllegalArgumentException("Wrong parameter type for standard output"));
        } else if (stream == STDIN) {
            if (!(source instanceof InputStream)) throw new RuntimeException(new IllegalArgumentException("Wrong parameter type for standard input"));
        }
        streamCollection.put(stream, source);
    }

    public final InputStream getInputStream(int stream){
        Closeable wanted = getStream(stream);
        if (wanted instanceof InputStream) return (InputStream) wanted;
        throw new ClassCastException("shell stream #"+stream+" is not an input stream");
    }

    public final OutputStream getOutputStream(int stream) {
        Closeable wanted = getStream(stream);
        if (wanted instanceof OutputStream) return (OutputStream) wanted;
        throw new ClassCastException("shell stream #"+stream+" is not an output stream");
    }

    public final PrintWriter getPrintWriter(int stream) {
        return new PrintWriter(getOutputStream(stream));
    }

    public final Shell getParent(){ return parent; }

    public final boolean isRootShell(){ return parent == null; }

    public final boolean isSubShell(){ return parent != null; }

    public final int getShellNumber() { return shellNumber; }

    @Override
    public final synchronized void start() {
        if (running) {
            throw new IllegalThreadStateException("Shell is already running");
        }
        super.start();
    }

}
