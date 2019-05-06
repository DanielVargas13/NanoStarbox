package box.star.text;

import box.star.contract.NotNull;
import box.star.io.Streams;

import java.io.*;

import static box.star.text.Char.*;

public class TextScanner implements VirtualTextScanner<TextScanner> {

  /** Reader for the input. */
  protected Reader reader;
  protected SerializableState state = new SerializableState();

  public TextScanner(@NotNull String path, @NotNull Reader reader) {
    this.reader = reader.markSupported() ? reader : new BufferedReader(reader);
    state.eof = false;
    state.usePrevious = false;
    state.previous = 0;
    state.index = 0;
    state.character = 1;
    state.characterPreviousLine = 0;
    state.line = 1;
    state.path = path;
  }

  public TextScanner(@NotNull String path, @NotNull InputStream inputStream) {
    this(path, new InputStreamReader(inputStream));
  }

  public TextScanner(@NotNull String path, @NotNull String s) {
    this(path, new StringReader(s));
  }

  public TextScanner(File file) {
    this(file.getPath(), Streams.getFileText(file.getPath()));
  }

  /**
   * Determine if the source string still contains characters that next()
   * can consume.
   * @return true if not yet at the end of the source.
   * @throws Exception thrown if there is an error stepping forward
   *  or backward while checking for more data.
   */
  @Override
  public boolean haveNext() throws Exception {
    if(state.usePrevious) {
      return true;
    }
    try {
      this.reader.mark(1);
    } catch (IOException e) {
      throw new Exception("Unable to preserve stream position", e);
    }
    try {
      // -1 is EOF, but next() can not consume the null character '\0'
      if(this.reader.read() <= 0) {
        state.eof = true;
        return false;
      }
      this.reader.reset();
    } catch (IOException e) {
      throw new Exception("Unable to read the next character from the stream", e);
    }
    return true;
  }

  /**
   * Checks if the end of the input has been reached.
   *
   * @return true if at the end of the file and we didn't step back
   */
  @Override
  public boolean endOfSource() {
    return state.eof && !state.usePrevious;
  }

  /**
   * Step backward one position.
   *
   * @throws Exception
   */
  @Override
  public void back() throws Exception {
    if (state.usePrevious || state.index <= 0) {
      throw new Exception("Stepping back two steps is not supported");
    }
    state.decrementIndexes();
    state.usePrevious = true;
    state.eof = false;
  }

  /**
   * Get the next character.
   *
   * @return
   * @throws Exception
   */
  @Override
  public char next() throws Exception {
    int c;
    if (state.usePrevious) {
      state.usePrevious = false;
      c = state.previous;
    } else {
      try {
        c = this.reader.read();
      } catch (IOException exception) {
        throw new Exception(exception);
      }
    }
    if (c <= 0) { // End of stream
      state.eof = true;
      return 0;
    }
    state.incrementIndexes(c);
    state.previous = (char) c;
    return state.previous;
  }

  /**
   * Match the next string input with a source string.
   * @param source
   * @param caseSensitive
   * @return
   */
  @Override
  @NotNull public String next(@NotNull String source, boolean caseSensitive) {
    StringBuilder out = new StringBuilder();
    char[] sequence = source.toCharArray();
    for (char c: sequence) {
      char test = next();
      if (!caseSensitive) {
        c = Character.toLowerCase(c);
        test = Character.toLowerCase(test);
      }
      if (test != c) {
        if (test > 0) {
          throw this.syntaxError("Expected '" + c + "' and instead saw '" +
              test + "'");
        }
        throw this.syntaxError("Expected '" + c + "' and instead saw ''");
      }
      out.append(test);
    }
    return out.toString();
  }

  /**
   * Assemble characters while characters match map.
   *
   * @param map
   * @return
   */
  @Override
  @NotNull public String next(char... map) {
    char c;
    StringBuilder sb = new StringBuilder();
    for (;;) {
      c = this.next();
      if (Char.mapContains(c, map)) {
        if (c != 0) {
          this.back();
        }
        return sb.toString().trim();
      }
      sb.append(c);
    }
  }

