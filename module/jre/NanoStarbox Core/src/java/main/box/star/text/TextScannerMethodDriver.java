package box.star.text;

/**
 * This interface hosts the text scanner method core operating functions.
 */
public interface TextScannerMethodDriver {
  void beginScanning(TextScannerMethodContext context, Object... parameters);
  boolean continueScanning(TextScannerMethodContext context, StringBuilder input);
  String returnScanned(TextScanner scanner, StringBuilder scanned);
}
