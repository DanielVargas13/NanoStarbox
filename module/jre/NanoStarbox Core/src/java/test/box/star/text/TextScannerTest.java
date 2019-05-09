package box.star.text;

import box.star.contract.NotNull;
import box.star.text.basic.Scanner;
import box.star.text.basic.ScannerMethod;
import box.star.text.basic.ScannerStateLock;
import org.junit.jupiter.api.Test;

import java.io.File;

import static box.star.text.Char.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TextScannerTest {

  Scanner x = new Scanner(new File("src/java/resource/mixed-content-page.html"));

  @Test
  void general_testing() {
    String result;

    result = x.nextField('<');
    assertEquals("", result);

    result = x.run(new ScannerMethod() {
      char[] terminator = new char[]{META_DOCUMENT_TAG_END};

      @Override
      public boolean terminator(@NotNull Scanner scanner, char character) {
        if (zeroTerminator(scanner, character)) return true;
        else return mapContains(character, terminator);
      }
    });
    assertEquals("<!DOCTYPE html>", result);
  }

  @Test void snapshot_lifecycle() {
    Scanner x = new Scanner("test-string", "0123456789");
    assertEquals(false, x.hasStateLock());
    ScannerStateLock s = x.getStateLock();
    assertEquals(true, x.hasStateLock());
    assertEquals("01", x.nextMapLength(2, MAP_ASCII_NUMBERS));
    s.close();
    assertEquals(false, x.hasStateLock());
    assertEquals("23", x.nextMapLength(2, MAP_ASCII_NUMBERS));
    snapshot_rewind_lifecycle();
  }

  void snapshot_rewind_lifecycle() {
    Scanner x = new Scanner("test-string", "0123456789");
    assertEquals(false, x.hasStateLock());
    ScannerStateLock s = x.getStateLock();
    assertEquals(true, x.hasStateLock());
    assertEquals("01", x.nextMapLength(2, MAP_ASCII_NUMBERS));
    s.back();
    assertEquals(false, x.hasStateLock());
    assertEquals("01", x.nextMapLength(2, MAP_ASCII_NUMBERS));
  }

  @Test void string_comparisons() {
    String s = "0123456789";
    String s2 = s + "A";
    String s3 = s + "a";
    Scanner x = new Scanner("test-string", s2);
    ScannerStateLock sx = x.getStateLock();
    assertEquals(s2, x.nextString(s2, true));
    sx.back();
    try {
      sx = x.getStateLock();
      x.nextString(s3, true);
    }
    catch (Scanner.SyntaxError e) {
      String certification = "Expected A and found a at location = " +
          "{line: 1, column: 11, index: 10, source: 'test-string'}";
      assertEquals(certification, e.getMessage());
      sx.back();
    }
    // finally test case insensitive
    assertEquals(s3, x.nextString(s2, false));
    // this doesn't matter if you are not going to use x.
    sx.close();
  }

}