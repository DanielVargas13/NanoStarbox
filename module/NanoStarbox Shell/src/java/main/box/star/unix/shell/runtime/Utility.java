package box.star.unix.shell.runtime;

public abstract class Utility extends FunctionModel<UtilityContext> {
  public Utility(Context parent) {
    super(parent, UtilityContext.class);
  }
}
