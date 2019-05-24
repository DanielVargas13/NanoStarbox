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
  protected Environment locals;
  public Main getContext() { return context; }
  public Function(String origin, String name){
    this(origin, name,  null);
  }
  public Function(String origin, String name, StreamTable io){
    this(origin, name, null, io);
  }
  public Function(String origin, String name, List<Command> body, StreamTable io) {
    this.origin = origin;
    this.name = name;
    this.body = body;
    this.io = io;
  }
  /**
   *
   * @param context
   * @param locals the local environment for this function execution, which is empty of no command environment operations were specified for this call.
   * @param io the io to use if the original definition is to be overridden. if neither this nor the origin specify the io, the Main io table will be copied.
   * @return the newly created function instance
   */
  protected Function createRuntimeInstance(Main context, Environment locals, StreamTable io) {
    try /* never throwing runtime exceptions with closure */ {
      if (this.context != null)
        throw new IllegalStateException("trying to create function instance from function instance");
      Function newInstance = (Function) super.clone();
      newInstance.context = context;
      newInstance.locals = locals;
      if (io != null) newInstance.io = io;
      // TODO: copy local io from main if it is null, or inherit all missing stdio channels from main.
      return newInstance;
    } catch (Exception e){throw new RuntimeException(e);}
    // finally /* never complete */ { ; }
  }
  final public int invoke(String... parameters){
    if (context == null)
      throw new IllegalStateException("trying to invoke function definition");
    Stack<String> params = new Stack<>();
    params.add(name);
    params.addAll(Arrays.asList(parameters));
    return exec(params);
  }
  final public int invoke(String name, String... parameters){
    if (context == null)
      throw new IllegalStateException("trying to invoke function definition");
    Stack<String> params = new Stack<>();
    params.add(name);
    params.addAll(Arrays.asList(parameters));
    return exec(params);
  }
  /**
   * User implementation
   * @param parameters the specified parameters, partitioned and fully-shell-expanded.
   * @return the execution status of this function
   */
  protected int exec(Stack<String> parameters){
    return 0;
  }
  @Override
  public String toString() {
    return "function "+name+"(){"+"\n# function body here\n} # default function io here";
  }
}
