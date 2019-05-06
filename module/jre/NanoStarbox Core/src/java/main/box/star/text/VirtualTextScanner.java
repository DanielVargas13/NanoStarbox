package box.star.text;

import box.star.contract.NotNull;

public interface VirtualTextScanner<ROOT extends VirtualTextScanner<ROOT>> {
  /**
   * Determine if the source string still contains characters that next()
   * can consume.
   * @return true if not yet at the end of the source.
   * @throws Exception thrown if there is an error stepping forward
   *  or backward while checking for more data.
   */
  boolean haveNext() throws Exception;

  /**
   * Checks if the end of the input has been reached.
   *
   * @return true if at the end of the file and we didn't step back
   */
  boolean endOfSource();

  /**
   * Go back one step.
   *
   * @throws Exception
   */
  void back() throws Exception;

  /**
   * Get the next character
   * @return
   * @throws Exception
   */
  char next() throws Exception;

  /**
   * Read the given source or throw an error.
   * @param source
   * @return
   */
  @NotNull String next(@NotNull String source, boolean caseSensitive);

  /**
   * Assemble characters while character match map.
   * @param map
   * @return
   */
  @NotNull String next(char... map);

  /**
   * Get the text up but not including one of the specified delimiter
   * characters or the end of line, whichever comes first.
   * @param delimiters A set of delimiter characters.
   * @return A string, trimmed.
   * @throws Exception Thrown if there is an error while searching
   *  for the delimiter
   */
  @NotNull String scanField(char... delimiters) throws Exception;

  /**
   * Skip characters until the next character is the requested character.
   * If the requested character is not found, no characters are skipped.
   * @param sequence A character or character sequence to match.
   * @return The requested character, or zero if the requested character
   * is not found.
   * @throws Exception Thrown if there is an error while searching
   *  for the to character
   */
  //String scanField(char... sequence) throws Exception;

  /**
   * Return the characters up to the next close quote character.
   * Backslash processing is done. The formal JSON format does not
   * allow strings in single quotes, but an implementation is allowed to
   * accept them.
   * @param quote The quoting character, either
   *      <code>"</code>&nbsp;<small>(double quote)</small> or
   *      <code>'</code>&nbsp;<small>(single quote)</small>.
   * @return      A String.
   * @throws Exception Unterminated string.
   */
  @NotNull String scanQuote(char quote, boolean multiLine) throws Exception;

  @NotNull String readLineWhiteSpace();
  @NotNull String readWhiteSpace();

  /**
   * Returns true if this character is the start of a backslash escape.
   *
   * @return
   */
  boolean haveEscape();

  @NotNull String start(@NotNull VirtualTextScannerMethod<ROOT> method, Object... parameters);

  /**
   * Make a Exception to signal a syntax error.
   *
   * @param message The error message.
   * @return  A Exception object, suitable for throwing
   */
  Exception syntaxError(@NotNull String message);

  /**
   * Make a Exception to signal a syntax error.
   *
   * @param message The error message.
   * @param causedBy The throwable that caused the error.
   * @return  A Exception object, suitable for throwing
   */
  Exception syntaxError(@NotNull String message, @NotNull Throwable causedBy);
  boolean quotedText(char character);
  boolean isQuoting();
}
