package box.star.bin.sh;

import box.star.OS;
import box.star.bin.sh.promise.FactoryFunction;
import box.star.bin.sh.promise.FunctionFactory;
import box.star.bin.sh.promise.ShellHost;

import java.io.*;
import java.util.*;

/**
 * Nano Starbox Function Shell
 */
public class Shell implements ShellHost<Shell> {

  private final static String LINE_SEPARATOR = "LINE_SEPARATOR";
  int status;
  SharedMap<String, String> variables;
  SharedMap<String, FunctionFactory> functions;
  Streams streams;
  public Shell() {
    this(System.getProperty("user.dir"), System.getenv(), null);
  }

  private Shell(String currentDirectory, Map<String, String> environment, Streams streams) {
    variables = (environment == null) ? new SharedMap<>() : new SharedMap<>(environment);
    setCurrentDirectory(currentDirectory);
    functions = new SharedMap<>();
    if (streams == null) this.streams = new Streams();
    else this.streams = new Streams();
  }

  private Shell(Shell shell, Map<String, String> environment) {
    variables = new SharedMap<>(environment);
    functions = shell.exportFunctions();
    streams = new Streams(shell.exportStreams());
    setCurrentDirectory(shell.getCurrentDirectory());
  }

  private static void fault(Exception x) throws RuntimeException {
    throw new RuntimeException(x);
  }

  public static String[] shiftParameter(String[] parameters) {
    return Arrays.copyOfRange(parameters, 1, parameters.length);
  }

  public int getStatus() {
    return status;
  }

  @Override
  public Shell applyVariables(Map<String, String> variables) {
    this.variables.putAll(variables);
    return this;
  }

  @Override
  public Shell applyFunctions(Map<String, FunctionFactory> factories) {
    this.functions.putAll(factories);
    return this;
  }

  @Override
  public Shell applyStreams(Streams overlay) {
    streams.layer(overlay);
    return this;
  }

  @Override
  public Shell clearFunctions() {
    functions = new SharedMap<>();
    return this;
  }

  @Override
  public Shell clearVariables() {
    variables = new SharedMap<>();
    return this;
  }

  @Override
  public Shell resetVariables() {
    variables = new SharedMap<>(System.getenv());
    return this;
  }

  @Override
  public Shell resetStreams() {
    streams = new Streams();
    return this;
  }

  @Override
  public String get(String key) {
    return variables.get(key);
  }

  @Override
  public <ANY> ANY get(Integer key) {
    return streams.get(key);
  }

  @Override
  public Shell set(String key, String value) {
    variables.put(key, value);
    return this;
  }

  @Override
  public Shell set(Integer key, Closeable stream) {
    streams.set(key, stream);
    return this;
  }

  @Override
  public Shell remove(String key) {
    variables.remove(key);
    return this;
  }

  @Override
  public Shell remove(Integer key) {
    streams.remove(key);
    return this;
  }

  @Override
  public String getCurrentDirectory() {
    return get("PWD");
  }

  @Override
  public Shell setCurrentDirectory(String directory) {
    set("PWD", directory);
    return this;
  }

  @Override
  public Shell defineFunction(FunctionFactory factory) {
    functions.put(factory.getName(), factory);
    return this;
  }

  @Override
  public Shell removeFunction(String name) {
    functions.remove(name);
    return this;
  }

  @Override
  public List<String> variables() {
    return new ArrayList<>(variables.keySet());
  }

  @Override
  public List<String> functions() {
    return new ArrayList<>(functions.keySet());
  }

  @Override
  public List<Integer> streams() {
    return streams.keyList();
  }

  @Override
  public boolean haveVariable(String key) {
    return variables.containsKey(key);
  }

  @Override
  public SharedMap<String, String> exportVariables() {
    return variables.copy();
  }

  @Override
  public boolean haveStream(Integer key) {
    return streams.hasStream(key);
  }

  @Override
  public SharedMap<Integer, Closeable> exportStreams() {
    return streams.export();
  }

  @Override
  public boolean haveFunction(String name) {
    return functions.containsKey(name);
  }

  @Override
  public SharedMap<String, FunctionFactory> exportFunctions() {
    return functions.copy();
  }

  @Override
  public FunctionFactory getFunctionFactory(String name) {
    if (haveFunction(name)) return functions.get(name);
    for (FunctionFactory main : functions.values()) {
      if (main.matchName(name)) return main;
    }
    throw new RuntimeException("Function " + name + " is not defined in this scope");
  }

  @Override
  public int run(String... parameters) {
    Executive p = exec(null, null, parameters);
    try { return status = p.waitFor(); }
    catch (InterruptedException e) { throw new RuntimeException(e);}
  }

