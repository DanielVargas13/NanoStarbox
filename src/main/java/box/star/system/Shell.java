package box.star.system;

import box.star.util.SharedMap;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class Shell {

  private final static String SHELL_GROUP_NAME = "Thread Group";

  private PrintStream traceStream = System.err;
  private boolean traceLifeCycle;

  private boolean tracing() {
    if (subShell) {
      return parent.tracing();
    }
    return traceLifeCycle;
  }

  private void postLifeCycleTraceMessage(String msg) {
    if (!tracing()) return;
    if (isSubShell()) {
      parent.postLifeCycleTraceMessage(msg);
      return;
    }
    traceStream.println(msg);
  }

  public void traceLifeCycle(boolean trace) {
    if (isSubShell()) {
      parent.traceLifeCycle(trace);
      return;
    }
    traceLifeCycle = trace;
  }

  public void traceLifeCycle(PrintStream err) {
    if (isSubShell()) {
      parent.traceLifeCycle(err);
      return;
    }
    traceStream = err;
    traceLifeCycle(true);
  }

  public static class ActionGroup extends ThreadGroup {

    private Shell shell;

    private List<Shell> shells = new ArrayList<>();
    private List<Action> actions = new ArrayList<>();

    private ConcurrentHashMap<String, Main> models = new ConcurrentHashMap<>();

    public static ActionGroup getCurrentGroup() {
      ThreadGroup group = Thread.currentThread().getThreadGroup();
      do {
        if (group instanceof ActionGroup) return (ActionGroup) group;
        group = group.getParent();
      } while (group != null);
      return null;
    }

    /**
     * Gets the current shell.
     * Can be called from any inherited thread.
     *
     * @return
     */
    public static Shell getCurrentShell() {
      return getCurrentGroup().getShell();
    }

    /**
     * Gets the current action.
     * Can only be called within an action.
     *
     * @return
     */
    public static Action getCurrentAction() {
      return (Action) Thread.currentThread();
    }

    private ActionGroup(Shell shell) {
      super(SHELL_GROUP_NAME);
      this.shell = shell;
      ActionGroup parent = ActionGroup.getCurrentGroup();
      if (parent != null) shell.parent = parent.getShell();
    }

    public Shell getShell() {
      return shell;
    }

    private boolean haveSuperActionModels() {
      return shell.parent != null;
    }

    private Main getSuperActionModel(String name) {
      return shell.parent.group.getActionModel(name);
    }

    private void createActionModel(Main main) {
      models.put(main.name, main);
      return;
    }

    private Main getActionModel(String name) {
      if (models.containsKey(name)) {
        return models.get(name);
      }
      if (haveSuperActionModels()) {
        return getSuperActionModel(name);
      }
      throw new IllegalArgumentException("unknown shell action model: " + name);
    }

    private void addAction(Action target) {
      actions.add(target);
    }

    private void removeAction(Action target) {
      actions.remove(target);
    }

    private void addShell(Shell target) {
      if (shell.isSubShell()) {
        shell.postLifeCycleTraceMessage("adding " + shell + " to " + shell.parent);
      } else {
        shell.postLifeCycleTraceMessage("adding " + shell);
      }
      shells.add(target);
    }

    private void removeShell(Shell target) {
      if (shell.isSubShell()) {
        shell.postLifeCycleTraceMessage("removing " + shell + " from " + shell.parent);
      } else {
        shell.postLifeCycleTraceMessage("removing " + shell);
      }
      shells.remove(target);
    }

  }

  private ActionGroup group;
  private Environment environment;
  private Shell parent;
  private boolean subShell;
  private String shellTypeName;
  private Action main;
  protected int status;

  public static Shell createShell(Main main) {
    Shell shell = new Shell();
    shell.main = new Action(shell.group, main);
    return shell;
  }

  private Shell() {
    this.group = new ActionGroup(this);
    spawn();
  }

  Object object;
  public Object getObject(){ return object; }

  private Object setObject(Object o){
    object = o;
    if (o == null || o.equals(false)){
      status = 1;
    } else {
      if (o instanceof Integer || o instanceof Long) {
        status = Integer.valueOf((int)o);
      } else {
        status = 0;
      }
    }
    return o;
  }

  public int getStatus() { return status; }

  public String getShellType() { return shellTypeName; }

  public String getShellModel() {
    return ((main == null) ? "boot-loader" : main.getName());
  }

  @Override
  public String toString() { return getShellType() + " " + getShellModel(); }

  private void spawn() {
    this.subShell = (this.parent != null);
    if (isSubShell()) {
      shellTypeName = "sub-shell";
      this.environment = this.parent.environment.copy();
      parent.onCreateSubShell(this);
    } else {
      shellTypeName = "root-shell";
      this.environment = new Environment();
    }
    onCreateShell();
  }

  public void registerActionModel(Main model) { group.createActionModel(model); }

  public Action createAction(String modelName) {
    Action action = new Action(group, modelName);
    onCreateAction(action);
    return action;
  }

  public Object call(String name, Object... parameters){
    Method f = (Method) group.getActionModel(name);
    return setObject(f.main(parameters));
  }

  public void start(Action action, String... parameters) {
    action.exec(true, parameters);
  }

  public int run(String function, String... parameters){
    Function f = (Function) group.getActionModel(function);
    return status = f.main(parameters);
  }

  public int run(Action action, String... parameters) {
    action.exec(false, parameters);
    return status = action.task.getStatus();
  }

  public int main(String... parameters) {
    main.exec(false, parameters);
    return status = main.task.getStatus();
  }

  public boolean isSubShell() { return subShell; }

  private void onCreateShell() {
    if (!isSubShell()) postLifeCycleTraceMessage("creating " + this);
  }

  private void onShellStart() {
    if (isSubShell()) {
      postLifeCycleTraceMessage("entering " + this + " in " + parent);
    } else {
      postLifeCycleTraceMessage("entering " + this);
    }
  }

  private void onShellExit() {
    if (isSubShell()) {
      postLifeCycleTraceMessage("exiting " + this + " in " + parent);
    } else {
      postLifeCycleTraceMessage("exiting " + this);
    }
  }

  private void onCreateSubShell(Shell child) {
    postLifeCycleTraceMessage("creating " + child + " in " + this);
  }

  private void onSubShellStart(Shell child) {
    postLifeCycleTraceMessage("starting " + child + " in " + this);
  }

  private void onSubShellExit(Shell child) {
    postLifeCycleTraceMessage("stopping " + child + " in " + this);
  }

  private void onSubShellException(Action action) {
    action.task.exception.printStackTrace();
  }

  private void onShellException(Action action) {
    if (isSubShell()) {
      parent.onSubShellException(action);
    } else {
      action.task.exception.printStackTrace();
    }
  }

  private void onCreateAction(Action action) {
    postLifeCycleTraceMessage("creating " + action + " in " + this);
  }

  private void onActionStart(Action action) {
    postLifeCycleTraceMessage("starting " + action + " in " + this);
  }

  private void onActionComplete(Action action) {
    postLifeCycleTraceMessage("finished " + action + " in " + this);
  }

  public static class Environment implements Cloneable {

    private SharedMap<Integer, Closeable> streams;
    private SharedMap<String, String> variables;

    public Environment() {
      this(null, System.out, System.err, System.getProperty("user.dir"));
    }

    public Environment(InputStream stdin, OutputStream stdout, OutputStream stderr, String currentDirectory) {
      variables = new SharedMap<>(System.getenv());
      streams = new SharedMap<>();
      if (stdin != null)
        streams.put(0, stdin);
      if (stdout != null)
        streams.put(1, stdout);
      if (stdout != null)
        streams.put(2, stderr);
      setCurrentDirectory(new File(currentDirectory));
    }

    /**
     * Creates a shallow copy of this environment.
     * <p>
     * Modifications to this environment will not be reflected in the parent
     * environment.
     *
     * @return
     * @throws CloneNotSupportedException
     */
    public Environment copy() {
      try {
        return (Environment) clone();
      }
      catch (CloneNotSupportedException e) {
        throw new RuntimeException(e);
      }
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
      Environment clone = (Environment) super.clone();
      clone.variables = variables.getLink();
      clone.streams = streams.getLink();
      return clone;
    }

    public final String[] compile() {
      List<String> keys = keys();
      String[] out = new String[keys.size()];
      int i = 0;
      for (String key : keys) {
        out[i++] = key + "=" + get(key);
      }
      return out;
    }

    public String get(String key) {
      return variables.get(key);
    }

    public void set(String key, String value) {
      variables.put(key, value);
    }

    public <ANY> ANY get(Integer key) {
      return (ANY) streams.get(key);
    }

    public void set(Integer key, Closeable value) {
      streams.put(key, value);
    }

    public List<String> keys() {
      return new ArrayList<>(variables.keySet());
    }

    public void remove(Object key) {
      if (key == null) return;
      if (key instanceof String) {
        variables.remove(key);
        return;
      } else if (key instanceof Integer) streams.remove(key);
      throw new IllegalArgumentException("unknown key type for remove operation: " + key.getClass().getName());
    }

    public List<Integer> streams() {
      return new ArrayList<>(streams.keySet());
    }

    public String getCurrentDirectory() {
      return get("PWD");
    }

    public void setCurrentDirectory(File directory) {
      set("PWD", directory.getPath());
    }

  }

  public static class Main implements Cloneable {

    protected Shell shell;
    private int status;

    private final String name;
    private int clients;
    private Exception exception;
    private boolean running;
    private final String statusWaitHandle = "", startup = "", shutdown = "";
    private long start, finish;
    private String[] parameters;
    private boolean actionMode, backgroundMode;

    public Main(String name) {
      this.name = name;
    }

    private Main open(boolean background, String[] parameters) {
      Main clone = null;
      try { clone = (Main) clone(); }
      catch (CloneNotSupportedException e) {}
      finally {
        clone.backgroundMode = background;
        clone.parameters = parameters;
        return clone;
      }
    }

    public int main(String[] parameters) {return 0;}

    final private boolean haveClients() {
      return clients != 0;
    }

    final private void unlockAllWaitHandles() {
      while (haveClients()) unlockWaitHandle();
    }

    final private void unlockWaitHandle() {
      synchronized (statusWaitHandle) {
        statusWaitHandle.notify();
      }
    }

    final private void waitForShutdown() {
      while (running) synchronized (shutdown) {
        try {
          shutdown.wait();
        }
        catch (InterruptedException e) {}
        finally {
        }
      }
    }

    final private void waitForCompletion() {
      if (running) synchronized (statusWaitHandle) {
        clients++;
        try {
          statusWaitHandle.wait();
        }
        catch (InterruptedException e) {}
        finally {
          clients--;
        }
      }
    }

    final public long[] getTime() {
      return new long[]{start, finish};
    }

    final public long getDuration() {
      return (finish == 0) ? new Date().getTime() : finish - start;
    }

    final public int getStatus() {
      waitForCompletion();
      return status;
    }

    final private Shell begin() {
      ActionGroup group = ActionGroup.getCurrentGroup();
      Action action = ActionGroup.getCurrentAction();
      shell = group.getShell();
      running = true;
      start = new Date().getTime();
      if (shell.main.equals(action)) group.addShell(shell);
      else {
        actionMode = true;
        group.addAction(action);
      }
      return shell;
    }

    final private void end() {
      finish = new Date().getTime(); // store length property
      running = false;
      unlockAllWaitHandles();
    }

    final public boolean isActionMode() {
      return actionMode;
    }

    final public boolean isBackgroundMode() {
      return backgroundMode;
    }

    final private void close() {
      ActionGroup group = ActionGroup.getCurrentGroup();
      if (actionMode) {
        shell.group.removeAction(ActionGroup.getCurrentAction());
      } else {
        group.removeShell(shell);
      }
    }

    final public boolean isRunning() {
      return running;
    }

    @Override
    final protected Object clone() throws CloneNotSupportedException {
      Main clone = (Main) super.clone();
      return clone;
    }
  }

  public static class Function extends Main {
    public Function(String name) {
      super(name);
    }
  }

  public static class Method extends Main {
    public Method(String name) {
      super(name);
    }
    @Override
    final public int main(String[] parameters) {
      return super.main(parameters);
    }
    public Object main(Object... parameters){return null;}
  }

  public static class Action extends Thread {

    private Main model, task;

    private Action(ActionGroup group, Main model) {
      super(group, model.name);
      this.model = model;
    }

    private Action(ActionGroup group, String name) {
      super(group, name);
      this.model = group.getActionModel(name);
    }

    @Override
    final public void run() {
      Shell shell = task.begin();
      if (task.actionMode) shell.onActionStart(this);
      else {
        if (shell.isSubShell()) shell.parent.onSubShellStart(shell);
        shell.onShellStart();
      }
      try {
        synchronized (task.startup) { task.startup.notifyAll(); }
        task.status = task.main(this.task.parameters);
      }
      catch (Exception exception) { this.task.exception = exception; }
      finally {
        task.end();
        synchronized (task.shutdown) { task.shutdown.notifyAll(); }
        try {
          if (task.exception != null) {
            shell.onShellException(this);
          } else {
            if (task.actionMode) shell.onActionComplete(this);
            else {
              shell.onShellExit();
              if (shell.isSubShell()) shell.parent.onSubShellExit(shell);
            }
          }
        }
        catch (Exception e) {}
        task.close();
      }
    }

    final private void exec(boolean background, String[] parameters) {
      this.task = model.open(background, parameters);
      this.setDaemon(background == true);
      super.start();
      if (background == false) {
        synchronized (task.shutdown) {
          try {
            task.shutdown.wait();
          }
          catch (InterruptedException e) {}
          finally {
            if (task.running) task.waitForShutdown();
          }
        }
      }
    }

    @Override
    final public synchronized void start() {
      throw new NotImplementedException();
    }

    @Override
    public String toString() {
      if (task != null) {
        String mode = task.isBackgroundMode() ? "task-action" : "shell-action";
        return mode + " " + task.name;
      }
      return "new " + model.name + " action";
    }

  }

}
