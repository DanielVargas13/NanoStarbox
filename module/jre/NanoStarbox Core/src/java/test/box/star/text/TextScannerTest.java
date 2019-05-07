package box.star.text;

import box.star.contract.NotNull;
import org.junit.jupiter.api.Test;

import java.io.File;

import static box.star.text.Char.META_DOCUMENT_TAG_END;
import static box.star.text.Char.mapContains;
import static org.junit.jupiter.api.Assertions.assertEquals;

class TextScannerTest {

  TextScanner x = new TextScanner(new File("src/java/resource/mixed-content-page.html"));

  @Test
  void start(){
    String result;

    result = x.nextMapVoid('<');
    assertEquals("", result);

    result = x.run(new TextScanner.Method(){
      char[] terminator = new char[]{META_DOCUMENT_TAG_END};
      @Override
      public boolean terminator(@NotNull TextScanner scanner, char character) {
        if (super.terminator(scanner, character)) return true;
        return mapContains(character, terminator);
      }
      @Override
      public @NotNull String compile(@NotNull TextScanner scanner) {
        return buffer.toString();
      }
    });
    assertEquals("<!DOCTYPE html>", result);
    System.out.println(result);
  }


}