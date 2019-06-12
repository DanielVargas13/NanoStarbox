package box.star.unix.shell.script;

import box.star.contract.NotNull;
import box.star.text.basic.Bookmark;
import box.star.text.basic.Scanner;
import box.star.unix.shell.script.Parameter;
import box.star.unix.shell.script.SourceElement;

public class HereDocument extends Parameter {
  public HereDocument(@NotNull Scanner scanner) {
    super(scanner);
  }
}
