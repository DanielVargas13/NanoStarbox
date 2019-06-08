package box.star.shell.script;

import box.star.text.basic.Scanner;

public class CommandList extends Command {

  public CommandSet commandList;

  public CommandList(Scanner scanner) {
    super(scanner);
  }

  @Override
  protected void start() {
    commandList = Interpreter.parseSubShell(scanner);
    parseEnding();
    finish();
  }

}