  /**
   * Get the next n characters.
   *
   * @param n     The number of characters to take.
   * @return      A string of n characters.
   * @throws Exception
   *   Substring bounds error if there are not
   *   n characters remaining in the source string.
   */
  @NotNull private String next(int n) throws Exception {
    if (n == 0) {
      return "";
    }
    char[] chars = new char[n];
    int pos = 0;
    while (pos < n) {
      chars[pos] = this.next();
      if (this.endOfSource()) {
        throw this.syntaxError("Substring bounds error");
      }
      pos += 1;
    }
    return new String(chars);
  }

  @Override
  final public boolean isQuoting() {
    return state.methodQuote != NULL_CHARACTER;
  }

  @Override
  final public boolean quotedText(char character) {
    // handle quoting
    if (isQuoting()) {
      // deactivate quoting if applicable
      if (character == state.methodQuote) state.methodQuote = NULL_CHARACTER;
      return true;
    }
    // activate quoting if applicable
    if (character == DOUBLE_QUOTE || character == SINGLE_QUOTE) {
      state.methodQuote = character;
      return true;
    }
    return false;
  }

  @NotNull final public String start(@NotNull VirtualTextScannerMethod<TextScanner> method, Object... parameters){
    method.startMethod(this, parameters);
    do { if (method.terminator(this, next())) break; }
    while (method.continueScanning(this));
    return method.computeMethodCall(this);
  }

//  /**
//   * Get the next char in the string, skipping whitespace.
//   * @throws Exception Thrown if there is an error reading the source string.
//   * @return  A character, or 0 if there are no more characters.
//   */
//  public char nextClean() throws Exception {
//    for (;;) {
//      char c = this.next();
//      if (c == 0 || c > ' ') {
//        return c;
//      }
//    }
//  }

//  /**
//   * Get the text up but not including the specified character or the
//   * end of line, whichever comes first.
//   * @param  delimiter A delimiter character.
//   * @return   A string.
//   * @throws Exception Thrown if there is an error while searching
//   *  for the delimiter
//   */
//  @Override
//  public String nextTo(char delimiter) throws Exception {
//    StringBuilder sb = new StringBuilder();
//    for (;;) {
//      char c = next();
//      if (c == delimiter || c == 0 || c == '\n' || c == '\r') {
//        if (c != 0) {
//          this.back();
//        }
//        return sb.toString().trim();
//      }
//      sb.append(c);
//    }
//  }


//  /**
//   * Get the text up but not including one of the specified delimiter
//   * characters or the end of line, whichever comes first.
//   * @param delimiters A set of delimiter characters.
//   * @return A string, trimmed.
//   * @throws Exception Thrown if there is an error while searching
//   *  for the delimiter
//   */
//  @Override
//  private String nextTo(String delimiters) throws Exception {
//    char c;
//    StringBuilder sb = new StringBuilder();
//    for (;;) {
//      c = this.next();
//      if (delimiters.indexOf(c) >= 0 || c == 0 || c == '\n' || c == '\r') {
//        if (c != 0) {
//          this.back();
//        }
//        return sb.toString().trim();
//      }
//      sb.append(c);
//    }
//  }

