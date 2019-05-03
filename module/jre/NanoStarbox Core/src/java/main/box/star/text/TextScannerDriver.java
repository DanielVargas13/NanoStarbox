package box.star.text;

public interface TextScannerDriver {
  boolean continueScanning(StringBuilder input, TextScannerContext textScanner);
}
