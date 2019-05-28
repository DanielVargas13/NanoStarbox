package box.star.text.basic.driver;

import box.star.text.Char;
import box.star.text.basic.Scanner;

public class UnsignedInteger implements Scanner.SourceDriver.WithSimpleControlPort {
  @Override
  public boolean collect(Scanner scanner, char character) {
    return Char.mapContains(character, Char.MAP_ASCII_NUMBERS);
  }
}
