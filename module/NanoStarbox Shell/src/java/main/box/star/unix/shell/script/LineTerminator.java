package box.star.unix.shell.script;

import box.star.contract.NotNull;
import box.star.lang.Char;
import box.star.text.basic.Scanner;

public class LineTerminator extends SourceElement {
  public String text;
  public LineTerminator(@NotNull Scanner scanner) {
    super(scanner);
  }
  @Override
  protected void start() {
    if (! Char.mapContains(scanner.next(), '\r', '\n')) {
      throw new Scanner.SyntaxError(this, "expected carriage return or line feed, found "+ scanner.nextWordPreview());
    }
    scanner.back();
    while (Char.mapContains(scanner.next(), '\r', '\n')) {
      if (scanner.current() == '\r') {
        text = text + Char.toString('\r', scanner.next('\n'));
      } else text = String.valueOf(scanner.current());
    }
    finish();
  }
}
