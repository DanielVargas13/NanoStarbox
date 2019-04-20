package box.star;

import com.sun.istack.internal.NotNull;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

/**
 * <p>Classic Turtle Soup with all the Fixin's.</p>
 */
public class Command {

    private static final int
            WRITABLE = 1,
            READABLE = 0,
            ERROR = 2;
    private static final int PROCESS = 3;
    private Closeable[] stream = new Closeable[]{
            null,
            System.out,
            System.err
    };

    private String command;
    private String[] parameters;
    private Environment environment = new Environment(System.getenv());

    /**
     * <p>Set a persistent key-value-pair in the command's environment table.</p>
     *
     * @param key
     * @param value
     * @return this command for further configuration/access.
     */
    public Command set(String key, String value) {
        environment.set(key, value);
        return this;
    }

    /**
     * <p>Fetch the value for the key from the command's environment.</p>
     *
     * @param key
     * @return
     */
    public String get(String key) {
        return buildEnvironment().get(key);
    }

    /**
     * <p>Update the command's environment with an overlay.</p>
     *
     * @param environment overlay-environment
     * @return this command for further configuration/access.
     */
    public Command set(Map<String, String> environment) {
        this.environment.map.putAll(environment);
        return this;
    }

    public final String unixCurrentDirKey="PWD";

    /**
     * <p>Sets the PWD environment variable for this command.</p>
     *
     * @param path
     * @return this command for further configuration/access.
     */
    public Command setDirectory(String path) {
        this.environment.set(unixCurrentDirKey, path);
        return this;
    }

    /**
     * <p>Fet's the PWD environmenvt variable for this command.</p>
     *
     * @return
     */
    public String getDirectory() {
        String currentDirectory = (environment.map.containsKey(unixCurrentDirKey)) ?
                environment.get(unixCurrentDirKey): System.getProperty("user.dir");
        return currentDirectory;
    }

    /**
     * <p>Creates a new command with the specified parameters.</p>
     *
     * @param command    the command to execute.
     * @param parameters the initial parameters for the command.
     */
    public Command(String command, String... parameters) {
        this.command = command;
        this.parameters = parameters;
    }

    public Command apply(String... parameters) {
        return new Command(this, parameters);
    }

    public Command connect(Command start){
        return Command.chain(this).pipe(start);
    }

    private Command(Command command, String... parameters) {
        this.command = command.command;
        List<String> p = new ArrayList<>();
        p.addAll(Arrays.asList(command.parameters));
        p.addAll(Arrays.asList(parameters));
        String[] n = new String[p.size()];
        this.parameters = p.toArray(n);
    }
    /**
     * <p>Sets the read-redirection stream for this command.</p>
     *
     * @param source
     * @return this command for further configuration/access.
     */
    public Command readFrom(InputStream source) {
        stream[READABLE] = source;
        return this;
    }

    /**
     * <p>Sets the write-redirection stream for this command.</p>
     *
     * @param destination
     * @return this command for further configuration/access.
     */
    public Command writeOutputTo(OutputStream destination) {
        stream[WRITABLE] = destination;
        return this;
    }

    /**
     * <p>Sets the error-redirection stream for this command.</p>
     *
     * @param destination
     * @return this command for further configuration/access.
     */
    public Command writeErrorTo(OutputStream destination) {
        stream[ERROR] = destination;
        return this;
    }

    private long[] processWaitTimes = new long[]{0, 0, 0, 0};

    /**
     * <p>Sets the timeout duration for a timer ATOM in milliseconds.</p>
     *
     * @param timer
     * @param milliseconds
     * @return
     */
    public Command setWaitTimeout(int timer, long milliseconds) {
        processWaitTimes[timer] = milliseconds;
        return this;
    }

    private static class Process {

        private java.lang.Process host;
        private Thread readable, writable, error;
        private long[] waitTimes;

        Process(java.lang.Process host, long[] waitTimeout) {
            this.host = host;
            this.waitTimes = waitTimeout;
        }

