package box.star.system;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Command implements IRunnableCommand, Runnable {

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
        synchronized (startupMonitor){
            startupMonitor.notifyAll();
        }
    }

    @Override
    public void onExit(int value) {
        running = false;
        exitValue = value;
        synchronized (terminationMonitor){
            terminationMonitor.notifyAll();
        }
    }

    @Override
    public void onException(Exception e) {
        e.printStackTrace();
        synchronized (startupMonitor){
            startupMonitor.notifyAll();
        }
        synchronized (terminationMonitor){
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

    public Command build(String... parameters) {
        return new Command(this, parameters);
    }

    public void run(){
        start();
    }

    public int start(){
        environment.run(this);
        return this.exitValue;
    }

    public void exec(){
        backgroundMode = true;
        environment.run(this);
        synchronized (startupMonitor){
            try { startupMonitor.wait(); }
            catch (InterruptedException e) {}
        }
    }

    public <ANY> ANY get(int stream) {
        return (ANY)streams[stream];
    }

    public void set(int stream, Closeable value){
        streams[stream] = value;
    }

    public void join(){
        if (isRunning() && isBackgroundMode()) synchronized (terminationMonitor) {
            try { terminationMonitor.wait(); }
            catch (InterruptedException e) {}
        }
    }

}