    /**
   * Get the text up but not including one of the specified delimiter
   * characters or the end of line, whichever comes first.
   * @param delimiters A set of delimiter characters.
   * @return A string, trimmed.
   * @throws Exception Thrown if there is an error while searching
   *  for the delimiter
   */
  @Override
  public String scanField(char... delimiters) throws Exception {
    char c;
    StringBuilder sb = new StringBuilder();
    for (;;) {
      c = this.next();
      if (Char.mapContains(c, delimiters) || c == 0 || c == '\n' || c == '\r') {
        if (c != 0) {
          this.back();
        }
        return sb.toString().trim();
      }
      sb.append(c);
    }
  }

//  /**
//   * Skip characters until the next character is the requested character.
//   * If the requested character is not found, no characters are skipped.
//   * @param sequence An exact termination sequence to match.
//   * @return The requested match, or zero if the requested sequence
//   * does not match.
//   * @throws Exception Thrown if there is an error while searching
//   *  for the sequence character
//   */
//  @Override
//  public String scanField(char... sequence) throws Exception {
//    return "";
//  }

//  @Override
//  public String nextNot(char[] delimiters) throws Exception {
//    char c;
//    StringBuilder sb = new StringBuilder();
//    for (;;) {
//      c = this.next();
//      if (! Char.mapContains(c, delimiters) || c == 0 || c == '\n' || c == '\r') {
//        if (c != 0) {
//          this.back();
//        }
//        return sb.toString().trim();
//      }
//      sb.append(c);
//    }
//  }

//  /**
//   * Skip characters until the next character is the requested character.
//   * If the requested character is not found, no characters are skipped.
//   * @param to A character to skip to.
//   * @return The requested character, or zero if the requested character
//   * is not found.
//   * @throws Exception Thrown if there is an error while searching
//   *  for the to character
//   */
//  public char SEEK(char to) throws Exception {
//    char c;
//    try {
//      SerializableState backup = state.clone();
//      this.reader.mark(1000000);
//      do {
//        c = next();
//        if (c == 0) {
//          // in some readers, reset() may throw an exception if
//          // the remaining portion of the input is greater than
//          // the mark size (1,000,000 above).
//          reader.reset();
//          this.state = backup;
//          return 0;
//        }
//      } while (c != to);
//      this.reader.mark(1);
//    } catch (IOException exception) {
//      throw new Exception(exception);
//    }
//    this.back();
//    return c;
//  }
//
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
  @Override
  public String scanQuote(char quote) throws Exception {
    char c;
    StringBuilder sb = new StringBuilder();
    for (;;) {
      c = this.next();
      switch (c) {
        case 0:
        case '\n':
        case '\r':
          throw this.syntaxError("Unterminated string");
        case '\\':
          c = this.next();
          switch (c) {
            case 'b':
              sb.append('\b');
              break;
            case 't':
              sb.append('\t');
              break;
            case 'n':
              sb.append('\n');
              break;
            case 'f':
              sb.append('\f');
              break;
            case 'r':
              sb.append('\r');
              break;
            case 'u':
              try {
                sb.append((char)Integer.parseInt(this.next(4), 16));
              } catch (NumberFormatException e) {
                throw this.syntaxError("Illegal escape", e);
              }
              break;
            case '"':
            case '\'':
            case '\\':
            case '/':
              sb.append(c);
              break;
            default:
              throw this.syntaxError("Illegal escape.");
          }
          break;
        default:
          if (c == quote) {
            return sb.toString();
          }
          sb.append(c);
      }
    }
  }

  @Override
  public boolean characterQuoting() {
    return false;
  }

  @Override
  public boolean haveEscape() {
    return state.backslash && state.previous == '\\';
  }

  /**
   * Make a Exception to signal a syntax error.
   *
   * @param message The error message.
   * @return  A Exception object, suitable for throwing
   */
  @Override
  public Exception syntaxError(String message) {
    return new SyntaxError(message + this.toString());
  }

  /**
   * Make a Exception to signal a syntax error.
   *
   * @param message The error message.
   * @param causedBy The throwable that caused the error.
   * @return  A Exception object, suitable for throwing
   */
  @Override
  public Exception syntaxError(String message, Throwable causedBy) {
    return new SyntaxError(message + this.toString(), causedBy);
  }

  /**
   * Make a printable string of this JSONTokener.
   *
   * @return " at {index} [character {character} line {line}]"
   */
  @Override
  public String toString() {
    return " at " + state.index + " [character " + state.character + " line " +
        state.line + "]";
  }

  public static class SerializableState implements Cloneable, Serializable {

