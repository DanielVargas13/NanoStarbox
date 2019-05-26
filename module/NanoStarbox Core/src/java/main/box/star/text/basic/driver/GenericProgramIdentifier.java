package box.star.text.basic.driver;

import box.star.text.Char;
import box.star.text.basic.ScanControl;
import box.star.text.basic.Scanner;

public class GenericProgramIdentifier implements ScanControl {

  protected int depth = 0;

  protected static char[] FIRST_LETTER_MAP =
      new Char.Assembler(Char.MAP_ASCII_LETTERS)
          .merge('_').toMap();

  protected static char[] NEXT_LETTER_MAP =
      new Char.Assembler(FIRST_LETTER_MAP)
          .merge(Char.MAP_ASCII_NUMBERS).merge('.').toMap();

  @Override
  public boolean collect(Scanner scanner, char character) {
    boolean status;
    if (depth == 0){ depth++;
      status = Char.mapContains(character, FIRST_LETTER_MAP);
    } else status = Char.mapContains(character, NEXT_LETTER_MAP);
    if (!status) scanner.back();
    return status;
  }

}
