package box.star.text;

import box.star.contract.NotNull;

public interface VirtualTextScannerMethod<ROOT extends VirtualTextScanner> extends Cloneable {

  String getScopeView(@NotNull ROOT context);

  /**
   * Return true to break processing on this character.
   *
   * @param character
   * @return false to continue processing.
   */
  boolean terminator(@NotNull ROOT context, char character);

  /**
   * Called by the TextScanner to signal that a new method call is beginning.
   *
   * @param parameters the parameters given by the caller.
   */
  void startMethod(@NotNull ROOT context, Object[] parameters);

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
   * @param context the host context.
   * @return the scanned data as a string.
   */
  String computeMethodCall(@NotNull ROOT context);

  /**
   * Signals whether or not the process should continue reading input.
   *
   * @return true if the TextScanner should read more input.
   */
  boolean continueScanning(@NotNull ROOT context);

}
