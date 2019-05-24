package box.star.shell;

import java.util.Arrays;
import java.util.List;
import java.util.Stack;

/**
 * Function Model
 */
public class Function implements Cloneable {
  protected Host context;
  protected String name;
  protected List<Command> body;
  protected StreamTable io;
  public String getName() {
    return name;
  }
  public Function(String name, List<Command> body, StreamTable io) {
    this.name = name;
    this.body = body;
    this.io = io;
  }
  @Override
  protected Function clone() {
    try /*  throwing runtime exceptions with closure */ {
      return (Function) super.clone();
    } catch (Exception e){throw new RuntimeException(e);}
    finally /*  complete */ {
      ;
    }
  }
  final protected void enterContext(Host context){
    this.context = context;
  }
  final public int invoke(Host context, String... parameters){
    this.context = context;
    Stack<String> params = new Stack<>();
    params.add(name);
    params.addAll(Arrays.asList(parameters));
    return exec(params);
  }
  protected int exec(Stack<String> parameters){
    return 0;
  }
  @Override
  public String toString() {
    return "function "+name+"(){"+"\n# function body here\n} # function io here";
  }
}
