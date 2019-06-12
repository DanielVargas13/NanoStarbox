package box.star.unix.shell.script;

import box.star.contract.NotNull;
import box.star.text.basic.Scanner;

abstract class CommandSet extends CommandModel {
  public CommandList commands;
  CommandSet(@NotNull Scanner scanner) {
    super(scanner);
  }
}
