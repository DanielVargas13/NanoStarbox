package box.star.shell.script;

import box.star.text.basic.Scanner;
import box.star.text.basic.driver.GenericProgramIdentifier;

import java.util.regex.Pattern;

public class DataOperation extends Interpreter {
  public String variable, operation;
  public Parameter value;
  public DataOperation(Scanner scanner) {
    super(scanner);
  }
  @Override
  protected void start() {
    variable = scanner.run(new GenericProgramIdentifier());
    if (variable.length() == 0) {
      cancel(); return;
    }
    operation = scanner.nextMap(0, 2,'=', '+', '-');
    if (operation.length() == 0){
      cancel(); return;
    }
    value = parse(Parameter.class);
    finish();
  }
}
