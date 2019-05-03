package box.star.text;

public class TextScannerPort<EXPECT> implements TextScannerTaskManager, TextScannerDelimiterMatcher {

  private final EXPECT expectation;
  public TextScannerPort(EXPECT expectation){ this.expectation = expectation; }
  @Override public boolean continueScanning(StringBuilder input, TextScannerServicePort textScanner) { return true; }
  @Override public boolean matchBreak(char character) { return character != 0; }
  public String getExpectation() { return String.valueOf(expectation); }

}
