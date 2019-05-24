package box.star.shell;

import java.util.Stack;

public class Plugin extends Function {
  public Plugin(String origin, String name) {
    super(origin, name);
  }
  @Override
  final protected int exec(Stack<String> parameters) {
    return exec(parameters.toArray());
  }
  /**
   * <p>Plugin gets Object array parameters for exec</p>
   * <br>
   * <p>Plugins operate as functions that can service object requests.</p>
   * <br>
   * <p>When called via text-script, all parameters will be strings.</p>
   * <br>
   * @param parameters
   * @return
   */
  final int exec(Object... parameters) {
    return 0;
  }
}
