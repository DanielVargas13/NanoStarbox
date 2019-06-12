package box.star.unix.shell.builtin;

import box.star.unix.shell.runtime.Builtin;
import box.star.unix.shell.runtime.Context;
import box.star.unix.shell.runtime.ContextResult;

import java.util.Stack;

import static box.star.unix.shell.runtime.ContextResult.TRUE;

public class True extends Builtin {

  public True(Context parent) {
    super(parent);
  }

  @Override
  public String toString() {
    return null;
  }

  @Override
  public ContextResult exec(ContextModel context, Stack<Object> parameters) {
    return TRUE;
  }

}
