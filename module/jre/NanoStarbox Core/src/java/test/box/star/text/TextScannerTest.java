package box.star.text;

import box.star.io.Streams;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TextScannerTest {

  TextScanner x = new TextScanner(new File("src/java/resource/mixed-content-page.html"));

  @Test
  void start(){
    String result;
    result = x.scan(new TextScanner.Method("doctype"){
      char[] controlTerms = new char[]{'>'};
      {
        this.max = 0;
        this.eatEscapes = true;
      }
      @Override
      public boolean matchBoundary(char character) {
        return TextScanner.charMapContains(controlTerms, character);
      }
    });
    System.err.println(result + x.scanExact('>'));
  }


}