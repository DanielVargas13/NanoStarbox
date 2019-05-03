package box.star.text;

import box.star.Tools;
import box.star.contract.Nullable;

public class TextScannerMethod implements TextScannerTaskManager, TextScannerBoundaryFilter {

  private static final String undefined = "undefined";

  protected int max = 0;

  private final String expectation;

  public TextScannerMethod(){this(null);}
  public TextScannerMethod(@Nullable Object expectation){ this.expectation = String.valueOf(Tools.makeNotNull(expectation, undefined)); }
  @Override public boolean continueScanning(StringBuilder input, TextScannerServicePort textScanner) { return true; }
  @Override public boolean matchBoundary(char character) { return character != 0; }
  public String getExpectation() { return expectation; }

}
