package box.star.shell;

import box.star.text.basic.Scanner;

import java.util.Stack;

public class Plugin extends Function implements Context.Profile.Plugin {
  public Plugin(String origin, String name) {
    super(origin, name);
  }
  @Override
  final protected int exec(Stack<String> parameters) {
    Stack<Object> p = new Stack<>();
    p.addAll(parameters);
    Object build = call(p);
    return (build == null)?1:0;
  }
  /**
   * <p>Plugin gets Object array parameters for exec</p>
   * <br>
   * <p>Plugins operate as functions that can service object requests.</p>
   * <br>
   * <p>When called via text-script, all parameters will be strings.</p>
   * <br>
   * <p>A plugin object may access the context scanner.</p>
   * <br>
   * @param parameters
   * @return
   */
  protected Object call(Stack<Object> parameters) {
    return 0;
  }
  final protected Main getMain(){
    return null;
  }
  final protected Scanner getScanner(){
    return getMain().source;
  }

}
