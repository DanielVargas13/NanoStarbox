package box.star.unix.shell.script;

import box.star.contract.NotNull;
import box.star.text.basic.Parser;
import box.star.text.basic.Scanner;
import box.star.unix.shell.runtime.Context;

public class SourceElement extends Parser implements Parser.WithParameterPort {

  protected Context context;

  public SourceElement(@NotNull Scanner scanner) {
    super(scanner);
  }

  @Override
  final protected void start(Object... parameters) {
    context = (Context) parameters[0];
    start();
  }

}
