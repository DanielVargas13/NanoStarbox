package box.star.unix.shell.runtime;

public abstract class Builtin extends FunctionModel<Builtin.ContextModel> {
  public Builtin(Context parent) {
    super(parent, ContextModel.class);
  }
  public static class ContextModel extends Function.ContextModel {}
}
