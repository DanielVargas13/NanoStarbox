package box.star.text.basic;

import box.star.Tools;
import box.star.contract.NotNull;
import box.star.text.Char;

import static box.star.text.Char.*;

public interface CharacterExpander {
  default String expand(@NotNull Scanner scanner){
    char character = scanner.current();
    long start = scanner.getIndex();
    switch (character) {
      case 'd':
        return DELETE + Tools.EMPTY_STRING;
      case 'e':
        return ESCAPE + Tools.EMPTY_STRING;
      case 't':
        return HORIZONTAL_TAB + Tools.EMPTY_STRING;
      case 'b':
        return "\b";
      case 'v':
        return VERTICAL_TAB + Tools.EMPTY_STRING;
      case 'r':
        return CARRIAGE_RETURN + Tools.EMPTY_STRING;
      case 'n':
        return LINE_FEED + Tools.EMPTY_STRING;
      case 'f':
        return FORM_FEED + Tools.EMPTY_STRING;
      /*unicode*/
      case 'u': {
        try { return String.valueOf((char) Integer.parseInt(scanner.nextMap(4,4, MAP_ASCII_HEX), 16)); }
        catch (NumberFormatException e) {
          scanner.walkBack(start);
          throw new Scanner.SyntaxError(scanner, "failed to parse unicode escape sequence using hex method", e);
        }
      }
      /*hex or octal*/
      case '0': {
        char c = scanner.next();
        if (c == 'x') {
          try { return String.valueOf((char) Integer.parseInt(scanner.nextMap(1, 4, MAP_ASCII_HEX), 16)); }
          catch (NumberFormatException e) {
            scanner.walkBack(start);
            throw new Scanner.SyntaxError(scanner, "failed to parse hex escape sequence", e);
          }
        } else {
          scanner.back();
        }
        String chars = '0' + scanner.nextMap(1,3, MAP_ASCII_OCTAL);
        int value = Integer.parseInt(chars, 8);
        if (value > 255) {
          scanner.walkBack(start);
          throw new Scanner.SyntaxError(scanner, "octal escape subscript out of range; expected 00-0377; have: " + value);
        }
        char out = (char) value;
        return out + Tools.EMPTY_STRING;
      }
      /*integer or pass-through */
      default: {
        if (Char.mapContains(character, MAP_ASCII_NUMBERS)) {
          String chars = character + scanner.nextMap(1,2, MAP_ASCII_NUMBERS);
          int value = Integer.parseInt(chars);
          if (value > 255) {
            scanner.walkBack(start);
            throw new Scanner.SyntaxError(scanner, "integer escape subscript out of range; expected 0-255; have: " + value);
          } else {
            char out = (char) value;
            return out + Tools.EMPTY_STRING;
          }
        } else return String.valueOf(character);
      }
    }
  }
}
