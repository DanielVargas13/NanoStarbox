package box.star.text.basic;

import box.star.Tools;
import box.star.contract.NotNull;
import box.star.contract.Nullable;

import static box.star.text.Char.SPACE;

/**
 * <h2>ScannerMethod</h2>
 *
 * <p>This class extends the operational capabilities of the basic text
 * {@link Scanner}.</p>
 * <br>
 * <p>Use this class to perform inline stream extrapolations.</p>
 * <br>
 *
 * <h3>Working Theory</h3>
 *
 * <p>A ScannerMethod is instantiated by an implementation to obtain structured
 * data. The method may be started with optional parameters, and may be cloned
 * for concurrent operations or sub-calls.</p>
 * <br>
 * <p>For example, you want to collect some meta-document-language attributes into a hash-map.
 * What you will do is send a hash map to the method through the text-scanner's
 * {@link Scanner#run(ScannerMethod, Object...)} method, and when the run method
 * calls your {@link #compile(Scanner)} method, you simply parse the data you
 * collected in your buffer, and store it in your attribute-map. To obtain your
 * method parameters you must record them during the Scanner's call to your
 * {@link #start(Scanner, Object[])} method.</p>
 * <br>
 * <p>A method may call other methods, and may also call upon the methods
 * of the scanner during any execution phase of its lifecycle.</p>
 * <br>
 *
 * <h3>lifecycle</h3>
 * <ul>
 * {@link #reset()}, {@link #start(Scanner, Object[])}, {@link #collect(Scanner, char)}, {@link #terminate(Scanner, char)} and {@link #scan(Scanner)}</li>
 * </ul>
 */
public class ScannerMethod implements Cloneable {

  protected String claim;
  protected StringBuilder buffer;
  protected int bufferOffset;

  protected ScannerMethod() {this("TextScannerMethod");}

  protected ScannerMethod(@NotNull String claim) {this.claim = claim;}

  /**
   * Create the character buffer
   *
   * <p><i>
   * Overriding is not recommended.
   * </i></p>
   */
  protected void reset() {
    buffer = new StringBuilder((int) SPACE);
    bufferOffset = -1;
  }

  /**
   * Called by the scanner to signal that a new method call is beginning.
   * <p>
   * if you override this, call the super method to initialize the input buffer.
   * <code>super(scanner, parameters); ... return sourceBuffer</code>
   *
   * @param scanner    the host scanner
   * @param parameters the parameters given by the caller.
   */
  protected void start(@NotNull Scanner scanner, Object[] parameters) {}

  /**
   * <p><i>
   * Overriding is not recommended.
   * </i></p>
   *
   * @return String representation
   */
  @NotNull
  public String toString() { return claim; }

  /**
   * Places the given character on the character buffer at the current position,
   * overwriting the current position.
   * <p>
   * This feature enables incorporation of escape expansions into the current
   * buffer.
   *
   * @param forLastBufferCharacter
   */
  protected void swap(@Nullable char forLastBufferCharacter) {
    if (bufferOffset > -1) buffer.setLength(bufferOffset--);
    buffer.append(forLastBufferCharacter);
    bufferOffset++;
  }

  /**
   * Places the given string on the character buffer at the current position,
   * overwriting the current position.
   * <p>
   * If the string is empty or null, the operation is silently aborted.
   * <p>
   * This feature enables incorporation of escape expansions into the current
   * buffer.
   *
   * @param forLastBufferCharacter
   */
  protected void swap(@Nullable String forLastBufferCharacter) {
    if (bufferOffset > -1) buffer.setLength(bufferOffset--);
    if (forLastBufferCharacter == null || forLastBufferCharacter.equals(Tools.EMPTY_STRING)) return;
    buffer.append(forLastBufferCharacter);
    bufferOffset += forLastBufferCharacter.length();
  }

  /**
   * Add a character to the method buffer.
   *
   * @param scanner
   * @param character
   */
  protected void collect(@NotNull Scanner scanner, char character) {
    buffer.append(character);
    bufferOffset++;
  }

  /**
   * Returns true if the character is zero.
   *
   * @param scanner
   * @param character
   * @return
   */
  protected boolean zeroTerminator(@NotNull Scanner scanner, char character) {
    //pop();
    //backStep(scanner);
    return character == 0;
  }

  /**
   * Return true to break processing at this character position.
   * <p>
   * The default method handles the zero terminator.
   *
   * @param scanner
   * @param character
   * @return false to continue processing.
   */
  protected boolean terminate(@NotNull Scanner scanner, char character) {
    return zeroTerminator(scanner, character);
  }

  /**
   * Return the compiled buffer contents.
   * <p>
   * This method is called after the scanner completes a method call.
   *
   * @param scanner
   * @return the buffer.
   */
  @NotNull
  protected String compile(@NotNull Scanner scanner) {
    return buffer.toString();
  }

  /**
   * <p>Signals whether or not the process should continue reading input.</p>
   *
   * <p>The default method returns true.</p>
   *
   * @param scanner
   * @return true if the TextScanner should read more input.
   */
  protected boolean scan(@NotNull Scanner scanner) { return true; }

  /**
   * Step back the scanner and the buffer by 1 character.
   * <p><i>
   * Overriding is not recommended.
   * </i></p>
   *
   * @param scanner
   */
  protected void backStep(@NotNull Scanner scanner) {
    scanner.back();
    buffer.setLength(bufferOffset--);
  }

  /**
   * Examine the character on the top of the buffer.
   * <p>
   * Works like {@link #pop()}, but doesn't modify the buffer.
   *
   * @return
   */
  protected char current() {
    return buffer.charAt(bufferOffset);
  }

  /**
   * Examine characters on the top of the buffer.
   * <p>
   * Works like {@link #pop(int)}, but doesn't modify the buffer.
   *
   * @param count
   * @return
   */
  protected char[] peek(int count) {
    int offset = Math.max(0, buffer.length() - count);
    return buffer.substring(offset).toCharArray();
  }

  /**
   * Cuts the top character from the top of the buffer.
   *
   * @return the top character on the buffer
   */
  protected char pop() {
    char c = buffer.charAt(bufferOffset--);
    buffer.setLength(bufferOffset);
    return c;
  }

  /**
   * Cut characters from the top of the buffer.
   *
   * @param count the amount of characters to cut
   * @return the characters selected
   */
  protected char[] pop(int count) {
    int offset = Math.max(0, buffer.length() - count);
    char[] c = buffer.substring(offset).toCharArray();
    buffer.setLength(offset);
    bufferOffset = offset - 1;
    return c;
  }

  /**
   * <h2>Clone</h2>
   * <p>Creates a re-entrant-safe-single-state-method, from a given method.</p>
   * <br>
   * <p>The method implementation must honor the reset contract, by configuring
   * itself as a new instance.</p>
   *
   * <p>A default method should not store any runtime values. Runtime values
   * should be applied during {@link #reset()} and
   * {@link #start(Scanner, Object[])}.</p>
   *
   * @return the cloned method instance
   */
  @NotNull
  @Override
  protected ScannerMethod clone() {
    try {
      ScannerMethod clone = (ScannerMethod) super.clone();
      clone.reset();
      return clone;
    }
    catch (CloneNotSupportedException failure) {
      throw new Scanner.Exception("unable to create method object", failure);
    }
  }

}
