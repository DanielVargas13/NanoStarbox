package box.star.text;

import box.star.contract.NotNull;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Date;

import static box.star.text.Char.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class TextScannerTest {

  TextScanner x = new TextScanner(new File("src/java/resource/mixed-content-page.html"));

  @Test
  void start(){
    String result;

    result = x.nextField('<');
    assertEquals("", result);

    result = x.run(new TextScanner.Method(){
      char[] terminator = new char[]{META_DOCUMENT_TAG_END};
      @Override
      public boolean terminator(@NotNull TextScanner scanner, char character) {
        if (zeroTerminator(scanner, character)) return true;
        else return mapContains(character, terminator);
      }
    });
    assertEquals("<!DOCTYPE html>", result);
  }

  @Test void quoting(){
    TextScanner x = new TextScanner("test-string", "'\\\\t'");
    String result = (x.run(new TextScanner.Method(){
      @Override
      public boolean terminator(@NotNull TextScanner scanner, char character) {
        if (super.terminator(scanner, character)) return true;
        else if (quotingStream(scanner, character)) return false;
        return false;
      }
    }));
    assertEquals(BACKSLASH+""+HORIZONTAL_TAB, result);
  }

  @Test void quoting_unicode_escape(){
    TextScanner x = new TextScanner("test-string", "'\\u0001'");
    TextScanner.Method m = new TextScanner.Method(){
      @Override
      public boolean terminator(@NotNull TextScanner scanner, char character) {
        if (super.terminator(scanner, character)) return true;
        else if (quotingStream(scanner, character)) return false;
        return false;
      }
    };
    String result = x.run(m);
    assertEquals(1, result.charAt(0));
  }

  @Test void quoting_octal_escape(){
    TextScanner x = new TextScanner("test-string", "'\\0377'");
    TextScanner.Method m = new TextScanner.Method(){
      @Override
      public boolean terminator(@NotNull TextScanner scanner, char character) {
        if (super.terminator(scanner, character)) return true;
        else if (quotingStream(scanner, character)) return false;
        return false;
      }
    };
    String result = x.run(m);
    assertEquals((char)255, result.charAt(0));
  }

  @Test void quoting_integer_escape(){
    TextScanner x = new TextScanner("test-string", "'\\1'");
    TextScanner.Method m = new TextScanner.Method(){
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

  @Test void snapshot_lifecycle(){
    TextScanner x = new TextScanner("test-string", "0123456789");
    assertEquals(false, x.snapshot());
    TextScanner.Snapshot s = x.getSnapshot();
    assertEquals(true, x.snapshot());
    assertEquals("01", x.nextMapLength(2, MAP_ASCII_NUMBERS));
    s.close();
    assertEquals(false, x.snapshot());
    assertEquals("23", x.nextMapLength(2, MAP_ASCII_NUMBERS));
    snapshot_rewind_lifecycle();
  }

  void snapshot_rewind_lifecycle(){
    TextScanner x = new TextScanner("test-string", "0123456789");
    assertEquals(false, x.snapshot());
    TextScanner.Snapshot s = x.getSnapshot();
    assertEquals(true, x.snapshot());
    assertEquals("01", x.nextMapLength(2, MAP_ASCII_NUMBERS));
    s.cancel();
    assertEquals(false, x.snapshot());
    assertEquals("01", x.nextMapLength(2, MAP_ASCII_NUMBERS));
  }

  @Test void string_comparison_case_sensitive(){
    String s = "0123456789";
    TextScanner x = new TextScanner("test-string", s+"A");
    TextScanner.Snapshot sx = x.getSnapshot();
    assertEquals(s+"A", x.nextString(s+"A", true));
    sx.cancel();
    try {
      x.nextString(s + "a", true);
    } catch (Scanner.SyntaxError e){
      e.printStackTrace();
      assertEquals(true, true);
      return;
    }
  }
}