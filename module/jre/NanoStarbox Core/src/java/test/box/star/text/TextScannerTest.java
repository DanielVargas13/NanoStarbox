package box.star.text;

import box.star.io.Streams;
import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

import static java.util.regex.Pattern.*;

class TextScannerTest {
  TextScanner x = new TextScanner(Streams.getFileText("src/java/resource/mixed-content-page.html"));

  @Test
  void main(){
   System.err.println(x.scanTextPattern(new TextPattern("doctype","<!doctype .*>", CASE_INSENSITIVE)));
  }

}