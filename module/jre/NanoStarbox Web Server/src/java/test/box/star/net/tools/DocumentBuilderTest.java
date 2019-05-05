package box.star.net.tools;

import box.star.text.TextScanner;
import box.star.text.TextScannerMethodContext;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.PrintStream;
import java.util.*;

import static box.star.text.TextScanner.ASCII.*;

public class DocumentBuilderTest {

  static TagScannerMethod documentTag = new TagScannerMethod();
  static AttributeScannerMethod attributeMethod = new AttributeScannerMethod();
  static class DocumentTag {

    String source;
    String attributes;
    String tagName, cmpName;

    public DocumentTag(TextScanner source) {
      this.source = source.scan(documentTag);
      if (this.source.endsWith(">")) this.attributes = "";
      else this.attributes = source.scan(attributeMethod);
      this.tagName = this.source.substring(1);
      this.cmpName = this.tagName.trim().toLowerCase(Locale.ENGLISH);
    }

    boolean isAnEndTagFor(DocumentTag root){
      String cmpName = root.cmpName;
      return this.cmpName.substring(1).equals(cmpName);
    }

    @Override
    public boolean equals(Object obj) {
      String cmpName = String.valueOf(obj).toLowerCase(Locale.ENGLISH);
      return this.cmpName.equals(cmpName);
    }

    @Override
    public String toString() {
      return source + attributes;
    }

  }

  static class TextEndingMatch extends TextScanner.Method {
    private String find;
    char quote, lastCharacter;
    boolean checkMatch;
    int length;
    @Override
    public void beginScanning(TextScannerMethodContext context, Object... parameters) {
      find = String.valueOf(parameters[0]);
      lastCharacter = find.charAt(find.length() - 1);
      length = find.length();
      quote = 0;
      checkMatch = false;
    }
    @Override
    public boolean continueScanning(TextScannerMethodContext context, StringBuilder input) {
      if (checkMatch && input.length() >= length) return ! (quote == 0 && input.toString().endsWith(find));
      return true;
    }
    @Override
    public boolean matchBoundary(TextScannerMethodContext context, char character) {

      if (context.haveEscapeWarrant()) return false;

      switch (quote){
        case SINGLE_QUOTE:{
          if (character == SINGLE_QUOTE) quote = NULL_CHARACTER;
          return false;
        }
        case DOUBLE_QUOTE:{
          if (character == DOUBLE_QUOTE) quote = NULL_CHARACTER;
          return false;
        }
        default:{
          if (character == DOUBLE_QUOTE){
            quote = DOUBLE_QUOTE;
            return false;
          }
          if (character == SINGLE_QUOTE){
            quote = SINGLE_QUOTE;
            return false;
          }
          if (character == lastCharacter) checkMatch = true;
          else checkMatch = false;
          return false;
        }
      }
    }

    @Override
    public String toString() {
      return find;
    }
  }

  static class ContentScannerMethod extends TextScanner.Method {
    public ContentScannerMethod() { super("start of document tag"); }
    @Override
    public boolean matchBoundary(TextScannerMethodContext context, char character) {
      return character == META_DOCUMENT_TAG_START;
    }
  }
  static class TagScannerMethod extends TextScanner.Method {
    char[] WHITE_SPACE = new char[]{9, 10, 11, 12, 13, ' '};
    public TagScannerMethod() {
      super("end of document element header");
      boundaryCeption = true;
    }
    @Override
    public boolean matchBoundary(TextScannerMethodContext context, char character) {
      return (TextScanner.charMapContains(character, WHITE_SPACE)) || (character == META_DOCUMENT_TAG_END);
    }
  }
  static class AttributeScannerMethod extends TextScanner.Method {

    char quote;
    @Override public void beginScanning(TextScannerMethodContext context, Object... parameters) { quote = NULL_CHARACTER; }

    public AttributeScannerMethod() {
      super("attribute scanner");
      boundaryCeption = true;
    }

    @Override
    public boolean matchBoundary(TextScannerMethodContext context, char character) {

      if (context.haveEscapeWarrant()) return false;

      switch (quote){
        case SINGLE_QUOTE:{
          if (character == SINGLE_QUOTE) quote = NULL_CHARACTER;
          return false;
        }
        case DOUBLE_QUOTE:{
          if (character == DOUBLE_QUOTE) quote = NULL_CHARACTER;
          return false;
        }
        default:{
          if (character == DOUBLE_QUOTE){
            quote = DOUBLE_QUOTE;
            return false;
          }
          if (character == SINGLE_QUOTE){
            quote = SINGLE_QUOTE;
            return false;
          }
          return character == META_DOCUMENT_TAG_END;
        }
      }
    }
  }

  TextScanner textScanner = new TextScanner(new File("src/java/resource/local/mixed-content-page.html"));
  ContentScannerMethod documentContent = new ContentScannerMethod();
  TextEndingMatch textEndingMatch = new TextEndingMatch();
  @Test
  void main() {
    PrintStream out = System.out;
    while (textScanner.hasNext()){
      out.print(textScanner.seek(documentContent));
      DocumentTag dt = new DocumentTag(textScanner);
      if (dt.equals("serve")){
        textScanner.seek(textEndingMatch, "</serve>");
        out.print("<!-- Template Data Goes Here -->");
        textScanner.scan(documentTag);
      } else {
        out.print(dt);
      }
      out.flush();
    }
  }

}