package box.star.shell;

import java.util.Stack;

public interface Executive {
  int exec(Context context, Stack<String> parameters);
}
