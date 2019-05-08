package box.star.text;

import box.star.contract.NotNull;
import org.junit.jupiter.api.Test;

import java.io.File;

import static box.star.text.Char.*;
import static box.star.text.TextScanner.Snapshot;
import static org.junit.jupiter.api.Assertions.assertEquals;

class TextScannerTest {

  TextScanner x = new TextScanner(new File("src/java/resource/mixed-content-page.html"));

  @Test
  void general_testing() {
    String result;

    result = x.nextField('<');
    assertEquals("", result);

    result = x.run(new TextScanner.Method() {
      char[] terminator = new char[]{META_DOCUMENT_TAG_END};

      @Override
      public boolean terminator(@NotNull TextScanner scanner, char character) {
        if (zeroTerminator(scanner, character)) return true;
        else return mapContains(character, terminator);
      }
    });
    assertEquals("<!DOCTYPE html>", result);
    snapshot_lifecycle();
    string_comparisons();
  }

  @Test
  void quoting_test_chain() {
    TextScanner x = new TextScanner("test-string", "'\\\\t'");
    String result = (x.run(new TextScanner.Method() {
      @Override
      public boolean terminator(@NotNull TextScanner scanner, char character) {
        if (super.terminator(scanner, character)) return true;
        else if (quotingStream(scanner, character)) return false;
        return false;
      }
    }));
    assertEquals(Char.toString(BACKSLASH, HORIZONTAL_TAB), result);
    quoting_unicode_escape();
  }

  void quoting_unicode_escape() {
    TextScanner x = new TextScanner("test-string", "'\\u0001'");
    TextScanner.Method m = new TextScanner.Method() {
      @Override
      public boolean terminator(@NotNull TextScanner scanner, char character) {
        if (super.terminator(scanner, character)) return true;
        else if (quotingStream(scanner, character)) return false;
        return false;
      }
    };
    String result = x.run(m);
    assertEquals(1, result.charAt(0));
    quoting_octal_escape();
  }

  void quoting_octal_escape() {
    TextScanner x = new TextScanner("test-string", "'\\0377'");
    TextScanner.Method m = new TextScanner.Method() {
      @Override
      public boolean terminator(@NotNull TextScanner scanner, char character) {
        if (super.terminator(scanner, character)) return true;
        else if (quotingStream(scanner, character)) return false;
        return false;
      }
    };
    String result = x.run(m);
    assertEquals((char) 255, result.charAt(0));
    quoting_integer_escape();
  }

  void quoting_integer_escape() {
    TextScanner x = new TextScanner("test-string", "'\\1'");
    TextScanner.Method m = new TextScanner.Method() {
      @Override
      public boolean terminator(@NotNull TextScanner scanner, char character) {
        if (super.terminator(scanner, character)) return true;
        else if (quotingStream(scanner, character)) return false;
        return false;
      }
    };
    String result = x.run(m);
    assertEquals('\1', result.charAt(0));
  }

  void snapshot_lifecycle() {
    TextScanner x = new TextScanner("test-string", "0123456789");
    assertEquals(false, x.snapshot());
    Snapshot s = x.getSnapshot();
    assertEquals(true, x.snapshot());
    assertEquals("01", x.nextMapLength(2, MAP_ASCII_NUMBERS));
    s.close();
    assertEquals(false, x.snapshot());
    assertEquals("23", x.nextMapLength(2, MAP_ASCII_NUMBERS));
    snapshot_rewind_lifecycle();
  }

  void snapshot_rewind_lifecycle() {
    TextScanner x = new TextScanner("test-string", "0123456789");
    assertEquals(false, x.snapshot());
    Snapshot s = x.getSnapshot();
    assertEquals(true, x.snapshot());
    assertEquals("01", x.nextMapLength(2, MAP_ASCII_NUMBERS));
    s.back();
    assertEquals(false, x.snapshot());
    assertEquals("01", x.nextMapLength(2, MAP_ASCII_NUMBERS));
  }

  void string_comparisons() {
    String s = "0123456789";
    String s2 = s + "A";
    String s3 = s + "a";
    TextScanner x = new TextScanner("test-string", s2);
    Snapshot sx = x.getSnapshot();
    assertEquals(s2, x.nextString(s2, true));
    sx.back();
    try {
      sx = x.getSnapshot();
      x.nextString(s3, true);
    }
    catch (Scanner.SyntaxError e) {
      String certification = "Expected A and found a at location = " +
          "{line: 1, column: 12, index: 11, source: 'test-string'}";
      assertEquals(certification, e.getMessage());
      sx.back();
    }
    // finally test case insensitive
    assertEquals(s3, x.nextString(s2, false));
    // this doesn't matter if you are not going to use x.
    sx.close();
  }

}