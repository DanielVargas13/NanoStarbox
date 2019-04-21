package box.star.system;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Command implements IRunnableCommand, Closeable {

    private static final ThreadGroup threadGroup = new ThreadGroup("Starbox System Commands");

    @Override
    public ThreadGroup getBackgroundThreadGroup() {
        return threadGroup;
    }

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
        return streams;
    }

    @Override
    public void onStart() {
        running = true;
    }

    @Override
    public void onExit(int value) {
        running = false;
        exitValue = value;
    }

    @Override
    public void onException(Exception e) {
        e.printStackTrace();
    }

    public boolean isRunning() {
        return running;
    }

    private int exitValue = -1;
    private boolean running, backgroundMode;
    private Closeable[] streams;
    private String[] parameters;
    private Environment environment;

    public int getExitValue() {
        return exitValue;
    }

    public Command(Environment environment, String... parameters) {
        this.environment = environment;
        this.parameters = parameters;
        this.streams = environment.getStreams();
    }

    private Command(Command command, String... parameters) {
        this.environment = command.environment;
        List<String> p = new ArrayList<>();
        p.addAll(Arrays.asList(command.parameters));
        p.addAll(Arrays.asList(parameters));
        String[] n = new String[p.size()];
        this.parameters = p.toArray(n);
        this.streams = environment.getStreams();
    }

    public Command copy(String... parameters) {
        return new Command(this, parameters);
    }

    public int run(){
        environment.run(this);
        return this.exitValue;
    }

    public <ANY> ANY get(int stream) {
        return (ANY)streams[stream];
    }

    public void set(int stream, Closeable value){
        streams[stream] = value;
    }

    @Override
    public void close() throws IOException {}

}
