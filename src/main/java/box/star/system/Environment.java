package box.star.system;

import java.io.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class Environment extends ConcurrentHashMap<String, String> {

    public static final int
            IO_READABLE = 0,
            IO_WRITABLE = 1,
            IO_ERROR = 2,
            WT_PROCESS = 3,
            WT_COUNT = 4;

    public final static long[] createWaitTimers(){
        return new long[WT_COUNT];
    }

    public interface IRunnableCommand {
        ThreadGroup getBackgroundThreadGroup();
        String getBackgroundThreadName();
        boolean getBackgroundMode();
        String[] getParameters();
        Closeable[] getStreams();
        void onStart();
        void onExit(int value);
        void onException(Exception e);
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

    public void run(IRunnableCommand runnableCommand) {

        if (! runnableCommand.getBackgroundMode()) {

            try {
                runnableCommand.onStart();
                Executive e = start(runnableCommand.getParameters());
                Closeable[] stdio = runnableCommand.getStreams();
                e.readInputFrom((InputStream) stdio[0])
                        .writeOutputTo((OutputStream) stdio[1])
                        .writeErrorTo((OutputStream) stdio[2]);
                runnableCommand.onExit(e.getExitValue());
            } catch (Exception e){runnableCommand.onException(e);}

        } else {
            Thread t = new Thread(runnableCommand.getBackgroundThreadGroup(),new Runnable() {
                @Override
                public void run() {
                    try {
                        runnableCommand.onStart();
                        Executive e = start(runnableCommand.getParameters());
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

    public Executive start(String... parameters) throws IOException {
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

    public static class Executive extends ThreadGroup {

        private static final void transfer(InputStream source, OutputStream dest) throws IOException {
            byte[] buf = new byte[8192];
            int n;
            while ((n = source.read(buf)) > 0) dest.write(buf, 0, n);
            if (!System.out.equals(dest) && !System.err.equals(dest)) dest.close();
            if (!System.in.equals(source)) source.close();
        }

        private java.lang.Process host;
        private Thread readable, writable, error;
        private long[] waitTimers;

        Executive(java.lang.Process host, long[] waitTimers) {
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

        @SuppressWarnings("unchecked")
        private <ANY> ANY get(int stream) {
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