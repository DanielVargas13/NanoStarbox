package box.star.text;

import box.star.io.Streams;
import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TextScannerTest {

  TextScanner x = new TextScanner(Streams.getFileText("src/java/resource/mixed-content-page.html"));

  @Test
  void start(){
    String result;
    result = x.seek(new TextScannerPort<String>("doctype"){
      char[] controlTerms = new char[]{'>'};
      @Override
      public boolean matchBreak(char character) {
        return TextScanner.charListContains(character, controlTerms);
      }
    });
    System.err.println(result + x.expect('>'));
  }


}