package box.star.text;

/**
 * This interface hosts the text scanner method core operating functions.
 */
public interface TextScannerMethod extends TextScanner.CharacterBoundaryControl {

  /**
   * Called by the TextScanner to signal that a new method call is beginning.
   *
   * @param context The safe use interface for context information.
   * @param parameters the parameters given by the caller.
   */
  void beginScanning(TextScannerSafeContext context, Object... parameters);

  /**
   * Signals whether or not the process should continue reading input.
   *
   * @param context The safe use interface for context information.
   * @param input The string buffer.
   * @return true if the TextScanner should read more input.
   */
  boolean continueScanning(TextScannerSafeContext context, StringBuilder input);

  /**
   * Extended Operations Option
   *
   * This method is called when it is again safe to call seek/scan/next on the
   * TextScanner.
   *
   * You can use this feature to create a virtual-pipe-chain.
   *
   * You can also (ideally) pre-process output, if having an exact copy of input
   * data is not relevant for your purpose.
   *
   * @param scanner the TextScanner.
   * @param scanned the input buffer.
   * @return the scanned data as a string.
   */
  String returnScanned(TextScanner scanner, StringBuilder scanned);

}
