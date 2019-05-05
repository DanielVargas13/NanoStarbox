package box.star.text;

/**
 * This context hosts all the methods that are safe for access from within
 * a text scanner method.
 */
public interface TextScannerSafeContext {
  RuntimeException claimSyntaxError(String message, Throwable causedBy);
  RuntimeException claimSyntaxError(String message);
  boolean hasNext();
  /**
   * Detects if the scanner has found a backslash-warrant at this position.
   *
   * Actual handling of the warrant must be performed by the calling method.
   *
   * @return true if this character position should start a backslash capture.
   */
  boolean haveEscapeWarrant();
  boolean end();
  long index();
  long line();
  long column();
  String sourceLabel();
}
