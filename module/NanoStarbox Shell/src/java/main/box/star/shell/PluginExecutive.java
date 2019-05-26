package box.star.shell;

import java.util.Stack;

public interface PluginExecutive {
  int exec(Context context, Stack<Object> parameters);
}
