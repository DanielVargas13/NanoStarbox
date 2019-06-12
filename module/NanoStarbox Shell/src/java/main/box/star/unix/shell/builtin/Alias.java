package box.star.unix.shell.builtin;

import box.star.unix.shell.runtime.Utility;
import box.star.unix.shell.runtime.Context;
import box.star.unix.shell.runtime.ContextResult;

import java.util.Stack;

public class Alias extends Utility {
  public Alias(Context parent) {
    super(parent);
  }
  @Override
  public ContextResult exec(ContextModel context, Stack<Object> parameters) {
    return null;
  }
  @Override
  public String toString() {
    return "alias";
  }
}
