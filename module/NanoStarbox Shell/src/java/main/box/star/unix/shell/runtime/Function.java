package box.star.unix.shell.runtime;

import box.star.unix.shell.script.FunctionDefinition;

public abstract class Function extends FunctionModel<FunctionContext> {

  final FunctionDefinition functionDefinition;

  public Function(Context parent) {
    super(parent, FunctionContext.class);
    functionDefinition = null;
  }

  public Function(Context parent, FunctionDefinition functionDefinition){
    super(parent, FunctionContext.class);
    this.functionDefinition = functionDefinition;
  }

  @Override
  public String toString(){
    return functionDefinition.commandName;
  }

}
