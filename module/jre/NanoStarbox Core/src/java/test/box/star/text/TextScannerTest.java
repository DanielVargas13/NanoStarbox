package box.star.text;

import box.star.contract.NotNull;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Date;

import static box.star.text.Char.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class TextScannerTest {

  TextScanner x = new TextScanner(new File("src/java/resource/mixed-content-page.html"));

  @Test void dump(){

  }
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
    System.out.println(result);
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

}