package box.star.shell.runtime;

import box.star.contract.NotNull;
import box.star.contract.Nullable;
import box.star.shell.runtime.io.Stream;
import box.star.shell.runtime.io.StreamTable;

import java.io.File;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public interface Shell {

  interface Executive {
    int exec(Context context, Stack<String> parameters);
  }
  interface Plugin {
    int exec(Context context, Stack<Object> parameters);
  }

  class Context {

    final static private String PROPERTY_ACCESS_READ_ONLY =
        "you can't do that,"+
            " the corresponding property is marked read only for clients";

    final static public boolean systemConsoleMode = System.console() != null;

    private interface FirstClassExecutive {
      int exec();
    }

    private interface SecondClassExecutive {
      int exec(Stack<String> parameters);
    }

    Context parent;
    Environment environment;
    StreamTable io;
    String origin;
    int shellLevel;
    boolean initialized;

    protected int exitValue;
    public final long timeStamp;

    public final static long bootTimeStamp;

    static {
      bootTimeStamp = System.currentTimeMillis();
    }

    {
      this.timeStamp = System.currentTimeMillis();
    }

    Context(){}

    Context(Context parent, String origin) {
      WithParentOf(parent).AndOriginOf(origin);
    }

    public boolean isReady() {
      return initialized && origin != null;
    }

    @NotNull final protected Context AndOriginOf(@NotNull String origin){
      if (this.origin != null)
        throw new IllegalStateException(PROPERTY_ACCESS_READ_ONLY);
      this.origin = origin;
      return this;
    }

    @NotNull final protected Context WithParentOf(@NotNull Context parent){
      if (this.parent != null)
        throw new IllegalStateException(PROPERTY_ACCESS_READ_ONLY);
      importContext(parent);
      initialized = true;
      return this;
    }

    void importContext(@NotNull Context parent){
      if (parent == null) {
        if (this instanceof Shell.MainContext) return;
        throw new IllegalArgumentException("parent context is null");
      }
      this.parent = parent;
      this.shellLevel = parent.shellLevel;
      importEnvironment(parent.environment);
      importStreamTable(parent.io);
    }

    @NotNull final protected Context importEnvironment(@Nullable Environment environment){
      if (this.environment == null){
        this.environment = new Environment();
        if (environment == null) {
          this.environment.loadFactoryEnvironment(true);
          return this;
        }
      }
      if (environment != null) this.environment.mapAllObjects(environment, true);
      return this;
    }

    @NotNull final protected Context importStreamTable(@Nullable StreamTable io){
      if (this.io == null) {
        this.io = new StreamTable();
        if (io == null) {
          this.io.loadFactoryStreams();
          return this;
        }
      }
      if (io != null) this.io.putAll(io);
      return this;
    }

    /**
     * <p>For the definitions phase, and String reporting</p>
     */
    Map<Integer, String> redirects;
    String redirectionText(){
      StringBuilder out = new StringBuilder();
      for (Integer k:redirects.keySet()){
        String table = redirects.get(k);
        String[] fields = table.split(":", 2);
        out.append(k).append(fields[0]).append(' ').append('"').append(fields[1]).append("\" ");
      }
      return out.substring(0, Math.max(0, out.length() - 1));
    }

    @NotNull
    final protected Context getMain(){
      if (this instanceof MainContext) return this;
      else return parent.getMain();
    }

    @NotNull
    protected URI getShellBaseDirectory(){
      return getMain().getShellBaseDirectory();
    }

    /**
     * Provides the $0...$N variables for text-expansion and other uses within all contexts.
     * @return
     */
    @NotNull
    protected Stack<String> getParameters(){ /* current-variables: $0...$N */
      return parent.getParameters();
    }

    @Nullable
    final protected Context getParent() {
      return parent;
    }

    public int getExitValue() {
      return exitValue;
    }

    final public String getOrigin() {
      return this.origin;
    }

    final public int getShellLevel() {
      return shellLevel;
    }

    protected Environment compileEnvironmentOperations(List<String> operations){
      // todo: compile environment operations into an environment table, which can be imported on a context
      return null;
    }

    protected StreamTable compileRedirects(Map<Integer, String> redirects){
      if (this.redirects == null) this.redirects = redirects;
      else this.redirects.putAll(redirects);
      // todo: parseEnvironmentOperationList redirects, and return a new stream table, which can be imported on a context
      return null;
    }

    public int evaluate(String origin, String text, StreamTable io) {
      // TODO: evaluation routine
      return 0;
    }

    public Stack<String> expandTextParameter(String origin, int number, String text){
      // TODO: expandParameter to stack with environment overlay
      return null;
    }

    public String expandText(String origin, String text){
      // TODO: expandText with environment overlay
      return null;
    }

    final public <T> T getObject(Class<T> type, String name){
      return environment.getObject(type, name);
    }
    final public Class getObjectType(String name){return environment.get(name).getObjectClass();}

    public String get(String name){ return environment.getString(name); }
    final public Stream get(int stream){
      return io.get(stream);
    }

    public void set(String name, Object value, boolean export){
      environment.put(name, new Environment.Variable(value, export));
    }

    public void set(String name, Object value){
      environment.put(name, new Environment.Variable(value));
    }

    final public void set(int number, Stream value){
      io.put(number, value);
    }

    public boolean has(String key){
      return environment.containsKey(key);
    }

    final public boolean has(int stream){
      return io.containsKey(stream);
    }

    public boolean has(String key, Class type){
      return environment.containsKey(key) && environment.get(key).isObjectOfClass(type);
    }

    public void export(String name, boolean value) {environment.export(name, value);}
    public boolean exporting(String name) {return environment.exporting(name);}
    public void mapAllStrings(Map<String, String> map, boolean export) {environment.mapAllStrings(map, export);}
    public void removeAllKeys(List<String> keys) {environment.removeAllKeys(keys);}

    public List<String> keyList() {return environment.keyList();}
    public List<String> exportList(){return environment.exportList();}

    final public void mapAllObjects(Map<String, Object> map, boolean export) {environment.mapAllObjects(map, export);}
    final public String getCurrentDirectory() {return environment.getCurrentDirectory();}
    final public void setCurrentDirectory(String currentDirectory) {environment.setCurrentDirectory(currentDirectory);}
    final public File getRelativeFile(String name) {return environment.getRelativeFile(name);}

  }

  class MainContext extends Context {
    Stack<String> parameters;
    URI shellBaseDirectory;
    protected MainContext(Context parent, String origin) {
      super(parent, origin);
    }
    @Override
    public final URI getShellBaseDirectory() {
      if (shellBaseDirectory == null) try /*  throwing runtime exceptions with closure */ {
        shellBaseDirectory = getClass().getProtectionDomain().getCodeSource().getLocation().toURI();
      } catch (Exception e){throw new RuntimeException("failed to get main context path", e);}
      return shellBaseDirectory;
    }
    final protected Context WithParametersOf(Stack<String> parameters){
      if (this.parameters != null)
        throw new IllegalStateException(Context.PROPERTY_ACCESS_READ_ONLY);
      this.parameters = parameters;
      return this;
    }
    @NotNull
    protected Stack<String> getParameters(){
      return parameters;
    }
    @NotNull final protected Context importParameters(Context parent){
      this.parameters = new Stack<>();
      this.parameters.addAll(parent.getParameters());
      return this;
    }
  }

  class CommandContext extends /* COMMAND [ | COMMAND... ] */ Context {
    CommandContext(Context parent, String origin) {
      super(parent, origin);
    }
  }

  class CommandShellContext extends /* [$](COMMAND...) */ MainContext implements Context.FirstClassExecutive {
    @Override
    void importContext(Context parent) {
      super.importContext(parent);
      this.shellLevel++;
      // spec uses a parameter copy
      importParameters(parent);
    }
    CommandShellContext(Context parent, String origin) { super(parent, origin); }
    @Override
    public int exec() {
      return 0;
    }
  }

  class CommandGroupContext extends /* { COMMAND... } */ Context implements Context.FirstClassExecutive {
    void importContext(Context parent){
      this.parent = parent;
      this.environment = parent.environment;
      importStreamTable(parent.io);
    }
    CommandGroupContext(Context parent, String origin) {
      super(parent, origin);
    }
    @Override
    public int exec() {
      return 0;
    }
  }

  class SourceContext extends /* source ... */ MainContext implements Context.SecondClassExecutive {
    @Override
    void importContext(Context parent){
      this.parent = parent;
      this.environment = parent.environment;
      importStreamTable(parent.io);
      importParameters(parent);
    }
    SourceContext(Context parent, String origin) {
      super(parent, origin);
    }
    public int exec(Stack<String> parameters) {
      return 0;
    }
    @Override
    protected @NotNull Stack<String> getParameters() {
      return parameters;
    }
  }

}
