package box.star.unix.shell.runtime;

import box.star.unix.shell.script.FunctionDefinition;

public abstract class Function extends FunctionModel<Function.ContextModel> {

  final FunctionDefinition functionDefinition;

  public Function(Context parent) {
    super(parent, ContextModel.class);
    functionDefinition = null;
  }

  public Function(Context parent, FunctionDefinition functionDefinition){
    super(parent, ContextModel.class);
    this.functionDefinition = functionDefinition;
  }

  public static class ContextModel extends Context {}

  @Override
  public String toString(){
    return functionDefinition.commandName;
  }

}
