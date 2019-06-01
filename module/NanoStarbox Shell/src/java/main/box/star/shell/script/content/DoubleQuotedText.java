package box.star.shell.script.content;

import box.star.contract.NotNull;
import box.star.text.Char;
import box.star.text.basic.Scanner;

import static box.star.text.Char.BACKSLASH;
import static box.star.text.Char.DOUBLE_QUOTE;

public class DoubleQuotedText implements Scanner.SourceDriver.WithMasterControlPorts {
  @Override
  public String expand(@NotNull Scanner scanner) {
    return Char.toString(BACKSLASH, scanner.current());
  }
  @Override
  public boolean collect(@NotNull Scanner scanner, @NotNull StringBuilder buffer, char character) {
    if (character == DOUBLE_QUOTE) return false;
    buffer.append(character);
    return true;
  }
}
