package box.star.text.basic;

import box.star.contract.NotNull;
import org.junit.jupiter.api.Test;

import java.io.File;

import static box.star.text.Char.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TextScannerTest {

  @Test
  void general_testing() {
    LegacyScanner x = new LegacyScanner(new File("src/java/resource/mixed-content-page.html"));
    String result;

    result = x.nextField('<');
    assertEquals("", result);

    result = x.run(new LegacyScanner.ScannerMethod() {
      char[] terminator = new char[]{META_DOCUMENT_TAG_END};

      @Override
      public boolean terminate(@NotNull LegacyScanner scanner, char character) {
        if (zeroTerminator(scanner, character)) return true;
        else return mapContains(character, terminator);
      }
    });
    assertEquals("<!DOCTYPE html>", result);
  }

  @Test
  void snapshot_lifecycle() {
    LegacyScanner x = new LegacyScanner("test-string", "0123456789");
    assertEquals(false, x.hasStateRecordLock());
    LegacyScanner.ScannerStateRecord s = x.getStateLock();
    assertEquals(true, x.hasStateRecordLock());
    assertEquals("01", x.nextMap(2, MAP_ASCII_NUMBERS));
    s.free();
    assertEquals(false, x.hasStateRecordLock());
    assertEquals("23", x.nextMap(2, MAP_ASCII_NUMBERS));
    snapshot_rewind_lifecycle();
  }

  void snapshot_rewind_lifecycle() {
    LegacyScanner x = new LegacyScanner("test-string", "0123456789");
    assertEquals(false, x.hasStateRecordLock());
    LegacyScanner.ScannerStateRecord s = x.getStateLock();
    assertEquals(true, x.hasStateRecordLock());
    assertEquals("01", x.nextMap(2, MAP_ASCII_NUMBERS));
    s.restore();
    assertEquals(false, x.hasStateRecordLock());
    assertEquals("01", x.nextMap(2, MAP_ASCII_NUMBERS));
  }

  @Test
  void string_comparisons() {
    String s = "0123456789";
    String s2 = s + "A";
    String s3 = s + "a";
    LegacyScanner x = new LegacyScanner("test-string", s2);
    LegacyScanner.ScannerStateRecord sx = x.getStateLock();
    assertEquals(s2, x.nextString(s2, true));
    sx.restore();
    try {
      sx = x.getStateLock();
      x.nextString(s3, true);
    }
    catch (LegacyScanner.SyntaxError e) {
      String certification = "Expected A and found a at location = " +
          "{line: 1, column: 11, index: 10, source: 'test-string'}";
      assertEquals(certification, e.getMessage());
      sx.restore();
    }
    // finally test case insensitive
    assertEquals(s3, x.nextString(s2, false));
    // this doesn't matter if you are not going to use x.
    sx.free();
  }

  @Test
  void location_tracking() {
    String s = "0123456789";
    LegacyScanner x = new LegacyScanner("test-string", s);
    x.next();
    assertEquals(1, x.getColumn());
    x.back();
    assertEquals(0, x.getColumn());
    System.err.println(x.run(new LegacyScanner.ScannerMethod() {}));
    assertEquals(s.indexOf("9"), x.getIndex());
    assertEquals(s.length(), x.getColumn());
    assertTrue(x.endOfSource());
    x.back();
    x.back();
    x.back();
    assertEquals(s.length() - 3, x.getColumn());
    System.err.println(x.run(new LegacyScanner.ScannerMethod() {
      @Override
      protected @NotNull String compile(@NotNull LegacyScanner scanner) {
        if (scanner.getIndex() == 9) {
          backStep(scanner);
          backStep(scanner);
        }
        return super.compile(scanner);
      }
    }));
    assertEquals(9 - 2, x.getIndex());
  }

}