  @Override
  public int run(SharedMap<String, String> locals, Streams streams, String... parameters) {
    Executive p = exec(locals, streams, parameters);
    try { return status = p.waitFor(); }
    catch (InterruptedException e) { throw new RuntimeException(e);}
  }

  public Executive execFunction(SharedMap<String, String> locals, String... parameters) {
    FactoryFunction f = getFunctionFactory(parameters[0]).createFunction(this, locals);
    return new Executive(f.exec(parameters));
  }

  public Executive execCommand(SharedMap<String, String> locals, String... parameters) {
    Process p = null;
    try {
      p = Runtime.getRuntime().exec(parameters, variables.compileEnvirons(locals), new File(getCurrentDirectory()));
    }
    catch (IOException e) {throw new RuntimeException(e);}
    return new Executive(p);
  }

  @Override
  public Executive exec(String... parameters) {
    return exec(null, null, parameters);
  }

  @Override
  public Executive exec(SharedMap<String, String> locals, Streams streams, String... parameters) {
    Executive executive;
    String commandName = parameters[0];
    if ("function".equals(commandName)) {
      parameters = shiftParameter(parameters);
      String target = parameters[0];
      if (!haveFunction(target)) {
        throw new IllegalArgumentException("command: " + target + " is not a function");
      }
      executive = execFunction(locals, parameters);
    } else if ("command".equals(commandName)) {
      parameters = shiftParameter(parameters);
      executive = execCommand(locals, parameters);
    } else {
      if (haveFunction(parameters[0])) {
        executive = execFunction(locals, parameters);
      } else {
        executive = execCommand(locals, parameters);
      }
    }
    // this is the part the sets up the streams
    Streams pStreams = this.streams.createLayer(streams);
    executive.readInputFrom(pStreams.get(0)).writeOutputTo(pStreams.get(1)).writeErrorTo(pStreams.get(2));
    return executive;
  }

  @Override
  public int runPipe(SharedMap<String, String> locals, Streams streams, List<String[]> commands) {
    Executive pipe = execPipe(locals, streams, commands);
    return status = pipe.exitValue();
  }

  @Override
  public Executive execPipe(SharedMap<String, String> locals, Streams streams, List<String[]> commands) {
    Streams pipe_streams = this.streams.createLayer(streams);
    Streams common_streams = new Streams(pipe_streams.get(0), null, pipe_streams.get(2));
    Stack<Executive> executives = new Stack<>();
    executives.add(exec(locals, common_streams, commands.remove(0)));
    common_streams.set(0, null);
    for (String[] command : commands) {
      Executive next = exec(locals, common_streams, command);
      next.readInputFrom(executives.peek().get(0));
      executives.add(next);
    }
    return executives.peek().writeOutputTo(pipe_streams.get(1));
  }

  public Command build(String... parameters) {
    return new Command(this, parameters);
  }

  @Override
  public int spawn(String... parameters) {
    Shell shell = new Shell(this, variables);
    return shell.run(parameters);
  }

  @Override
  public int spawn(Map<String, String> variables, String... parameters) {
    Shell shell = new Shell(this, variables);
    return shell.run(parameters);
  }

  @Override
  public Shell readInputFrom(InputStream is) {
    streams.set(0, is);
    return this;
  }

  @Override
  public Shell writeOutputTo(OutputStream os) {
    streams.set(1, os);
    return this;
  }

  public ByteArrayOutputStream writeCapture() {
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    writeOutputTo(os);
    return os;
  }

  @Override
  public Shell writeErrorTo(OutputStream os) {
    streams.set(2, os);
    return this;
  }

  public ByteArrayOutputStream errorCapture() {
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    writeErrorTo(os);
    return os;
  }

  public File getFile(String file) {
    File directory;
    if (System.getProperty("user.dir").equals(getCurrentDirectory())) {
      directory = new File(".");
    } else {
      directory = new File(getCurrentDirectory());
    }
    if (file.startsWith("/") || file.matches("^[A-Zaz]:.*$")) return new File(file);
    if (file.startsWith("./") || file.startsWith(".\\")) return new File(directory, file.substring(2));
    if (file.startsWith("../") || file.startsWith("..\\")) return new File(directory, file.substring(3));
    if (file.equals(".")) return directory;
    return new File(directory, file);
  }

  @Override
  public String getLineSeparator() {
    if (haveVariable(LINE_SEPARATOR)) return get(LINE_SEPARATOR);
    return OS.getLineSeparator();
  }

  @Override
  public Shell setLineSeparator(String separator) {
    set(LINE_SEPARATOR, separator);
    return this;
  }

}
