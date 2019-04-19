package box.star.util;

import java.io.Closeable;
import java.io.File;
import java.util.Hashtable;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;

public class Shell extends Thread {

    public ConcurrentHashMap<String, String> environment;
    public Map<Integer, Closeable> streams;

    Stack<Process> jobs = new Stack<>();

    private int exitCode = 0;
    private File currentDirectory;

    public void setExitCode(int exitCode) {
        this.exitCode = exitCode;
    }

    public Shell(String name) {
        super(name);
        environment = new ConcurrentHashMap<>(System.getenv());
        setCurrentDirectory(new File(System.getProperty("user.dir")));
        streams = new Hashtable<>();
        streams.put(0, System.in);
        streams.put(1, System.out);
        streams.put(2, System.err);
    }

    public final String[] getCompiledEnvironment(){
        String[] out = new String[environment.size()];
        int i = 0;
        for (String key : environment.keySet()) {
            out[i++] = key + "=" + environment.get(key);
        }
        return out;
    }

    public void setCurrentDirectory(File currentDirectory) {
        environment.put("PWD", currentDirectory.getPath());
        this.currentDirectory = currentDirectory;
    }

    public File getCurrentDirectory() {
        return currentDirectory;
    }

    public void main(String[] parameters) {}

    String[] parameters;

    @Override
    public void run() {
        main(parameters);
        while (jobs.size() > 0){
            Process p = jobs.peek();
            if (p.isAlive()) {
                try {
                    p.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            jobs.remove(p);
        }
    }

    public int getExitCode(){
        if (isAlive()) {
            try {
                join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return exitCode;
    }

}
