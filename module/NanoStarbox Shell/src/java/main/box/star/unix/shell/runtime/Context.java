package box.star.unix.shell.runtime;

import java.util.Stack;

public abstract class Context extends Environment<Context> {

  protected Context(){}

  ContextResult exitValue = new ContextResult(0);

  public int getLastResult(){ return exitValue.status; }
  public long getLastResultTime(){ return exitValue.creationTime; }
  public boolean getLastBooleanResult() {return exitValue.status == 0;}
  public Object getLastObjectResult(){
    return exitValue.object;
  }
  public boolean haveObjectResult(){
    return exitValue.object != null;
  }

  void setResult(int status) {
    exitValue = new ContextResult(status);
  }
  void setResult(boolean status) {
    exitValue = new ContextResult(status);
  }
  void setResult(Object value) {
    exitValue = new ContextResult(value);
  }
  void setResult(int status, Object value){
    exitValue = new ContextResult(status, value);
  }
  void setResult(boolean status, Object value){
    exitValue = new ContextResult(status, value);
  }

  public MainContext getMain() {
    if (this instanceof MainContext) return (MainContext) this;
    Context parent = getParent();
    if (parent instanceof MainContext) return (MainContext) parent;
    else if (parent != null) return parent.getMain();
    throw new RuntimeException("unable to locate main context");
  }

  public <T extends Context> T createSubContext(Class<T> cls) {
    if (cls.equals(MainContext.class)) {
      throw new IllegalStateException("cannot create main sub context");
    }
    try {
      T x = cls.newInstance();
      x.setParent(this);
      return x;
    } catch (Exception e){}
    return null;
  }

  Stack<String> parameters = new Stack<>();

  public String getCommandName(){
    return parameters.firstElement();
  }

  public void setParameters(Stack<String> parameters) {
    this.parameters = parameters;
  }

  public void unshiftParameter(String value){
    this.parameters.add(1, value);
  }

  public void shiftParameter(){
    this.parameters.remove(1);
  }

  public void shiftParameter(int count){
    while (--count > -1) shiftParameter();
  }

  public int getParameterCount(){
    return parameters.size() - 1;
  }

  public String getFinalParameter(){
    return parameters.peek();
  }

  public static class CommandGroupContext extends Context {}

  public static class CommandShellContext extends Context {}
}