    String path;
    /** current read character position on the current line. */
    long character;
    /** flag to indicate if the end of the input has been found. */
    boolean eof;
    /** current read index of the input. */
    long index;
    /** current line of the input. */
    long line;
    /** previous character read from the input. */
    public char previous;
    /** flag to indicate that a previous character was requested. */
    boolean usePrevious;
    /** the number of characters read in the previous line. */
    long characterPreviousLine;

    boolean backslash;
    char methodQuote;

    @Override
    protected SerializableState clone() {
      try /*  throwing runtime exceptions with closure */ {
        return (SerializableState)super.clone();
      } catch (CloneNotSupportedException e){throw new RuntimeException(e);}
      finally /*  complete */ {
        ;
      }
    }

    /**
     * Decrements the indexes based on the previous character read.
     */
    public void decrementIndexes() {
      this.index--;
      if(this.previous=='\r' || this.previous == '\n') {
        this.line--;
        this.character=this.characterPreviousLine ;
      } else if(this.character > 0){
        if (previous=='\\') this.backslash = ! this.backslash;
        this.character--;
      }
    }

    /**
     * Increments the internal indexes according to the previous character
     * read and the character passed as the current character.
     * @param c the current character read.
     */
    public void incrementIndexes(int c) {
      if(c > 0) {
        if (c =='\\'){
          this.backslash = ! this.backslash;
        }
        this.index++;
        if(c=='\r') {
          this.line++;
          this.characterPreviousLine = this.character;
          this.character=0;
        }else if (c=='\n') {
          if(this.previous != '\r') {
            this.line++;
            this.characterPreviousLine = this.character;
          }
          this.character=0;
        } else {
          this.character++;
        }
      }
    }


  }

  public static class Method<FOR extends VirtualTextScanner> implements VirtualTextScannerMethod<FOR> {

    protected String claim;
    protected StringBuilder buffer;

    /**
     * Called by the scanner to signal that a new method call is beginning.
     *
     * if you override this, call the super method to initialize the input buffer.
     * <code>super(scanner, parameters); ... return sourceBuffer</code>
     * @param scanner the host scanner
     * @param parameters the parameters given by the caller.
     */
    @Override
    public void startMethod(@NotNull FOR scanner, Object[] parameters) {
      buffer = new StringBuilder();
    }

    @Override
    @NotNull public String toString() { return "ScanMethod "+claim; }

    @Override
    @NotNull public String getScopeView(@NotNull FOR virtualSourceScanner) {
      return claim;
    }

    /**
     * Return true to break processing on this character.
     *
     * @param scanner
     * @param character
     * @return false to continue processing.
     */
    @Override
    public boolean terminator(@NotNull FOR scanner, char character) {
      buffer.append(character);
      if (scanner.haveEscape()) return false;
      else if (scanner.quotedText(character)) return false;
      return character == 0;
    }

    /**
     * <p>Extended Operations Option</p>
     *
     * <p>This method is called when it is again safe to call seek/scan/next on the
     * TextScanner.</p>
     *
     * <p>You can use this feature to create a virtual-pipe-chain.</p>
     *
     * <p>The default method returns the sourceBuffer as string.</p>
     *
     * <p>You can also (ideally) pre-process output, if having an exact copy of input
     * data is not relevant for your purpose.</p>
     *
     * @param scanner the TextScanner.
     * @return the scanned data as a string.
     */
    @Override
    @NotNull public String computeMethodCall(@NotNull FOR scanner) {
      scanner.back();
      buffer.setLength(buffer.length() - 1);
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
    @Override
    public boolean continueScanning(@NotNull FOR scanner) {
      return true;
    }

    public int matchStringIndex(@NotNull String check){
      return buffer.indexOf(check);
    }

    public boolean matchClass(char c, @NotNull char... map){
      return mapContains(c, map);
    }

    @Override
    @NotNull public TextScanner.Method clone() {
      try /* bake-cookies ignoring exceptions with final closure */ {
        return (Method) super.clone();
      }
      catch (CloneNotSupportedException fatal) {throw new Exception("unable to create object copy", fatal);}
    }

  }
}
