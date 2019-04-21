package box.star.system;

import java.io.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Environment extends ConcurrentHashMap<String, String> {

    private static final ThreadGroup threadGroup = new ThreadGroup("Starbox System Environment");

    private static final ConcurrentHashMap<String, Action> actionMap = new ConcurrentHashMap<>();

    public static void registerAction(Class<? extends Action> type){
        try {
            Action factory = type.newInstance();
            actionMap.put(factory.toString(), factory);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void registerAction(Action factory){
        actionMap.put(factory.toString(), factory);
    }

    public static final int
            IO_READABLE = 0,
            IO_WRITABLE = 1,
            IO_ERROR = 2,
            WT_PROCESS = 3,
            WT_COUNT = 4;

    public final static long[] createWaitTimers(){
        return new long[WT_COUNT];
    }

    private Closeable[] streams = new Closeable[]{null, System.out, System.err};

    public Closeable[] getStreams(){
        return streams.clone();
    }

    public <ANY> ANY get(int stream) {
        return (ANY)streams[stream];
    }

    public void set(int stream, Closeable value){
        streams[stream] = value;
    }

    private long[] executiveWaitTimers = createWaitTimers();
    private File currentDirectory = new File(System.getProperty("user.dir"));

    public void setWaitTimeout(int timer, long value){
        executiveWaitTimers[timer] = value;
    }

    public long getWaitTimeout(int timer){
        return executiveWaitTimers[timer];
    }

    public Environment(){this(null, null, null);}
    public Environment(File directory, Map<String, String> base){this(directory, base, null);}

    private Environment(File directory, Map<String, String> base, long[] waitTimers) {
        if (directory != null) setCurrentDirectory(directory);
        if (base == null) {
            this.putAll(System.getenv());
        }
        if (waitTimers != null) {
            for(int i = 0; i<Math.min(waitTimers.length, this.executiveWaitTimers.length); i++)
                this.executiveWaitTimers[i] = waitTimers[i];
        }
    }

    public Environment copy(){
        return new Environment(currentDirectory, this, this.executiveWaitTimers);
    }

    public final String[] compile(){
        String[] out = new String[size()];
        int i = 0;
        for (String key : keySet()) {
            out[i++] = key + "=" + get(key);
        }
        return out;
    }

    public File getCurrentDirectory() {
        return currentDirectory;
    }

    public void setCurrentDirectory(File currentDirectory) {
        this.put("PWD", currentDirectory.getPath());
        this.currentDirectory = currentDirectory;
    }

    public String getPlatformName(){
        return System.getProperty("os.name");
    }

    public void run(ICommandHost runnableCommand) {

        if (! runnableCommand.isBackgroundMode()) {

            try {
                Executive e = start(runnableCommand.getParameters());
                runnableCommand.onStart(e.getStreams());
                Closeable[] stdio = runnableCommand.getStreams();
                e.readInputFrom((InputStream) stdio[0]);
                        e.writeOutputTo((OutputStream) stdio[1]);
                        e.writeErrorTo((OutputStream) stdio[2]);
                runnableCommand.onExit(e.getExitValue());
            } catch (Exception e){runnableCommand.onException(e);}

        } else {
            Thread t = new Thread(threadGroup,new Runnable() {
                @Override
                public void run() {
                    try {
                        Executive e = start(runnableCommand.getParameters());
                        runnableCommand.onStart(e.getStreams());
                        Closeable[] stdio = runnableCommand.getStreams();
                        e.readInputFrom((InputStream)stdio[0])
                                .writeOutputTo((OutputStream)stdio[1])
                                .writeErrorTo((OutputStream)stdio[2]);
                        runnableCommand.onExit(e.getExitValue());
                    } catch (Exception e){runnableCommand.onException(e);}
                }
            }, runnableCommand.getBackgroundThreadName());
            t.start();
        }
    }

    public Executive start(String... parameters) throws IOException{
        String commandName = parameters[0];
        if (actionMap.containsKey(commandName)){
            Action factory = actionMap.get(commandName);
            Action command;
            try { command = (Action) factory.createBuiltin(); }
            catch (Exception e) {throw new RuntimeException(e);}
            command.start(this, parameters);
            return new Executive(command, executiveWaitTimers);
        }
        for (Action factory: actionMap.values()){
            if (factory.match(commandName)) {
                Action command;
                try { command = (Action) factory.createBuiltin(); }
                catch (Exception e) {throw new RuntimeException(e);}
                command.start(this, parameters);
                return new Executive(command, executiveWaitTimers);
            }
        }
        Process p = Runtime.getRuntime().exec(parameters, compile(), currentDirectory);
        return new Executive(p, executiveWaitTimers);
    }

    public int run(Closeable[] stdio, String... parameters) throws IOException {
        Executive run = start(parameters);
        if (stdio == null) {
            throw new IllegalArgumentException("no streams were provided for the inline-process");
        }
        if (stdio.length < 3) {
            throw new IllegalArgumentException("not enough streams for environment executive; at least 3 streams are required.");
        }
        run.readInputFrom((InputStream)stdio[0])
            .writeOutputTo((OutputStream)stdio[1])
                .writeErrorTo((OutputStream)stdio[2]);
        return run.getExitValue();
    }

}