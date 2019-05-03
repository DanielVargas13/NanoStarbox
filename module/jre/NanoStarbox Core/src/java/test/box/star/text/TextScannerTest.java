package box.star.text;

import box.star.io.Streams;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TextScannerTest {

  TextScanner x = new TextScanner(Streams.getFileText("src/java/resource/mixed-content-page.html"));

  @Test
  void start(){
    String result;
    result = x.scan(new TextScannerPort("doctype"){
      char[] controlTerms = new char[]{'>'};
      {
        this.max = 0;
      }
      @Override
      public boolean matchBreak(char character) {
        return TextScanner.charListContains(character, controlTerms);
      }
    });
    System.err.println(result + x.scanExact('>'));
  }


}