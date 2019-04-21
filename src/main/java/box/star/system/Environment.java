package box.star.system;

import box.star.util.TokenGenerator;

import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class Environment extends ConcurrentHashMap<String, String> {

    static final ThreadGroup threadGroup = new ThreadGroup("Starbox System Environment");

    private static final ConcurrentHashMap<String, Action> actionMap = new ConcurrentHashMap<>();
    private int lastExitValue = 0;

    private ConcurrentHashMap<String, Object> objectStore = new ConcurrentHashMap<>();
    private TokenGenerator objStoreToken = new TokenGenerator();

    public String store(Object value){
        String token;
        do {
            token = objStoreToken.createNewToken(new int[]{4, 4});
        } while (objectStore.contains(token));
        objectStore.put(token, value);
        return token;
    }

    public <ANY> ANY fetch(String token){
        return (ANY)objectStore.get(token);
    }

    /**
     * Registers the given java class action class as a known-factory-action-method.
     *
     * @param type the subclass of Action to register.
     */
    public static void registerAction(Class<? extends Action> type){
        try {
            Action factory = type.newInstance();
            actionMap.put(factory.toString(), factory);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Removes the given java class action from the known-factory-action-methods.
     *
     * @param command the command to disable.
     */
    public static void cancelAction(String command){
        actionMap.remove(command);
    }

    /**
     * Registers a custom java class action from an instance-factory instance.
     *
     * @param factory the Action subclass instance to use
     */
    public static void registerAction(Action factory){
        actionMap.put(factory.toString(), factory);
    }

    public static final int
            IO_READABLE = 0,
            IO_WRITABLE = 1,
            IO_ERROR = 2,
            WT_PROCESS = 3,
            WT_COUNT = 4;

    /**
     * Convenience method to create blank wait timers.
     *
     * @return a wait-timer-set.
     */
    public final static long[] createWaitTimers(){
        return new long[WT_COUNT];
    }

    private Closeable[] streams = new Closeable[]{null, System.out, System.err};

    /**
     * Gets a copy of the stream-set.
     *
     * @return a copy of the stream-set.
     */
    public Closeable[] getStreams(){
        return streams.clone();
    }

    /**
     * Gets the stream associated with a stdio stream number.
     *
     * @param stream the stream number to get.
     * @param <ANY> (Closeable: auto cast)
     * @return the Closeable associated with this stream number
     */
    public <ANY> ANY get(int stream) {
        return (ANY)streams[stream];
    }

    /**
     * Sets the stream to a closeable value.
     *
     * @param stream the io stream to use (see IO_*)
     * @param value the closeable stream to set.
     */
    public void set(int stream, Closeable value){
        streams[stream] = value;
    }

    private long[] executiveWaitTimers = createWaitTimers();
    private File currentDirectory = new File(System.getProperty("user.dir"));

    /**
     * Sets the environment process/io wait-timer.
     * <br><br>
     * <p>Timers:
     * <ul>
     *     <li><code>Environment.IO_READABLE</code> for the (readable) command output-stream-wait-time</li>
     *     <li><code>Environment.IO_WRITABLE</code> for the (writable) command input-stream-wait-time</li>
     *     <li><code>Environment.IO_ERROR</code> for the (readable) command error-stream-wait-time</li>
     *     <li><code>Environment.WT_PROCESS</code> for the process wait-time</li>
     * </ul>
     * </p>
     * @param timer the timer to write
     * @param millis the value in milliseconds
     */
    public void setWaitTimeout(int timer, long millis){
        executiveWaitTimers[timer] = millis;
    }

    /**
     * gets the environment process/io wait-timer.
     *
     * @param timer the timer to read.
     * @return the timer value.
     */
    public long getWaitTimeout(int timer){
        return executiveWaitTimers[timer];
    }

    /**
     * Creates a new default environment based on the system profile.
     *
     * Current directory is inherited.
     *
     * Standard input is null.
     * Standard output is inherited.
     * Standard error is inherited.
     */
    public Environment(){this(null, null, null);}

    /**
     * Creates a custom environment.
     *
     * @param directory the current directory to use for execution.
     * @param base the map to use for the environment.
     */
    public Environment(File directory, Map<String, String> base){this(directory, base, null);}

    /**
     * Creates a custom environment.
     *
     * @param directory the directory to use for execution.
     * @param base the map to use for the environment.
     * @param waitTimers the process/io timer-set to use.
     */
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

    /**
     * Creates a simple copy of the current environment.
     *
     * Wait timers are copied, the current directory is copied, and the environment
     * variables are copied.
     *
     * @return a new environment
     */
    public Environment copy(){
        return new Environment(currentDirectory, this, this.executiveWaitTimers);
    }

    /**
     * Compiles the current environment for execution.
     *
     * @return the compiled environment.
     */
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

    /**
     * Gets the system string name for the environment
     * @return
     */
    public String getPlatformName(){
        return OS.getOperatingSystemKind().toString();
    }

    /**
     * Runs the command in the foreground or background, depending on the implementation.
     *
     * foreground commands set the last exit value of the command environment.
     *
     * @param runnableCommand the command interface to execute
     */
    public void run(ICommandHost runnableCommand) {

        if (! runnableCommand.isBackgroundMode()) {

            try {
                Executive e = start(runnableCommand.getParameters());
                runnableCommand.onStart(e.getStreams());
                Closeable[] stdio = runnableCommand.getStreams();
                e.readInputFrom((InputStream) stdio[0]);
                        e.writeOutputTo((OutputStream) stdio[1]);
                        e.writeErrorTo((OutputStream) stdio[2]);
                runnableCommand.onExit(lastExitValue = e.getExitValue());
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

    /**
     * Runs a command and returns an executive.
     *
     * Finds the action by name or match. if that fails, locates the command
     * in the current environment if available.
     *
     * @param parameters the command parameter string.
     * @return the environment executive.
     * @throws IOException if the command is not found.
     */
    public Executive start(String... parameters) throws IOException{
        String commandName = parameters[0];
        if (actionMap.containsKey(commandName)){
            Action factory = actionMap.get(commandName);
            Action command;
            try { command = (Action) factory.createAction(); }
            catch (Exception e) {throw new RuntimeException(e);}
            command.start(this, parameters);
            return new Executive(command, executiveWaitTimers);
        }
        for (Action factory: actionMap.values()){
            if (factory.match(commandName)) {
                Action command;
                try { command = (Action) factory.createAction(); }
                catch (Exception e) {throw new RuntimeException(e);}
                command.start(this, parameters);
                return new Executive(command, executiveWaitTimers);
            }
        }
        Process p = Runtime.getRuntime().exec(parameters, compile(), currentDirectory);
        return new Executive(p, executiveWaitTimers);
    }

    /**
     * Runs the given command and waits for the exit value.
     *
     * @param stdio the streams to use for the connection.
     * @param parameters the command and parameters.
     * @return the exit value.
     * @throws IOException if the command could not be resolved.
     */
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
        return lastExitValue = run.getExitValue();
    }

    public static boolean isWindows(){
        return OS.getOperatingSystemKind().equals(OS.Kind.Windows);
    }

    public static boolean isLinux(){
        return OS.getOperatingSystemKind().equals(OS.Kind.Linux);
    }

    public static boolean isMacOS(){
        return OS.getOperatingSystemKind().equals(OS.Kind.MacOS);
    }

    public static boolean isOtherOperatingSystem(){
        return OS.getOperatingSystemKind().equals(OS.Kind.Other);
    }

    public static String getLocalHostName(){
        InetAddress ip;
        String hostname = null;
        try {
            ip = InetAddress.getLocalHost();
            hostname = ip.getHostName();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return hostname;
    }

    public static String getLocalNetworkAddress(){
        InetAddress ip;
        try {
            ip = InetAddress.getLocalHost();
            return ip.toString();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * OS.Kind ostype=OS.getOperatingSystemKind();
     * switch (ostype) {
     *     case Windows: break;
     *     case MacOS: break;
     *     case Linux: break;
     *     case Other: break;
     * }
     * helper class to check the operating system this Java VM runs in
     *
     * please keep the notes below as a pseudo-license
     *
     * http://stackoverflow.com/questions/228477/how-do-i-programmatically-determine-operating-system-in-java
     * compare to http://svn.terracotta.org/svn/tc/dso/tags/2.6.4/code/base/common/src/com/tc/util/runtime/Os.java
     * http://www.docjar.com/html/api/org/apache/commons/lang/SystemUtils.java.html
     */
    static final class OS {
      /**
       * types of Operating Systems
       */
      public enum Kind {
        Windows, MacOS, Linux, Other
      };

      // cached result of OS detection
      private static Kind thisKind;

      /**
       * detect the operating system from the os.name System property and cache
       * the result
       *
       * @returns - the operating system detected
       */
      public static Kind getOperatingSystemKind() {
        if (thisKind == null) {
          String OS = System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH);
          if ((OS.indexOf("mac") >= 0) || (OS.indexOf("darwin") >= 0)) {
            thisKind = Kind.MacOS;
          } else if (OS.indexOf("win") >= 0) {
            thisKind = Kind.Windows;
          } else if (OS.indexOf("nux") >= 0) {
            thisKind = Kind.Linux;
          } else {
            thisKind = Kind.Other;
          }
        }
        return thisKind;
      }
    }

    /**
     * Custom command driver interface
     */
    public interface ICommandHost {
        /**
         * Tell the environment the name of this thread.
         *
         * This feature is only called if the command is being run in background mode
         * as specified by isBackgroundMode()
         * @return the name of the thread
         */
        String getBackgroundThreadName();

        /**
         * Tell the environment how this command is run.
         * @return true if this command runs in the background.
         */
        boolean isBackgroundMode();

        /**
         * Tell the environment what paramters this command provides.
         * @return the parameters of the target command.
         */
        String[] getParameters();

        /**
         * Supply the external process streams for this command.
         *
         * @return the external: input, output and error streams.
         */
        Closeable[] getStreams();

        /**
         * The environment notifies the client interface that the process has begun.
         *
         * @param pipe the internal stdio of the process.
         */
        void onStart(Closeable[] pipe);

        /**
         * The environment notifies the client interface that the process has terminated.
         *
         * @param value the value of the process termination.
         */
        void onExit(int value);

        /**
         * The environment notifies the client that a fatal exception has taken place.
         *
         * @param e
         */
        void onException(Exception e);
    }

    public int getExitValue() {
        return lastExitValue;
    }

    public static class Executive extends ThreadGroup {

        private static final void transfer(InputStream source, OutputStream dest) throws IOException {
            byte[] buf = new byte[8192];
            int n;
            while ((n = source.read(buf)) > 0) dest.write(buf, 0, n);
            dest.flush();
            if (!System.out.equals(dest) && !System.err.equals(dest)) dest.close();
            if (!System.in.equals(source)) source.close();
        }

        private Process host;
        private Thread readable, writable, error;
        private long[] waitTimers;

        Executive(Process host, long[] waitTimers) {
            super("Starbox Environment Executive");
            this.host = host;
            if (waitTimers != null) this.waitTimers = waitTimers;
        }

        public Executive readInputFrom(InputStream input) {
            if (input == null) return this;
            (readable = new Thread(this, new Runnable() {
                @Override
                public void run() {
                    try {
                        transfer(input, get(IO_WRITABLE));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }, "Reader")).start();
            return this;
        }

        public Executive writeOutputTo(OutputStream output) {
            if (output == null) return this;
            (writable = new Thread(this, new Runnable() {
                @Override
                public void run() {
                    try {
                        transfer(get(IO_READABLE), output);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }, "Writer")).start();
            return this;
        }

        public Executive writeErrorTo(OutputStream output) {
            if (output == null) return this;
            (error = new Thread(this, new Runnable() {
                @Override
                public void run() {
                    try {
                        transfer(get(IO_ERROR), output);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }, "Error")).start();
            return this;
        }

        public int getExitValue() {
            if (writable != null) try {
                if (waitTimers[IO_WRITABLE] > 0) writable.join(waitTimers[IO_WRITABLE]);
                else writable.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (readable != null) try {
                if (waitTimers[IO_READABLE] > 0) readable.join(waitTimers[IO_READABLE]);
                else readable.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (error != null) try {
                if (waitTimers[IO_ERROR] > 0) error.join(waitTimers[IO_ERROR]);
                else error.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            try {
                if (waitTimers[WT_PROCESS] > 0) host.waitFor(waitTimers[WT_PROCESS], TimeUnit.MILLISECONDS);
                else host.waitFor();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return host.exitValue();
        }

        public Closeable[] getStreams(){
            return new Closeable[]{get(0), get(1), get(2)};
        }

        @SuppressWarnings("unchecked")
        public <ANY> ANY get(int stream) {
            switch (stream) {
                case IO_WRITABLE: // = 1
                    return (ANY) host.getOutputStream();
                case IO_READABLE: // = 0
                    return (ANY) host.getInputStream();
                case IO_ERROR: // = 2
                    return (ANY) host.getErrorStream();
            }
            return null;
        }

    }

    public static class ExitTrap extends RuntimeException {
        private int status;
        public ExitTrap(int status, String message){
            super(message);
            this.status = status;
        }
        public int getStatus() {
            return status;
        }
    }

}