package box.star.shell.script;

import box.star.text.Char;
import box.star.text.basic.Scanner;

public class Command extends Interpreter {

  public static final char[] COMMAND_TERMINATOR_MAP = new Char.Assembler(
      Char.toMap('\0', '\n', '\r', '#', ';', '&', '(', ')', '{', '}')
  ).toMap();

  public EnvironmentOperationList environmentOperations;
  public ParameterList parameters;
  public RedirectList redirects;
  public box.star.shell.script.Command pipe;
  public Command(Scanner scanner) {
    super(scanner);
  }
  @Override
  protected void start() {
    environmentOperations = parseEnvironmentOperationList(scanner);
    parameters = parseParameterList(scanner);
    redirects = parseParameterRedirectList(scanner);
    if (parameters.isEmpty()) cancel(); else finish();
  }
}
