package box.star.shell.script;

import box.star.text.basic.Scanner;

public class ShellSubMain extends Command {

  public CommandList commandList;

  public ShellSubMain(Scanner scanner) {
    super(scanner);
  }

  @Override
  protected void start() {
    commandList = Interpreter.parseSubShell(scanner);
    parseEnding();
    finish();
  }

}
