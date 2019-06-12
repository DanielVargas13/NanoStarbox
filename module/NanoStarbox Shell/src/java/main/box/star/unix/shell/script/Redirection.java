package box.star.unix.shell.script;

import box.star.contract.NotNull;
import box.star.text.basic.Scanner;

public class Redirection extends SourceElement {
  public String descriptor;
  public String operation;
  public String data;
  public Redirection(@NotNull Scanner scanner) {
    super(scanner);
  }
}
