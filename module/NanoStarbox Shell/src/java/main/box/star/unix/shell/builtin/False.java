package box.star.unix.shell.builtin;

import box.star.unix.shell.runtime.Builtin;
import box.star.unix.shell.runtime.Context;
import box.star.unix.shell.runtime.ContextResult;

import java.util.Stack;

import static box.star.unix.shell.runtime.ContextResult.FALSE;

public class False extends Builtin {

  public False(Context parent) {
    super(parent);
  }

  @Override
  public String toString() {
    return null;
  }

  @Override
  public ContextResult exec(ContextModel context, Stack<Object> parameters) {
    return FALSE;
  }

}