        void readInputFrom(InputStream input) {
            if (input == null) return;
            (readable = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        transfer(input, get(WRITABLE));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            })).start();
        }

        void writeOutputTo(OutputStream output) {
            if (output == null) return;
            (writable = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        transfer(get(READABLE), output);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            })).start();
        }

        void writeErrorTo(OutputStream output) {
            if (output == null) return;
            (error = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        transfer(get(ERROR), output);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            })).start();
        }

        int getStatus() {
            if (writable != null) try {
                if (waitTimes[WRITABLE] > 0) writable.join(waitTimes[WRITABLE]);
                else writable.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (readable != null) try {
                if (waitTimes[READABLE] > 0) readable.join(waitTimes[READABLE]);
                else readable.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (error != null) try {
                if (waitTimes[ERROR] > 0) error.join(waitTimes[ERROR]);
                else error.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            try {
                if (waitTimes[PROCESS] > 0) host.waitFor(waitTimes[PROCESS], TimeUnit.MILLISECONDS);
                else host.waitFor();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return host.exitValue();
        }

        @SuppressWarnings("unchecked")
        <ANY> ANY get(int stream) {
            switch (stream) {
                case WRITABLE:
                    return (ANY) host.getOutputStream();
                case READABLE:
                    return (ANY) host.getInputStream();
                case ERROR:
                    return (ANY) host.getErrorStream();
            }
            return null;
        }

    }

    private Process createProcess(String... parameters) throws IOException {
            HashMap<String, String> build = buildEnvironment();
            String currentDirectory = (build.containsKey(unixCurrentDirKey)) ?
                    build.get(unixCurrentDirKey): System.getProperty("user.dir");
            return new Process(Runtime.getRuntime().exec(invocation(parameters), Environment.compile(build),
                    new File(currentDirectory)), processWaitTimes);
    }

    /**
     * <p>Compiles this command and its parameters into a single String[].</p>
     *
     * @param parameters
     * @return the new command string array.
     */
    public String[] invocation(String... parameters) {
        Stack<String> x = new Stack<>();
        x.push(command);
        if (this.parameters != null) x.addAll(Arrays.asList(this.parameters));
        if (parameters != null) x.addAll(Arrays.asList(parameters));
        String[] out = new String[x.size()];
        return x.toArray(out);
    }

    /**
     * <p>Starts execution of this command or pipe with the specified parameters.</p>
     *
     * @param parameters
     * @return
     */
    public int start(String... parameters) throws Exception {
        return call(stream, parameters);
    }

    private int call(Object[] stream, String... parameters) throws Exception {
        Process process = createProcess(pipe ? null : parameters);
        if (process == null) return -1;
        process.writeErrorTo((OutputStream) stream[ERROR]);
        process.writeOutputTo((OutputStream) stream[WRITABLE]);
        if (pipe) {
            link.writeOutputTo(process.get(WRITABLE));
            link.start(parameters);
        } else {
            process.readInputFrom((InputStream) stream[READABLE]);
        }
        return process.getStatus();
    }

    /**
     * <p>Creates a copy of the command given.</p>
     *
     * @param start the first command of the pipe chain.
     * @return a copy of the command suitable for pipe chaining as the root (first command) of the pipe chain.
     */
    public static Command chain(Command start) {
        Command shadow = new Command(start.command, start.parameters);
        shadow.environment = new Environment(start.environment.map);
        shadow.chain = true;
        shadow.processWaitTimes = start.processWaitTimes;
        return shadow;
    }

    private static Command shadowCommandPipe(Command master, Command slave) {
        Command shadow = new Command(slave.command, slave.parameters);
        shadow.environment = new Environment(slave.environment.map);
        shadow.pipe = true;
        shadow.link = master;
        shadow.processWaitTimes = slave.processWaitTimes;
        return shadow;
    }

    private boolean pipe, chain;
    private Command link;

    /**
     * <p>Links the command given to the end of the pipe chain.</p>
     *
     * @param command
     * @return the last command in the pipe chain, which is suitable for the next call to pipe().
     */
    public Command pipe(Command command) {
        if (brokenPipeChain()) throw
                new RuntimeException(new IllegalStateException("Call to pipe must have a designated root chain."));
        return shadowCommandPipe(this, command);
    }

    private boolean brokenPipeChain() {
        Command root = this;
        while (root.link != null) root = root.link;
        return root.chain == false;
    }

    private static class Environment {
        HashMap<String, String> map;

        Environment(Map<String, String> base) {
            map = new HashMap<>(base);
        }

        HashMap<String, String> runtime(Environment overlay) {
            HashMap<String, String> build = new HashMap<>(map);
            if (overlay != null && overlay != this) build.putAll(overlay.map);
            return build;
        }

        public String get(String key) {
            return map.get(key);
        }

        public void set(String key, String value) {
            map.put(key, value);
        }

        public int size() {
            return map.size();
        }

        static String[] compile(HashMap<String, String> build) {
            String[] out = new String[build.size()];
            int i = 0;
            for (String key : build.keySet()) {
                out[i++] = key + "=" + build.get(key);
            }
            return out;
        }
    }

    private HashMap<String, String> buildEnvironment() {
        Command target = this;
        while (target.link != null) target = target.link;
        return target.environment.runtime(environment);
    }

    private static final void transfer(@NotNull InputStream source, @NotNull OutputStream dest) throws IOException {
        byte[] buf = new byte[8192];
        int n;
        while ((n = source.read(buf)) > 0) dest.write(buf, 0, n);
        if (!System.out.equals(dest) && !System.err.equals(dest)) dest.close();
        if (!System.in.equals(source)) source.close();
    }

}
