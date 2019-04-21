package box.star.system;

import java.io.Closeable;

public class Command implements IRunnableCommand {

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
        return new String[0];
    }

    @Override
    public Closeable[] getStreams() {
        return new Closeable[]{null, System.out, System.err};
    }

    @Override
    public void onStart() {
        ranOnce = running = true;
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

    public void setBackgroundMode(boolean backgroundMode) {
        this.backgroundMode = backgroundMode;
    }

    public boolean isRunning() {
        return running;
    }

    public boolean ranOnce(){
        return ranOnce;
    }

    int exitValue = -1;
    boolean running, backgroundMode, ranOnce;

    public int getExitValue() {
        return exitValue;
    }

}
