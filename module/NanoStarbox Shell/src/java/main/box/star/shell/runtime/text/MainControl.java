package box.star.shell.runtime.text;

import box.star.text.basic.ScannerDriver;
import box.star.text.basic.Scanner;

public class MainControl implements ScannerDriver.WithBufferControlPort {
  @Override public boolean collect(Scanner scanner, char character) { return false; }
  @Override
  public boolean collect(Scanner scanner, StringBuilder buffer, char character) {
    return false;
  }
}
