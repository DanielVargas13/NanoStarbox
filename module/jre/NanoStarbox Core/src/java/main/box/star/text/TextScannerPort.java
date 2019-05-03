package box.star.text;

import box.star.Tools;
import box.star.contract.Nullable;

public class TextScannerPort implements TextScannerTaskManager, TextScannerDelimiterMatcher {

  private static final String undefined = "undefined";

  private final String expectation;

  public TextScannerPort(){this(null);}
  public TextScannerPort(@Nullable Object expectation){ this.expectation = String.valueOf(Tools.makeNotNull(expectation, undefined)); }
  @Override public boolean continueScanning(StringBuilder input, TextScannerServicePort textScanner) { return true; }
  @Override public boolean matchBreak(char character) { return character != 0; }
  public String getExpectation() { return expectation; }

}
