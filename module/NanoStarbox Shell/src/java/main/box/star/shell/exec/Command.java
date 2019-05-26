package box.star.shell.exec;

import box.star.shell.Context;

import java.util.Stack;

/**
 * <p>Command</p>
 * <br>
 * <p>A class hosts this interface to provide command services to the shell.
 * The class must support the default constructor.</p>
 * <br>
 */
public interface Command {
  int exec(Context context, Stack<String> parameters);
}
