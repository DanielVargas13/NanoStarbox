package box.star.text.basic.driver;

import box.star.text.basic.ScannerDriver;
import box.star.text.basic.Scanner;

public class MacroFilter implements ScannerDriver.WithExpansionControlPort {
  @Override
  public boolean collect(Scanner scanner, char character) {
    return false;
  }
  @Override
  public boolean expand(Scanner scanner) {
    return false;
  }
}
