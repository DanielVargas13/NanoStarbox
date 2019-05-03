package box.star.text;

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
        this.bufferLimit = 0;
        this.performBackslashControl = true;
      }
      @Override
      public boolean matchBoundary(char character) {
        return TextScanner.charMapContains(controlTerms, character);
      }
    });
    System.err.println(result + x.scanExact('>'));
  }


}