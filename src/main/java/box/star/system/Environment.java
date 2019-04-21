package box.star.system;

import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

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

    public static void cancelAction(String command){
        actionMap.remove(command);
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
        return OS.getOperatingSystemType().toString();
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

    public static boolean isWindows(){
        return OS.getOperatingSystemType().equals(OS.Kind.Windows);
    }

    public static boolean isLinux(){
        return OS.getOperatingSystemType().equals(OS.Kind.Linux);
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
     * OS.Kind ostype=OS.getOperatingSystemType();
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
      protected static Kind detectedOS;

      /**
       * detect the operating system from the os.name System property and cache
       * the result
       *
       * @returns - the operating system detected
       */
      public static Kind getOperatingSystemType() {
        if (detectedOS == null) {
          String OS = System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH);
          if ((OS.indexOf("mac") >= 0) || (OS.indexOf("darwin") >= 0)) {
            detectedOS = Kind.MacOS;
          } else if (OS.indexOf("win") >= 0) {
            detectedOS = Kind.Windows;
          } else if (OS.indexOf("nux") >= 0) {
            detectedOS = Kind.Linux;
          } else {
            detectedOS = Kind.Other;
          }
        }
        return detectedOS;
      }
    }

    public static interface ICommandHost {
        String getBackgroundThreadName();
        boolean isBackgroundMode();
        String[] getParameters();
        Closeable[] getStreams();
        void onStart(Closeable[] pipe);
        void onExit(int value);
        void onException(Exception e);
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
}