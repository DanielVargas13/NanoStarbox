package box.star.unix.shell.script;

import box.star.contract.NotNull;
import box.star.text.basic.Scanner;

public class Command extends CommandModel {

  public ParameterList parameters;

  public Command(@NotNull Scanner scanner) {
    super(scanner);
  }

}