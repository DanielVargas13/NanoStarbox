package box.star.unix.shell.builtin;

import box.star.unix.shell.runtime.Context;

public class Colon extends True {
  public Colon(Context parent) {
    super(parent);
  }
  @Override
  public String toString() {
    return ":";
  }
}
