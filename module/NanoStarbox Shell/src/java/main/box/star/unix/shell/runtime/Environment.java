package box.star.unix.shell.runtime;

public class Environment<ParentEnvironment extends Environment> {

  ParentEnvironment parent;
  StreamMap streamMap;
  VironMap variableMap;

  public void setParent(ParentEnvironment parent) {
    if (this.parent != null)
      throw new IllegalStateException("this context does not support re-parenting");
    this.parent = parent;
  }

  public ParentEnvironment getParent() {
    return parent;
  }

}
