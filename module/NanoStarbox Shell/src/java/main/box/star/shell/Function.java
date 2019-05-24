package box.star.shell;

import java.util.*;

/**
 * Command Shell Function Model
 */
public class Function extends Context.Profile.FunctionClass implements Cloneable {

  public Context getContext() { return parent; }

//  public Function(String origin, String name){
//    this(origin, name,  null);
//  }
//  public Function(String origin, String name, Map<Integer, String> redirects){
//    this(origin, name, null, redirects);
//  }
//  Function(String origin, String name, List<Command> body, Map<Integer, String> redirects) {
//    this.origin = origin;
//    this.name = name;
//    this.body = body;
//    this.redirects = redirects;
//  }

  // ?
  public static Function parse(Scanner textScanner){
    return null;
  }

  /**
   *
   * @param context
   * @return the newly created function instance
   */
  protected Function createRuntimeInstance(Context context) {
    try /* never throwing runtime exceptions with closure */ {
      if (this.parent != null)
        throw new IllegalStateException("trying to create function instance from function instance");
      Function newInstance = (Function) super.clone();
      newInstance.parent = context;
      return newInstance;
    } catch (Exception e){throw new RuntimeException(e);}
    // finally /* never complete */ { ; }
  }

  final public int invoke(String... parameters){
    if (parent == null)
      throw new IllegalStateException("trying to invoke function definition");
    Stack<String> params = new Stack<>();
    params.add(getName());
    params.addAll(Arrays.asList(parameters));
    return exec(params);
  }
  final public int invoke(String name, String... parameters){
    if (parent == null)
      throw new IllegalStateException("trying to invoke function definition");
    Stack<String> params = new Stack<>();
    params.add(name);
    params.addAll(Arrays.asList(parameters));
    return exec(params);
  }

}
