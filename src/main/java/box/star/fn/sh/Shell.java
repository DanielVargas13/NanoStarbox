package box.star.fn.sh;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Nano Starbox Function Shell
 */
public class Shell {
  
  SharedMap<String, String> variables;
  SharedMap<String, Function>functions;
  Streams streams;
  
  public Shell(){
    this(System.getProperty("user.dir"), System.getenv(), null);
  }

  public Shell(String currentDirectory, Map<String, String> environment, Streams streams) {
    variables = (environment == null)? new SharedMap<>() : new SharedMap<>(environment);
    setCurrentDirectory(currentDirectory);
    functions = new SharedMap<>();
    if (streams == null) this.streams = new Streams();
    else this.streams = new Streams();
  }

  private Shell(Shell shell, Map<String, String> environment) {
    variables = new SharedMap<>(environment);
    functions = shell.functions.copy();
    streams = shell.streams.copy();
    setCurrentDirectory(shell.getCurrentDirectory());
  }

  public String get(String key) {
    return variables.get(key);
  }

  public void set(String key, String value) {
    variables.put(key, value);
  }

  public void remove(String key) {
    variables.remove(key);
    return;
  }

  public String getCurrentDirectory() {
    return get("PWD");
  }

  public void setCurrentDirectory(String directory) {
    set("PWD", directory);
  }

  public void defineFunction(String name, Function function){
    functions.put(name, function);
  }

  public void removeFunction(String name){
    functions.remove(name);
  }

  public List<String> variables() {
    return new ArrayList<>(variables.keySet());
  }

  public List<String> functions(){
    return new ArrayList<>(functions.keySet());
  }

  public boolean haveFunction(String name){
    return functions.containsKey(name);
  }
  
  private Function getFunction(String name){
    if (haveFunction(name))return functions.get(name);
    throw new RuntimeException("Function "+name+" is not defined in this scope");
  }

  public int run(String... parameters) {
    Executive p = exec(null, null, parameters);
    try { return p.waitFor(); }
    catch (InterruptedException e) { throw new RuntimeException(e);}
  }

  public int run(SharedMap<String, String> locals, Streams streams, String... parameters) {
    Executive p = exec(locals, streams, parameters);
    try { return p.waitFor(); }
    catch (InterruptedException e) { throw new RuntimeException(e);}
  }

  public Executive exec(SharedMap<String, String> locals, Streams streams, String... parameters) {
    Executive executive;
    if (haveFunction(parameters[0])) {
      Function f = getFunction(parameters[0]).createInstance(this, locals);
      executive = new Executive(f.exec(parameters));
    } else try {
      Process p = Runtime.getRuntime().exec(parameters, variables.compileEnvirons(locals), new File(getCurrentDirectory()));
      executive = new Executive(p);
    } catch (IOException e) { throw new RuntimeException(e);}
    Streams pStreams = this.streams.createLayer(streams);
    executive.readInputFrom(pStreams.get(0)).writeOutputTo(pStreams.get(1)).writeErrorTo(pStreams.get(2));
    return executive;
  }

  public Executive exec(Command command) {
    if (command.executive == null) {
      command.executive = exec(command.locals, command.streams, command.parameters);
    }
    return command.executive;
  }

  public Command build(String... parameters){
    return new Command(this, parameters);
  }

  public int spawn(String... parameters){
    Shell shell = new Shell(this, variables);
    return shell.run(parameters);
  }

  public int spawn(Map<String, String>variables, String... parameters){
    Shell shell = new Shell(this, variables);
    return shell.run(parameters);
  }

}
