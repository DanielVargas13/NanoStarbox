package box.star.unix.shell.runtime;

public abstract class Utility extends FunctionModel<Utility.ContextModel> {
  public Utility(Context parent) {
    super(parent, ContextModel.class);
  }
  public static class ContextModel extends Function.ContextModel {}
}
