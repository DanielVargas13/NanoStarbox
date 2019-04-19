package box.star.exec;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class Shell extends ThreadGroup {

    public final Streams io;
    public final Environment environment;
    private File currentDirectory;
    List<Thread> background = new ArrayList<>();

    private Thread thread;

    public Shell(){
        super("Shell");
        io = new Streams();
        environment = new Environment();
        setCurrentDirectory(System.getProperty("user.dir"));
    }

    private Shell(Shell main){
        super("Subshell");
        io = new Streams(main);
        environment = new Environment(main);
        setCurrentDirectory(main.currentDirectory);
    }

    final public void setCurrentDirectory(String currentDirectory) {
        this.currentDirectory = environment.changeDirectory(new File(currentDirectory));
    }

    final public void setCurrentDirectory(File currentDirectory) {
        this.currentDirectory = environment.changeDirectory(currentDirectory);
    }

    final public File getCurrentDirectory() {
        return currentDirectory;
    }

    public Process createProcess(Streams io){
        return new Process(this, io);
    }

    public interface ISubshell {
        void run(Shell shell);
    }

    public Shell createSubshell(ISubshell subshell) {
        return createSubshell(null, subshell);
    }

    public Shell createSubshell(Streams io, ISubshell subshellInterface) {
        Shell shell = new Shell(this);
        shell.thread = new Thread(this, "Subshell"){
            @Override
            public void run() {
                subshellInterface.run(shell);
                shell.joinBackground();
            }
        };
        return shell;
    }

    public int run(Shell subshell) {
        subshell.thread.start();
        return exitCode = subshell.getExitCode();
    }

    public int run(String... parameters){
        return run(createProcess(null), parameters);
    }

    public int run(Process process, String... parameters) {
        process.start(parameters);
        return exitCode = process.getExitCode();
    }

    public void start(Shell subshell) {
        subshell.thread.start();
        background.add(subshell.thread);
    }


    public void start(Process process, String... parameters){
        process.start(parameters);
        background.add(process);
    }

    private int exitCode = -1;

    public void setExitCode(int exitCode) {
        this.exitCode = exitCode;
    }

    public int getExitCode() {
        if (thread != null) try {
            if (thread.isAlive()) thread.join();
        } catch (InterruptedException e) {e.printStackTrace();}
        return exitCode;
    }

    public void joinBackground(){
        for(Thread t : background){
            try { t.join(); }
            catch (InterruptedException e) { e.printStackTrace(); }
        }
    }

}
