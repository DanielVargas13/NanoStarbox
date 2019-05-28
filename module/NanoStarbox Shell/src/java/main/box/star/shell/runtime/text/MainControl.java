package box.star.shell.runtime.text;

import box.star.text.basic.Scanner;

public class MainControl implements Scanner.SourceDriver.WithBufferControlPort {
  public boolean collect(Scanner scanner, StringBuilder buffer, char character) {
    if (character == 0) {
      System.err.println();
      return false;
    }
    System.err.print(character);
    return ! scanner.endOfSource();
  }
}
