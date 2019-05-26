package box.star.shell.exec;

import box.star.shell.Context;

import java.util.Stack;

public interface Plugin {
  int exec(Context context, Stack<Object> parameters);
}
