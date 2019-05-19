package box.star.bin.sh;

import box.star.bin.sh.promise.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class Function extends Process implements FunctionMain, FunctionFactory, FactoryFunction, VariableCatalog<Function>, Runnable, Cloneable {

  protected ShellHost shell;
  protected SharedMap<String, String> local;
  protected BufferedInputStream stdin;
  protected BufferedOutputStream stdout, stderr;
  private String name;
  private Thread thread;
  private String[] parameters;
  private int status;
  private Pipe p_stdin, p_stdout, p_stderr;
  private boolean destroying, running;

  @Override
  public String getHelpUri() {
    return null;
  }

  @Override
  protected Function clone() {
    hostAccessOnly("clone");
    try {
      return (Function) super.clone();
    }
    catch (Exception ignored) {}
    return null;
  }

  public FactoryFunction createFunction(ShellHost shell, SharedMap<String, String> superLocal) {
    hostAccessOnly("createFunction(shell, superLocal)");
    Function instance = clone();
    instance.shell = shell;
    if (superLocal == null) instance.local = new SharedMap<>();
    else instance.local = superLocal;
    return instance;
  }

  final public Function exec(String... parameters) {
    hostAccessOnly("exec(String... parameters)");
    this.parameters = parameters;
    p_stdin = new Pipe();
    p_stdout = new Pipe();
    p_stderr = new Pipe();
    stdin = new BufferedInputStream(p_stdin.input);
    stdout = new BufferedOutputStream(p_stdout.output);
    stderr = new BufferedOutputStream(p_stderr.output);
    thread = new Thread(this);
    thread.start();
    return this;
  }

  @Override
  public int main(String[] parameters) {return 0;}

  @Override
  final public void run() {
    if (running) {
      hostAccessOnly("run");
      return;
    }
    running = true;
    try {
      status = main(parameters);
    }
    finally {
      running = false;
      try {
        stdin.close();
        stdout.close();
        stderr.close();
      }
      catch (IOException ignored) {}
    }
  }

  @Override
  final public int exitValue() {
    hostAccessOnly("exitValue");
    try { thread.join(); }
    catch (InterruptedException ie) {}
    catch (Exception e) {if (!destroying) throw new RuntimeException(e);}
    return status;
  }

  @Override
  final public boolean isAlive() {
    hostAccessOnly("isAlive() == true");
    return thread.isAlive();
  }

  private void hostAccessOnly(String context) {
    if (Thread.currentThread().equals(thread)) {
      throw new RuntimeException(context, new IllegalThreadStateException("this method cannot be accessed from within its own thread."));
    }
  }

  private void clientAccessOnly(String context) {
    if (!Thread.currentThread().equals(thread)) {
      throw new RuntimeException(context, new IllegalThreadStateException("this method cannot be accessed from its host."));
    }
  }

  @Override
  final public OutputStream getOutputStream() {
    hostAccessOnly("getOutputStream");
    return new BufferedOutputStream(p_stdin.output);
  }

  @Override
  final public InputStream getInputStream() {
    hostAccessOnly("getInputStream");
    return new BufferedInputStream(p_stdout.input);
  }

  @Override
  final public InputStream getErrorStream() {
    hostAccessOnly("getErrorStream");
    return new BufferedInputStream(p_stderr.input);
  }

  @Override
  final public int waitFor() throws InterruptedException {
    hostAccessOnly("waitFor");
    thread.join();
    return status;
  }

  @Override
  final public boolean waitFor(long timeout, TimeUnit unit) throws InterruptedException {
    hostAccessOnly("waitFor(timeout, unit)");
    if (!thread.isAlive()) return true;
    if (timeout <= 0) return false;

    long remainingNanos = unit.toNanos(timeout);
    long deadline = System.nanoTime() + remainingNanos;

    do {
      // Round up to next millisecond
      long msTimeout = TimeUnit.NANOSECONDS.toMillis(remainingNanos + 999_999L);
      thread.wait(msTimeout);
      if (Thread.interrupted())
        throw new InterruptedException();
      if (!thread.isAlive()) {
        return true;
      }
      remainingNanos = deadline - System.nanoTime();
    } while (remainingNanos > 0);
    return (!thread.isAlive());
  }

  @Override
  final public void destroy() {
    hostAccessOnly("destroy");
    try {
      destroying = true;
      stdin.close();
      stdout.close();
      stderr.close();
      if (thread.isAlive()) {
        thread.interrupt();
      }
    }
    catch (IOException e) { e.printStackTrace(); }
  }

  @Override
  final public Process destroyForcibly() {
    hostAccessOnly("destroyForcibly");
    destroy();
    return this;
  }

  @Override
  public Function applyVariables(Map<String, String> variables) {
    clientAccessOnly("applyVariables(variables)");
    local.putAll(variables);
    return this;
  }

  @Override
  public Function clearVariables() {
    clientAccessOnly("clearVariables");
    local.clear();
    return this;
  }

  @Override
  public Function resetVariables() {
    clientAccessOnly("resetVariables");
    local.clear();
    return this;
  }

  @Override
  public String get(String key) {
    clientAccessOnly("getVariables");
    return local.get(key);
  }

  @Override
  public Function set(String key, String value) {
    clientAccessOnly("setVariables");
    local.put(key, value);
    return this;
  }

  @Override
  public Function remove(String key) {
    clientAccessOnly("remove(key)");
    local.remove(key);
    return this;
  }

  @Override
  public List<String> variables() {
    clientAccessOnly("variables");
    return new ArrayList<>(local.keySet());
  }

  @Override
  public boolean haveVariable(String key) {
    clientAccessOnly("haveVariable");
    return local.containsKey(key);
  }

  @Override
  public SharedMap<String, String> exportVariables() {
    clientAccessOnly("exportVariables");
    return local;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public boolean matchName(String name) {
    return false;
  }

  private class Pipe {

    PipedOutputStream output = new PipedOutputStream();
    PipedInputStream input = new PipedInputStream();

    Pipe() {
      try {
        output.connect(input);
      }
      catch (IOException e) { throw new RuntimeException(e); }
    }

    void close() {
      hostAccessOnly("close");
      try {
        input.close();
        output.close();
      }
      catch (Exception e) {}
    }
  }

}
