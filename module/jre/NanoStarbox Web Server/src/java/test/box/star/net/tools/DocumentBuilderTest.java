package box.star.net.tools;

import box.star.text.TextScanner;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.PrintStream;
import java.util.*;

import static box.star.text.TextScanner.ASCII.*;

public class DocumentBuilderTest {

  static PrintStream out = System.out;
  static PrintStream  err = System.err;

  static class DocumentTagReader {

    static class TagScannerMethod extends TextScanner.Method {
      public TagScannerMethod() {
        super(META_DOCUMENT_TAG_END);
        boundaryCeption = true;
      }
      @Override
      public boolean exitMethod(char character) {
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
      public boolean exitMethod(char character) {
        if (haveEscapeWarrant() || matchQuote(character)) return false;
        return character == META_DOCUMENT_TAG_END;
      }
    }

    static TagScannerMethod documentTag = new TagScannerMethod();
    static AttributeScannerMethod attributeMethod = new AttributeScannerMethod();

    String source;
    String attributes;
    String tagName, cmpName;

    public DocumentTagReader(TextScanner source) {
      this.source = source.scan(documentTag);
      if (this.source.endsWith(">")) this.attributes = "";
      else this.attributes = source.scan(attributeMethod);
      this.tagName = this.source.substring(1);
      this.cmpName = this.tagName.trim().toLowerCase(Locale.ENGLISH);
    }

    boolean isAnEndTagFor(DocumentTagReader root){
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
    public boolean exitMethod(char character) {
      return character == META_DOCUMENT_TAG_START;
    }
  }

  TextScanner textScanner = new TextScanner(new File("src/java/resource/local/mixed-content-page.html"));
  ContentScannerMethod documentContent = new ContentScannerMethod();
  TextScanner.Method.FindString endTag = new TextScanner.Method.FindString().EscapeQuotes().AnyCase();

  @Test
  void main() {

    // Test class assembly
    CharacterClassAssembly();

    // Test document scanning
    while (textScanner.hasNext()){
      out.print(textScanner.seek(documentContent));
      DocumentTagReader dt = new DocumentTagReader(textScanner);
      if (dt.equals("serve")){
        textScanner.seek(endTag, "</serve>");
        out.print("<!-- Template Data Goes Here -->");
      } else {
        out.print(dt);
      }
    }

    out.flush();

  }

  void CharacterClassAssembly(){
    char[] map = new TextScanner.CharacterClass(9, 13).merge(' ').assemble();
    assert(Arrays.equals(map, MAP_WHITE_SPACE));
    assert(Arrays.equals(new char[]{9,10,11,12,13,32}, MAP_WHITE_SPACE));
  }

}