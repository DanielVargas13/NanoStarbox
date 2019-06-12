package box.star.unix.shell.runtime;

import java.util.Stack;

public abstract class FunctionModel<FunctionContextClass extends Context> {
  public final FunctionContextClass local;
  public FunctionModel(Context parent, Class<FunctionContextClass> cls) {
    local = parent.createSubContext(cls);
  }
  abstract public String toString();
  abstract public ContextResult exec(FunctionContextClass context, Stack<Object> parameters);
}
