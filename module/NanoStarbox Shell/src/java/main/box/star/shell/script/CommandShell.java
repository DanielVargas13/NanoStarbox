package box.star.shell.script;

import box.star.shell.ScriptParser;
import box.star.text.basic.Scanner;

public class CommandShell extends Command {

  public CommandSet commandList;

  public CommandShell(Scanner scanner) {
    super(scanner);
  }

  @Override
  protected void start() {
    commandList = ScriptParser.parseCommandShell(scanner);
    parseEnding();
    finish();
  }

}
