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

    /**
     * Create a process.
     * @return a new process with an inherited-io-stream-set
     */
    public Process createProcess(){
        return createProcess(null);
    }

    /**
     * Create a process.
     * @param io io-stream-set
     * @return a new process with corrresponding io-stream-set
     */
    public Process createProcess(Streams io){
        return new Process(this, io);
    }

    /**
     * shell-main-interface
     */
    public interface ISubshell {
        /**
         * Threaded Shell Access Method
         *
         * @param shell the current shell.
         */
        void run(Shell shell);
    }

    /**
     * Creates a subshell.
     * @param subshell shell-main-interface
     * @return a new subshell with an inherited-stream-set and shell-main-interface.
     */
    public Shell createSubshell(ISubshell subshell) {
        return createSubshell(null, subshell);
    }

    /**
     * Creates a subshell.
     * @param io
     * @param subshellInterface
     * @return a new subshell with a stream-set and shell main interface.
     */
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

    /**
     * Runs shell in the foreground.
     * @param subshell
     * @return the status code of the execution.
     */
    public int run(Shell subshell) {
        subshell.thread.start();
        return exitCode = subshell.getExitCode();
    }

    /**
     * Runs process in the foreground.
     * @param parameters
     * @return the status code of the execution.
     */
    public int run(String... parameters){
        return run(createProcess(null), parameters);
    }

    /**
     * Runs process in the foreground.
     * @param process from createProcess
     * @param parameters
     * @return the status code of the execution.
     */
    public int run(Process process, String... parameters) {
        process.start(parameters);
        return exitCode = process.getExitCode();
    }

    /**
     * Runs shell in the background.
     * @param subshell
     * @return
     */
    public Shell start(Shell subshell) {
        subshell.thread.start();
        background.add(subshell.thread);
        return subshell;
    }

    /**
     * Runs process in the background.
     * @param process
     * @param parameters
     * @return
     */
    public Process start(Process process, String... parameters){
        process.start(parameters);
        background.add(process);
        return process;
    }

    /**
     * Runs process in the background
     * @param parameters
     * @return
     */
    public Process start(String... parameters) {
        return start(createProcess(null), parameters);
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
