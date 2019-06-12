package box.star.unix.shell.runtime;

public abstract class Builtin extends FunctionModel<BuiltinContext> {
  public Builtin(Context parent) {
    super(parent, BuiltinContext.class);
  }
}
