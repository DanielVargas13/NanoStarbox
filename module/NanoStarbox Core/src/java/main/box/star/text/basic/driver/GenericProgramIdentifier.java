package box.star.text.basic.driver;

import box.star.text.Char;
import box.star.text.basic.Scanner;

public class GenericProgramIdentifier implements Scanner.SourceDriver.WithSimpleControlPort {

  private int depth = 0;

  protected static Char.Map FIRST_LETTER_MAP =
      new Char.Map("first identifier letter", new Char.Assembler(Char.MAP_ASCII_LETTERS.toMap())
          .merge('_'));

  protected static Char.Map NEXT_LETTER_MAP =
      new Char.Map("identifier character", new Char.Assembler(FIRST_LETTER_MAP.toMap())
          .merge(Char.MAP_ASCII_NUMBERS.toMap()).merge('.'));

  @Override
  public boolean collect(Scanner scanner, char character) {
    boolean status;
    if (depth == 0){ depth++;
      status = FIRST_LETTER_MAP.contains(character);
    } else status = NEXT_LETTER_MAP.contains(character);
    if (! scanner.endOfSource() && ! status) scanner.back();
    return status;
  }

}
