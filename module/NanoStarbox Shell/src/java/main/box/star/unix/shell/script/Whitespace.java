package box.star.unix.shell.script;

import box.star.contract.NotNull;
import box.star.text.Char;
import box.star.text.basic.Scanner;

public class Whitespace extends SourceElement {
  String text;
  public Whitespace(@NotNull Scanner scanner) {
    super(scanner);
  }
  @Override
  protected void start() {
    text = scanner.nextMap(Char.MAP_ASCII_ALL_WHITE_SPACE);
    finish();
  }
}
