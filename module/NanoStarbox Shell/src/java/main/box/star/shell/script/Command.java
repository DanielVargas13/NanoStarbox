package box.star.shell.script;

import box.star.text.basic.Scanner;

public class Command extends Interpreter {
  public EnvironmentOperationList environmentOperations;
  public ParameterList parameters;
  public RedirectList redirects;
  public box.star.shell.script.Command pipe;
  public Command(Scanner scanner) {
    super(scanner);
  }
  @Override
  protected void start() {
    parameters = parseParameterList(scanner);
    if (parameters.isEmpty()) cancel(); else finish();
  }
}
