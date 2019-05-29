package box.star.text.basic;

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

}