package box.star;

import java.io.*;
import java.util.Hashtable;
import java.util.Map;
import java.util.Stack;

public class Shell extends Thread {

    public interface IExecutiveFactory {
        IExecutive getMainShell();
        IExecutive getSubShell(IExecutive mainShell);
    }

    public interface IExecutive {
        void main(String[] parameters);
        int exitStatus();
    }

    private Shell parent; private int shellNumber, exitCode = -1;
    private Stack<Shell> subShells = new Stack<>();

    String currentDirectory;
    private Map<Integer, Closeable> streamCollection = new Hashtable<>(3);

    public Hashtable<String, String> environment;

    private IExecutiveFactory controllerFactory;
    private IExecutive controller;

    private String resolve(String file){
        boolean local = file == null || file.equals("") ||
                file.equals(".") || file.startsWith("./") ||
                (! file.startsWith("/") && ! file.matches("^[a-zA-Z]:"));
        return ((local)?new File(currentDirectory, file):new File(file)).getPath();
    }

    public FileInputStream getFileInputStream(String file){
        try { return new FileInputStream(resolve(file)); }
        catch (FileNotFoundException e) { throw new RuntimeException(e); }
    }

    public FileOutputStream getFileOutputStream(String file) {
        try { return new FileOutputStream(resolve(file)); }
        catch (Exception e) {throw new RuntimeException(e);}
    }

    private void mapAllStreams(Map<Integer, Closeable> data){
        if (data == null) return;
        for(Integer stream:data.keySet()) setStream(stream, data.get(stream));
    }

    public Shell(IExecutiveFactory factory) {
        this(factory, null);
    }

    public Shell(IExecutiveFactory factory, Map<Integer, Closeable> streamCollection){
        super(Shell.class.getName());
        this.controllerFactory = factory;
        this.controller = factory.getMainShell();
        this.environment = new Hashtable<>(System.getenv());
        setStream(0, System.in);
        setStream(1, System.out);
        setStream(2, System.err);
        this.mapAllStreams(streamCollection);
        this.setCurrentDirectory(System.getProperty("user.dir"));
    }

    private Shell(Shell main, Map<Integer, Closeable> streams) {
        this.parent = main;
        this.controllerFactory = main.controllerFactory;
        this.controller = controllerFactory.getSubShell(main.controller);
        this.currentDirectory = main.currentDirectory;
        this.mapAllStreams(main.streamCollection); // get base...
        this.mapAllStreams(streams); // get layer...
        this.environment = (Hashtable)main.environment.clone(); // get copy...
        synchronized (main.subShells) {
            this.shellNumber = main.subShells.size();
            main.subShells.push(this);
        }
    }

    public Shell createSubShell(Map<Integer, Closeable> streamCollection){
        return new Shell(this, streamCollection);
    }

    private String[] parameters = new String[0];

    public void exec(String... parameters){
        Thread caller = Thread.currentThread();
        if (caller.equals(this)) {
            throw new IllegalThreadStateException("cannot call class method from within shell");
        }
        this.parameters = parameters;
        this.start();
    }

    public int getExitStatus() {
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

    boolean running;
    @Override
    public void run() {
        if (running) {
            throw new IllegalThreadStateException("Shell is already running");
        }
        running = true;
        if (controller == null) return;
        try {
            controller.main(parameters);
        } catch (Exception e){throw new RuntimeException(e);}
        finally {
            running = false;
            exitCode = controller.exitStatus();
        }
    }

    public void setCurrentDirectory(String currentDirectory) {
        if (environment.containsKey("PWD")) {
            environment.put("PWD", currentDirectory);
        }
        this.currentDirectory = currentDirectory;
    }

    public String getCurrentDirectory() {
        return currentDirectory;
    }

    public Closeable getStream(int stream) {
        return streamCollection.get(stream);
    }

    public void setStream(int stream, Closeable source) {
        if (stream == 2) {
            if (!(source instanceof OutputStream)) throw new RuntimeException(new IllegalArgumentException("Wrong parameter type for standard error"));
        } else if (stream == 1) {
            if (!(source instanceof OutputStream)) throw new RuntimeException(new IllegalArgumentException("Wrong parameter type for standard output"));
        } else if (stream == 0) {
            if (!(source instanceof InputStream)) throw new RuntimeException(new IllegalArgumentException("Wrong parameter type for standard input"));
        }
        streamCollection.put(stream, source);
    }

    public BufferedReader getBufferedReader(int stream, int size) {
        return new BufferedReader(new InputStreamReader((InputStream)getStream(stream)), size);
    }

    public BufferedReader getBufferedReader(int stream) {
        return getBufferedReader(stream, 4096);
    }

    public PrintWriter getPrintWriter(int stream) {
        return new PrintWriter((OutputStream)getStream(stream));
    }

    public Shell getParent(){
        return parent;
    }

    public boolean isRootShell(){
        return parent == null;
    }

    public boolean isSubShell(){
        return parent != null;
    }

    public int getShellNumber() {
        return shellNumber;
    }

}
