package box.star.shell;

import java.util.Arrays;
import java.util.List;
import java.util.Stack;

/**
 * Command Shell Function Model
 */
public class Function implements Cloneable {
  private Main context;
  final protected String name;
  final protected List<Command> body;
  final protected StreamTable io;
  public Main getContext() { return context; }
  public Function(String name, StreamTable io){
    this(name, null, io);
  }
  public Function(String name, List<Command> body, StreamTable io) {
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
  final public int invoke(String... parameters){
    if (context == null)
      throw new IllegalStateException("trying to invoke function prototype");
    Stack<String> params = new Stack<>();
    params.add(name);
    params.addAll(Arrays.asList(parameters));
    return exec(params);
  }
  final public int invoke(String name, String... parameters){
    if (context == null)
      throw new IllegalStateException("trying to invoke function prototype");
    Stack<String> params = new Stack<>();
    params.add(name);
    params.addAll(Arrays.asList(parameters));
    return exec(params);
  }
  /**
   * User implementation
   * @param parameters
   * @return
   */
  protected int exec(Stack<String> parameters){
    return 0;
  }
  @Override
  public String toString() {
    return "function "+name+"(){"+"\n# function body here\n} # default function io here";
  }
}
