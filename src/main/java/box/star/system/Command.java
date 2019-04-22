package box.star.system;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

import static box.star.system.Environment.*;

public class Command implements Environment.ICommandHost, Runnable, Closeable {

    private Closeable[] pipe;

    @Override
    public String getBackgroundThreadName() {
        return "Starbox System Command";
    }

    @Override
    public boolean isBackgroundMode() {
        return backgroundMode;
    }

    @Override
    public String[] getParameters() {
        return parameters;
    }

    @Override
    public Closeable[] getStreams() {
        Closeable[] build = new Closeable[3];
        for (int i = 0; i < streams.length; i++) {
            Closeable value = streams[i];
            if (value instanceof Command) {
                Command command = (Command) value;
                command.exec();
                build[i] = command.pipe[i];
            } else {
                build[i] = value;
            }
        }
        return build;
    }

    @Override
    final public void onStart(Closeable[] pipe) {
        this.pipe = pipe;
        running = true;
        synchronized (startupMonitor) {
            startupMonitor.notifyAll();
        }
    }

    private void joinChildren() {
        for (Closeable io : streams) {
            if (io instanceof Command) ((Command) io).join();
        }
    }

    @Override
    final public void onExit(int value) {
        joinChildren();
        running = false;
        exitValue = value;
        synchronized (terminationMonitor) {
            terminationMonitor.notifyAll();
        }
    }

    @Override
    public void onException(Exception e) {
        //e.printStackTrace();
        synchronized (startupMonitor) {
            startupMonitor.notifyAll();
        }
        synchronized (terminationMonitor) {
            terminationMonitor.notifyAll();
        }
    }

    public boolean isRunning() {
        return running;
    }

    private int exitValue = -1;
    private boolean running, backgroundMode;
    private Closeable[] streams;
    private String[] parameters;
    private Environment environment;

    private String startupMonitor = "start", terminationMonitor = "stop";

    public int getExitValue() {
        if (isRunning()) join();
        return pipeChain.peek().exitValue;
    }

    public Command(Environment environment, String... parameters) {
        this.environment = environment;
        this.parameters = parameters;
        this.streams = environment.getStreams();
        this.pipeChain.push(this);
    }

    private Command(Command command, String... parameters) {
        this.environment = command.environment;
        List<String> p = new ArrayList<>();
        p.addAll(Arrays.asList(command.parameters));
        p.addAll(Arrays.asList(parameters));
        String[] n = new String[p.size()];
        this.parameters = p.toArray(n);
        this.streams = environment.getStreams();
        this.pipeChain.push(this);
    }

    public Command create(String... parameters) {
        return new Command(this, parameters);
    }

    public void run() {
        if (isRunning()) return;
        start();
    }

    public int start() {
        if (isRunning()) throw new IllegalStateException("this command is already running");
        environment.run(this);
        return this.exitValue;
    }

    public void exec() {
        if (isRunning()) return;
        backgroundMode = true;
        environment.copy(true).run(this);
        synchronized (startupMonitor) {
            try {
                startupMonitor.wait();
            } catch (InterruptedException e) {
            }
        }
    }

    public <ANY> ANY get(int stream) {
        return (ANY) streams[stream];
    }

    public Command set(int stream, Closeable value) {
        streams[stream] = value;
        return this;
    }

    public void join() {
        if (isRunning() && isBackgroundMode()) synchronized (terminationMonitor) {
            try {
                terminationMonitor.wait();
            } catch (InterruptedException e) {
            }
        }
    }

    @Override
    public void close() throws IOException {
        if (isRunning()) {
            pipe[IO_READABLE].close();
            pipe[IO_WRITABLE].close();
            pipe[IO_ERROR].close();
        }
    }

    private Stack<Command> pipeChain = new Stack<>();

    public Command pipe(Command cmd) {
        pipeChain.peek().set(IO_WRITABLE, cmd);
        pipeChain.push(cmd);
        return this;
    }

    public Stack<Command> getPipeChain() {
        return (Stack<Command>) pipeChain.clone();
    }

    private final static String dq = "\"";
    private final static String esc = "\\";

    @Override
    public String toString() {
        List<String> out = new ArrayList<>();
        out.add(parameters[0]);
        for (int i = 1; i < parameters.length; i++) {
            String content = parameters[i];
            content.replaceAll(dq, esc + dq);
            out.add(dq + parameters[i] + dq);
        }
        return String.join(" ", out);
    }

}
