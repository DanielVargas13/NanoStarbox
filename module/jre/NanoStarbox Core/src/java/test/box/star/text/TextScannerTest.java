package box.star.text;

import box.star.io.Streams;
import box.star.state.Settings;
import org.junit.jupiter.api.Test;

import static box.star.text.TextScannerTest.DocumentElements.*;
import static java.util.regex.Pattern.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class TextScannerTest {

  enum DocumentElements {
    ELEMENT_START, TAG_CLOSE, WHITESPACE
  }

  Settings patterns = new Settings("HyperText Document Reader");

  void addTextPattern(DocumentElements element, TextPattern pattern){
    patterns.set(element, pattern);
  }

  {
    addTextPattern(ELEMENT_START, new TextPattern("element-header", "[a-z\\s>]", CASE_INSENSITIVE));
    addTextPattern(WHITESPACE, new TextPattern("whitespace", "\\s", CASE_INSENSITIVE));
  }

  TextScanner x = new TextScanner(Streams.getFileText("src/java/resource/mixed-content-page.html"));

  @Test
  void start(){
    scanField();
    scanCharacterMatch();
    scanControl();
    scanSeek();
  }

  void scanField(){
    assertEquals("<!", x.scanField(patterns.get(ELEMENT_START)));
  }

  void scanCharacterMatch(){
    assertEquals("DOCTYPE", x.scanField(patterns.get(WHITESPACE)));
  }

  void scanControl(){
    x.scanControl();
  }
  void scanSeek(){
    assertEquals("html", x.scanSeek(new TextPattern("test", ">", CASE_INSENSITIVE)));
  }

}