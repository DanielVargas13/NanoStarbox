package box.star.text.basic;

import box.star.text.Char;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ScannerTest {

  @Test void next_char(){
    Scanner scan = new Scanner("next_char", "test");
    assertEquals('t', scan.next('t'));
    assertEquals('e', scan.next('e'));
    scan.back();
    assertEquals('e', scan.next('e'));
    scan.walkBack(-1);
    assertEquals('t', scan.next('t'));
  }

  @Test void next_word(){
    Scanner scanner = new Scanner(Scanner.class.getName(), "next word test");
    assertEquals("next", scanner.nextWord());
    assertEquals(" ", scanner.nextLineSpace());
  }

  @Test void translate_character_map(){
    Scanner scanner = new Scanner(Scanner.class.getName(), "");
    assertEquals("`0', `1', `2', `3', `4', `5', `6', `7', `8' and `9'",
        scanner.translateCharacterMap("and", Char.MAP_ASCII_NUMBERS));
  }

  @Test void get_character_map_runtime_label(){
    Scanner scanner = new Scanner(Scanner.class.getName(), "");
    assertEquals("`0', `1', `2', `3', `4', `5', `6', `7', `8' or `9'",
        scanner.getRuntimeLabel(Char.MAP_ASCII_NUMBERS));
  }

}