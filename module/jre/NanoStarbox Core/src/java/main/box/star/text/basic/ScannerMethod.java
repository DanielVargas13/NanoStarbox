package box.star.text.basic;

import box.star.contract.NotNull;
import box.star.contract.Nullable;
import box.star.text.Char;

import static box.star.text.Char.*;

/**
 * ScannerMethod
 *
 * Extends the operational capabilities of {@link Scanner}
 *
 * Use ScannerMethods to perform inline stream extrapolations.
 *
 */
public class ScannerMethod implements Cloneable {

  /**
   * Stored results of last call to parseQuote
   */
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
    bufferOffset = - 1;
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
   * The expand pass-through-method.
   *
   * The recommended override for systematic performance integrity of the expand
   * command.
   *
   * @param sanner
   * @param c
   * @return
   */
  protected String tryExpand(@NotNull Scanner sanner, char c){ return Char.toString(c); }

  /**
   * Performs all right-hand-side-ampersand operations.
   *
   * (for this: right-hand-side = "everything after")
   *
   * the default method expands nothing.
   *
   * @param scanner
   */
  protected String expandAmpersand(@NotNull Scanner scanner){
    return "";
  }

  /**
   * Performs all right-hand-side-backslash operations.
   *
   * (for this: right-hand-side = "everything after")
   *
   * escaping `&' will call {@link #expandAmpersand(Scanner)}.
   *
   * @param scanner
   * @param character
   * @return
   */
  @Nullable
  protected String expand(@NotNull Scanner scanner, char character){
    switch (character){
      case '&': return expandAmpersand(scanner);
      case 'd': return DELETE+"";
      case 'e': return ESCAPE+"";
      case 't': return "\t";
      case 'b': return "\b";
      case 'v': return VERTICAL_TAB+"";
      case 'r': return "\r";
      case 'n': return "\n";
      case 'f': return "\f";
      /*unicode*/ case 'u': {
        try { return String.valueOf((char) Integer.parseInt(scanner.nextMapLength(4, MAP_ASCII_HEX), 16)); }
        catch (NumberFormatException e) { throw scanner.syntaxError("Illegal escape", e); }
      }
      /*hex or octal*/ case '0': {
        char c = scanner.next();
        if (c == 'x'){
          try { return String.valueOf((char) Integer.parseInt(scanner.nextMapLength(4, MAP_ASCII_HEX), 16)); }
          catch (NumberFormatException e) { throw scanner.syntaxError("Illegal escape", e); }
        } else {
          scanner.back();
        }
        String chars = '0'+scanner.nextMapLength(3, MAP_ASCII_OCTAL);
        int value = Integer.parseInt(chars, 8);
        if (value > 255){
          throw scanner.syntaxError("octal escape subscript out of range; expected 00-0377; have: "+value);
        }
        char out = (char) value;
        return out+"";
      }
      /*integer or pass-through */ default: {
        if (mapContains(character, MAP_ASCII_NUMBERS)){
          String chars = character + scanner.nextMapLength(2, MAP_ASCII_NUMBERS);
          int value = Integer.parseInt(chars);
          if (value > 255){
            throw scanner.syntaxError("integer escape subscript out of range; expected 0-255; have: "+value);
          } else {
            char out = (char)value;
            return out+"";
          }
        } else return tryExpand(scanner, character);
      }
    }
  }

  /**
   *  Places the given character on the character buffer at the current position,
   *  overwriting the current position.
   *
   * This feature enables incorporation of escape expansions into the current
   * buffer.
   *
   * @param forLastBufferCharacter
   */
  protected void swap(@Nullable char forLastBufferCharacter){
    if (bufferOffset > -1) buffer.setLength(bufferOffset--);
    buffer.append(forLastBufferCharacter);
  }

  /**
   * Places the given string on the character buffer at the current position,
   * overwriting the current position.
   *
   * If the string is empty or null, the operation is silently aborted.
   *
   * This feature enables incorporation of escape expansions into the current
   * buffer.
   *
   * @param forLastBufferCharacter
   */
  protected void swap(@Nullable String forLastBufferCharacter){
    if (bufferOffset > -1) buffer.setLength(bufferOffset--);
    if (forLastBufferCharacter == null || forLastBufferCharacter.equals("")) return;
    buffer.append(forLastBufferCharacter);
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
    return character == 0;
  }

  /**
   * Return true to break processing at this character position.
   *
   * The default method handles the zero terminator.
   *
   * @param scanner
   * @param character
   * @return false to continue processing.
   */
  protected boolean terminator(@NotNull Scanner scanner, char character) {
    return zeroTerminator(scanner, character);
  }

  /**
   * Return the compiled buffer contents.
   *
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
  protected boolean scanning(@NotNull Scanner scanner) { return true; }

  /**
   * Step back the scanner and the buffer by 1 character.
   * <p><i>
   * Overriding is not recommended.
   * </i></p>
   *
   * @param scanner
   */
  protected void back(@NotNull Scanner scanner) {
    scanner.back();
    buffer.setLength(bufferOffset--);
  }

  /**
   * Examine the character on the top of the buffer.
   *
   * Works like {@link #pop()}, but doesn't modify the buffer.
   *
   * @return
   */
  protected char peek(){
    return buffer.charAt(bufferOffset);
  }

  /**
   * Examine characters on the top of the buffer.
   *
   * Works like {@link #pop(int)}, but doesn't modify the buffer.
   *
   * @param count
   * @return
   */
  protected char[] peek(int count){
    int offset = Math.max(0, buffer.length() - count);
    return buffer.substring(offset).toCharArray();
  }

  /**
   * Cuts the top character from the top of the buffer.
   *
   * @return the top character on the buffer
   */
  protected char pop(){
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
  protected char[] pop(int count){
    int offset = Math.max(0, buffer.length() - count);
    char[] c = buffer.substring(offset).toCharArray();
    buffer.setLength(offset);
    bufferOffset = offset - 1;
    return c;
  }

  /**
   * Probably shouldn't use this if you are reading this.
   *
   * @return
   */
  @NotNull @Override
  protected ScannerMethod clone() {
    try { return (ScannerMethod) super.clone(); }
    catch (CloneNotSupportedException failure) {
      throw new Scanner.Exception("unable to create method object", failure);
    }
  }

}
