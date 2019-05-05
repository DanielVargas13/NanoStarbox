package box.star.net.tools;

import box.star.text.TextScanner;
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
    public String toString() { return source + attributes; }

  }

  static class ContentScannerMethod extends TextScanner.Method {
    public ContentScannerMethod() { super(META_DOCUMENT_TAG_START); }
    @Override
    public boolean matchBoundary(char character) {
      return character == META_DOCUMENT_TAG_START;
    }
  }

  static class TagScannerMethod extends TextScanner.Method {
    public TagScannerMethod() {
      super(META_DOCUMENT_TAG_END);
      boundaryCeption = true;
    }
    @Override
    public boolean matchBoundary(char character) {
      if (TextScanner.charMapContains(character, MAP_WHITE_SPACE)) return true;
      if (character == META_DOCUMENT_TAG_END) return true;
      return false;
    }
  }
  static class AttributeScannerMethod extends TextScanner.Method {
    public AttributeScannerMethod() {
      super("attribute scanner");
      boundaryCeption = true;
    }
    @Override
    public boolean matchBoundary(char character) {
      if (haveEscapeWarrant() || matchQuote(character)) return false;
      return character == META_DOCUMENT_TAG_END;
    }
  }

  TextScanner textScanner = new TextScanner(new File("src/java/resource/local/mixed-content-page.html"));
  ContentScannerMethod documentContent = new ContentScannerMethod();
  TextScanner.FindStringMethod endTag = new TextScanner.FindStringMethod().EscapeQuotes().AnyCase();

  @Test
  void main() {
    PrintStream out = System.out;
    while (textScanner.hasNext()){
      out.print(textScanner.seek(documentContent));
      DocumentTag dt = new DocumentTag(textScanner);
      if (dt.equals("serve")){
        textScanner.seek(endTag, "</serve>");
        out.print("<!-- Template Data Goes Here -->");
      } else {
        out.print(dt);
      }
      out.flush();
    }
  }

  @Test void map(){
    char[] map = new TextScanner.CharacterClass(9, 13).merge(20).assemble();
    for (int c:MAP_WHITE_SPACE) System.out.println(Integer.valueOf(c));
  }
}