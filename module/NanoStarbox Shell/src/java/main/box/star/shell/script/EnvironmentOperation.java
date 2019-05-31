package box.star.shell.script;

import box.star.text.basic.Scanner;
import box.star.text.basic.driver.GenericProgramIdentifier;

import java.util.regex.Pattern;

public class EnvironmentOperation extends Interpreter {
  String variable, operation, value;
  public EnvironmentOperation(Scanner scanner) {
    super(scanner);
  }
  @Override
  protected void start() {
    variable = scanner.run(new GenericProgramIdentifier());
    operation = scanner.nextPattern(2, Pattern.compile("="));
    if (operation.length() == 0) { cancel(); return; }
    Parameter data = parse(Parameter.class);
    if (data.status.equals(Status.OK)) value = data.getText();
  }
}
