package box.star.shell.script;

import box.star.text.basic.Scanner;

public class Command extends Interpreter implements box.star.text.basic.Parser.NewFuturePromise {
  public EnvironmentOperationList environmentOperations;
  public ParameterList parameters;
  public RedirectList redirects;
  public box.star.shell.script.Command pipe;
  public Command(Scanner scanner) {
    super(scanner);
  }
  protected void main(){
    environmentOperations = EnvironmentOperationList.parse(scanner);
    String name;
    name = "breaking point";
  }
  @Override
  protected void start() {
    main(); finish();
  }
}
