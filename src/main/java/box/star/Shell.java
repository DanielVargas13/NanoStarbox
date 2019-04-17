package box.star;

import java.io.*;
import java.util.Hashtable;
import java.util.Map;
import java.util.Stack;

public class Shell extends Thread {

    private Shell parent; private int shellNumber, exitCode = -1;
    private Stack<Shell> subshells = new Stack<>();

    String currentDirectory;
    private Map<Integer, Closeable> streamCollection = new Hashtable<>(3);

    public Hashtable<String, String> environment;
    private IShellExecutive controller;

    public interface IShellExecutive {
        void main(String[] parameters);
        int exitStatus();
    }

    private String resolve(String file){
        boolean local = file == null || file.equals("") ||
                file.equals(".") || file.startsWith("./") ||
                (! file.startsWith("/") && ! file.matches("^[a-zA-Z]:/"));
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

    public Shell(Map<Integer, Closeable>streamCollection, IShellExecutive controller){
        super(Shell.class.getName());
        this.controller = controller;
        this.environment = new Hashtable<>(System.getenv());
        setStream(0, System.in);
        setStream(1, System.out);
        setStream(2, System.err);
        this.mapAllStreams(streamCollection);
        this.setCurrentDirectory(System.getProperty("user.dir"));
    }

    private Shell(Shell main, Map<Integer, Closeable> streams, IShellExecutive controller) {
        this.parent = main;
        this.controller = controller;
        this.currentDirectory = main.currentDirectory;
        this.mapAllStreams(main.streamCollection); // get base...
        this.mapAllStreams(streams); // get layer...
        this.environment = (Hashtable)main.environment.clone(); // get copy...
        synchronized (main.subshells) {
            this.shellNumber = main.subshells.size();
            main.subshells.push(this);
        }
    }

    public Shell createSubshell(Map<Integer, Closeable>streamCollection, IShellExecutive controller){
        return new Shell(this, streamCollection, controller);
    }

    private String[] parameters = new String[0];

    public void exec(String... parameters){
        this.parameters = parameters;
        this.start();
    }

    public int getExitCode() {
        if (this.isAlive()) {
            try { this.join();
            } catch (InterruptedException e) {}
        }
        return exitCode;
    }

    @Override
    public void run() {
        if (controller == null) return;
        try {
            controller.main(parameters);
        } catch (Exception e){throw new RuntimeException(e);}
        finally {
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

    public Closeable getStream(int number) {
        return streamCollection.get(number);
    }

    public void setStream(int number, Closeable source) {
        if (number == 2) {
            if (!(source instanceof OutputStream)) throw new RuntimeException(new IllegalArgumentException("Wrong parameter type for standard error"));
        } else if (number == 1) {
            if (!(source instanceof OutputStream)) throw new RuntimeException(new IllegalArgumentException("Wrong parameter type for standard output"));
        } else if (number == 0) {
            if (!(source instanceof InputStream)) throw new RuntimeException(new IllegalArgumentException("Wrong parameter type for standard input"));
        }
        streamCollection.put(number, source);
    }

    public BufferedReader getBufferedReader(int channel, int size) {
        return new BufferedReader(new InputStreamReader((InputStream)getStream(channel)), size);
    }

    public BufferedReader getBufferedReader(int channel) {
        return getBufferedReader(channel, 4096);
    }

    public PrintWriter getPrintWriter(int channel) {
        return new PrintWriter((OutputStream)getStream(channel));
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
