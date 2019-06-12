package box.star.unix.shell.script;

import box.star.contract.NotNull;
import box.star.text.basic.Scanner;

public class SourceTerminator extends SourceElement {
  public SourceTerminator(@NotNull Scanner scanner) {
    super(scanner);
  }
  @Override
  protected void start() {
    if (! scanner.endOfSource()) throw new IllegalStateException("scanner is not at end of source: "+getOrigin());
    finish();
  }
}
