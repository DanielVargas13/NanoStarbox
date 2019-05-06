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
    result = x.start(new TextScanner.Method(){
      char[] controlTerms = new char[]{META_DOCUMENT_TAG_END};
      /**
       * Return true to break processing on this character.
       *
       * @param scanner
       * @param character
       * @return false to continue processing.
       */
      @Override
      public boolean terminator(@NotNull TextScanner scanner, char character) {
        if (super.terminator(scanner, character)) return true;
        return mapContains(character, controlTerms);
      }
    });
    System.out.println(result);
  }


}