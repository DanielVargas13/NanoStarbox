package box.star.shell;

import java.util.Arrays;
import java.util.List;
import java.util.Stack;

/**
 * Command Shell Function Model
 */
public class Function implements Cloneable {
  private Main context;
  final public String origin;
  final public String name;
  protected List<Command> body;
  protected StreamTable io;
  public Main getContext() { return context; }
  public Function(String origin, String name, StreamTable io){
    this(origin, name, null, io);
  }
  public Function(String origin, String name, List<Command> body, StreamTable io) {
    this.origin = origin;
    this.name = name;
    this.body = body;
    this.io = io;
  }
  protected Function createRuntimeInstance(Main context) {
    try /* never throwing runtime exceptions with closure */ {
      if (context != null)
        throw new IllegalStateException("trying to create function copy from function copy");
      Function newInstance = (Function) super.clone();
      newInstance.context = context;
      return newInstance;
    } catch (Exception e){throw new RuntimeException(e);}
    // finally /* never complete */ { ; }
  }
  final public int invoke(Environment locals, String... parameters){
    if (context == null)
      throw new IllegalStateException("trying to invoke function prototype");
    Stack<String> params = new Stack<>();
    params.add(name);
    params.addAll(Arrays.asList(parameters));
    return exec(locals, params);
  }
  final public int invoke(Environment locals, String name, String... parameters){
    if (context == null)
      throw new IllegalStateException("trying to invoke function prototype");
    Stack<String> params = new Stack<>();
    params.add(name);
    params.addAll(Arrays.asList(parameters));
    return exec(locals, params);
  }
  /**
   * User implementation
   * @param locals the local environment for this function, which is empty of no command environment operations were specified for this call.
   * @param parameters the specified parameters, partitioned and fully-shell-expanded.
   * @return the execution status of this function
   */
  protected int exec(Environment locals, Stack<String> parameters){
    return 0;
  }
  @Override
  public String toString() {
    return "function "+name+"(){"+"\n# function body here\n} # default function io here";
  }
}
