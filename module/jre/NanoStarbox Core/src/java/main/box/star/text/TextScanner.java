package box.star.text;

import box.star.contract.NotNull;
import box.star.io.Streams;

import java.io.*;

import static box.star.text.Char.*;

public class TextScanner implements Scanner<TextScanner> {

  /**
   * Reader for the input.
   */
  protected Reader reader;
  protected SerializableState state;

  public TextScanner(@NotNull String path, @NotNull Reader reader) {
    this.reader = reader.markSupported() ? reader : new BufferedReader(reader);
    this.state = new SerializableState(path);
  }
  public TextScanner(@NotNull String path, @NotNull InputStream inputStream) {
    this(path, new InputStreamReader(inputStream));
  }

  public TextScanner(@NotNull String path, @NotNull String s) {
    this(path, new StringReader(s));
  }

  public TextScanner(@NotNull File file) {
    this(file.getPath(), Streams.getFileText(file.getPath()));
  }

  public boolean snapshot() {
    return state.snapshot;
  }

  @Override @NotNull
  public <ANY extends Scanner.Snapshot> ANY getSnapshot() {
    return (ANY) new Snapshot(this);
  }

  /**
   * Determine if the source string still contains characters that next()
   * can consume.
   *
   * @return true if not yet at the end of the source.
   * @throws Exception thrown if there is an error stepping forward
   *                   or backward while checking for more data.
   */
  @Override
  public boolean haveNext() throws Exception {
    if (state.usePrevious) {
      return true;
    }
    try {
      this.reader.mark(1);
    }
    catch (IOException e) {
      throw new Exception("Unable to preserve stream position", e);
    }
    try {
      // -1 is EOF, but next() can not consume the null character '\0'
      if (this.reader.read() <= 0) {
        state.eof = true;
        return false;
      }
      this.reader.reset();
    }
    catch (IOException e) {
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
   * @throws Exception if unable to step backward
   */
  @Override
  public void back() throws Exception {
    if (state.usePrevious || state.index <= 0) {
      throw new Exception("Stepping back two steps is not supported. Try using a snapshot.");
    }
    state.decrementIndexes();
    state.usePrevious = true;
    state.eof = false;
  }

  /**
   * Get the next character.
   *
   * @return
   * @throws Exception if read fails
   */
  @Override
  public char next() throws Exception {
    int c;
    if (state.usePrevious) {
      state.usePrevious = false;
      c = state.previous;
    } else {
      try { c = this.reader.read(); }
      catch (IOException exception) { throw new Exception(exception); }
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
   *
   * @param source
   * @param caseSensitive
   * @return
   * @throws SyntaxError if match fails
   */
  @Override
  @NotNull
  public String nextString(@NotNull String source, boolean caseSensitive) throws SyntaxError {
    StringBuilder out = new StringBuilder();
    char[] sequence = source.toCharArray();
    for (char c : sequence) {
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
        throw this.syntaxError("Expected '" + c + "' and instead saw end of source");
      }
      out.append(test);
    }
    return out.toString();
  }

  /**
   * Scan and assemble characters while scan in map.
   *
   * @param map
   * @return
   * @throws Exception if read fails.
   */
  @Override
  @NotNull
  public String nextMap(@NotNull char... map) throws Exception {
    char c;
    StringBuilder sb = new StringBuilder();
    do {
      c = this.next();
      if (Char.mapContains(c, map)) sb.append(c);
      else {
        this.back();
        break;
      }
    } while (c != 0);
    return sb.toString();
  }

  /**
   * Scan and assemble characters while scan is not in map.
   *
   * @param map
   * @return
   * @throws Exception if read fails.
   */
  @Override
  @NotNull
  public String nextField(@NotNull char... map) throws Exception {
    char c;
    StringBuilder sb = new StringBuilder();
    do {
      c = this.next();
      if (!Char.mapContains(c, map)) sb.append(c);
      else {
        this.back();
        break;
      }
    } while (c != 0);
    return sb.toString();
  }

  /**
   * Get the next n characters.
   *
   * @param n The number of characters to take.
   * @return A string of n characters.
   * @throws Exception Substring bounds error if there are not
   *                   n characters remaining in the source string.
   */
  @NotNull
  @Override
  public String nextLength(int n) throws Exception {
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
  final public boolean parseQuotation(char character) {
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

  @NotNull
  final public String run(@NotNull Char.Scanner.Method<TextScanner> method, Object... parameters) {
    method.reset();
    method.start(this, parameters);
    char c;
    do {
      c = next();
      method.collect(this, c);
    }
    while (!method.terminator(this, c) && method.scanning(this));
    return method.compile(this);
  }

  /**
   * Return the characters up to the next close quote character.
   * Backslash processing is done. The formal JSON format does not
   * allow strings in single quotes, but an implementation is allowed to
   * accept them.
   *
   * @param quote     The quoting character, either
   *                  <code>"</code>&nbsp;<small>(double quote)</small> or
   *                  <code>'</code>&nbsp;<small>(single quote)</small>.
   * @param multiLine if true, quotes span multiple lines.
   * @return A String.
   * @throws Exception Unterminated string.
   */
  @Override @NotNull
  public String nextQuote(char quote, boolean multiLine) throws Exception {
    char c;
    StringBuilder sb = new StringBuilder();
    for (; ; ) {
      c = this.next();
      switch (c) {
        case CARRIAGE_RETURN:
        case LINE_FEED:
          if (multiLine) {
            sb.append(c);
            break;
          }
        case 0:
          throw this.syntaxError("Unterminated string");
        case BACKSLASH:
          c = this.next();
          switch (c) {
            case 'b':
              sb.append('\b');
              break;
            case 't':
              sb.append('\t');
              break;
            case 'n':
              sb.append(LINE_FEED);
              break;
            case 'f':
              sb.append('\f');
              break;
            case 'r':
              sb.append(CARRIAGE_RETURN);
              break;
            case 'u':
              try { sb.append((char) Integer.parseInt(this.nextLength(4), 16)); }
              catch (NumberFormatException e) { throw this.syntaxError("Illegal escape", e); }
              break;
            case DOUBLE_QUOTE:
            case SINGLE_QUOTE:
            case BACKSLASH:
            case SOLIDUS:
              sb.append(c);
              break;
            default:
              throw this.syntaxError("Illegal escape");
          }
          break;
        default:
          if (c == quote) return sb.toString();
          else sb.append(c);
      }
    }
  }

  @Override
  public boolean haveEscape() {
    return state.backslash && state.previous == BACKSLASH;
  }

  /**
   * Make a Exception to signal a syntax error.
   *
   * @param message The error message.
   * @return A Exception object, suitable for throwing
   */
  @Override @NotNull
  public SyntaxError syntaxError(String message) {
    return new SyntaxError(message + this.scope());
  }

  /**
   * Make a Exception to signal a syntax error.
   *
   * @param message  The error message.
   * @param causedBy The throwable that caused the error.
   * @return A Exception object, suitable for throwing
   */
  @Override @NotNull
  public SyntaxError syntaxError(@NotNull String message, @NotNull Throwable causedBy) {
    return new SyntaxError(message + this.scope(), causedBy);
  }

  @Override
  public String scope() {
    return " at " + state.index + " [character " + state.character + " line " +
        state.line + "]";
  }

  @Override
  public String toString() {
    return scope();
  }

  @Override
  public String getPath() {
    return state.path;
  }

  @Override
  public long getIndex() {
    return state.index;
  }

  @Override
  public long getLine() {
    return state.line;
  }

  @Override
  public long getColumn() {
    return state.character;
  }

  public static class Snapshot implements Scanner.Snapshot {
    private TextScanner main;
    private SerializableState state;

    Snapshot(@NotNull TextScanner main) {
      if (main.snapshot())
        throw new Exception("cannot acquire scanner snapshot lock", new IllegalStateException());
      if (! main.haveNext()){
        throw new Exception("the scanner has no data available for this operation", new IllegalStateException());
      }
      this.main = main;
      this.state = main.state.clone();
      try {
        main.reader.mark(1000000);
      }
      catch (IOException e) {
        throw new Exception("failed to set scanner snapshot buffer", e);
      }
      main.state.snapshot = true;
    }

    @Override
    public void cancel() throws Exception {
      if (main == null) return;
      try {
        try {
          main.reader.reset();
          main.state = state;
        }
        catch (IOException e) {
          throw new Exception("failed to cancel snapshot", e);
        }
      }
      finally { close(); }
    }

    @Override
    public void close() {
      if (main == null) return;
      try {
        try { main.reader.mark(1);}
        catch (IOException ignore) {}
      }
      finally {
        this.main.state.snapshot = false;
        this.main = null;
        this.state = null;
      }
    }
  }

  protected static class SerializableState implements Cloneable, Serializable {

    /**
     * previous character read from the input.
     */
    public char previous;
    boolean snapshot;
    String path;
    /**
     * current read character position on the current line.
     */
    long character;
    /**
     * flag to indicate if the end of the input has been found.
     */
    boolean eof;
    /**
     * current read index of the input.
     */
    long index;
    /**
     * current line of the input.
     */
    long line;
    /**
     * flag to indicate that a previous character was requested.
     */
    boolean usePrevious;
    /**
     * the number of characters read in the previous line.
     */
    long characterPreviousLine;

    boolean backslash;
    char methodQuote;

    public SerializableState(String path){
      SerializableState state = this;
      //state.eof = false;
      //state.usePrevious = false;
      //state.previous = 0;
      //state.index = 0;
      state.character = 1;
      //state.characterPreviousLine = 0;
      state.line = 1;
      state.path = path;
      //state.snapshot = false;
      //state.methodQuote = NULL_CHARACTER;
      //state.backslash = false;
    }

    @Override
    protected SerializableState clone() {
      try /*  throwing runtime exceptions with closure */ {
        return (SerializableState) super.clone();
      }
      catch (CloneNotSupportedException e) {throw new RuntimeException(e);}
    }

    /**
     * Decrements the indexes based on the previous character read.
     */
    public void decrementIndexes() {
      this.index--;
      if (this.previous == CARRIAGE_RETURN || this.previous == LINE_FEED) {
        this.line--;
        this.character = this.characterPreviousLine;
      } else if (this.character > 0) {
        if (previous == BACKSLASH) this.backslash = !this.backslash;
        this.character--;
      }
    }

    /**
     * Increments the internal indexes according to the previous character
     * read and the character passed as the current character.
     *
     * @param c the current character read.
     */
    public void incrementIndexes(int c) {
      if (c > 0) {
        if (c == '\\') {
          this.backslash = !this.backslash;
        }
        this.index++;
        if (c == '\r') {
          this.line++;
          this.characterPreviousLine = this.character;
          this.character = 0;
        } else if (c == '\n') {
          if (this.previous != '\r') {
            this.line++;
            this.characterPreviousLine = this.character;
          }
          this.character = 0;
        } else this.character++;
      }
    }

  }

  public static class Method implements Scanner.Method<TextScanner> {

    protected String claim;
    protected StringBuilder buffer;

    public Method() {}

    public Method(@NotNull String claim) {this.claim = claim;}

    /**
     * Create the character buffer
     *
     * <p><i>
     *   Overriding is not recommended.
     * </i></p>
     */
    @Override
    public void reset() {
      buffer = new StringBuilder();
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
    @Override
    public void start(@NotNull TextScanner scanner, Object[] parameters) {}

    /**
     * <p><i>
     *   Overriding is not recommended.
     * </i></p>
     * @return String representation
     */
    @Override
    @NotNull
    public String toString() { return claim; }

    /**
     * Add a character to the method buffer.
     *
     * This super method does not collect backslashes unless the backslash is
     * escaped.
     *
     * @param scanner
     * @param character
     */
    @Override
    public void collect(@NotNull TextScanner scanner, char character) {
      if (character == BACKSLASH && ! scanner.haveEscape()) return;
      buffer.append(character);
    }

    public boolean zeroTerminator(@NotNull TextScanner scanner, char character){
      if (character == 0) {
        if (scanner.haveEscape())
          throw scanner.syntaxError("escaping end of source with a backslash");
        if (scanner.isQuoting())
          throw scanner.syntaxError("failure to find end of quoted text");
        return true;
      }
      return false;
    }

    /**
     * Stored results of last call to parseQuote
     */
    public boolean quoting, escaped;

    public boolean parseQuote(@NotNull TextScanner scanner, char character){
      escaped = scanner.haveEscape();
      quoting = scanner.parseQuotation(character);
      return (escaped || quoting);
    }

    /**
     * Return true to break processing on this character.
     *
     * This super method handles backslash escapes, quoting, and end of source.
     *
     * @param scanner
     * @param character
     * @return false to continue processing.
     */
    @Override
    public boolean terminator(@NotNull TextScanner scanner, char character) {
      if (zeroTerminator(scanner, character)) return true;
      parseQuote(scanner, character);
      return false;
    }

    /**
     * Return the compiled buffer contents.
     *
     * This super method does not return the last character read.
     *
     * @param scanner
     * @return the buffer.
     */
    @Override
    @NotNull
    public String compile(@NotNull TextScanner scanner) {
      back(scanner);
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
    public boolean scanning(@NotNull TextScanner scanner) { return true; }

    /**
     * Step back the scanner and the buffer by 1 character.
     * <p><i>
     *   Overriding is not recommended.
     * </i></p>
     *
     * @param scanner
     */
    @Override
    public void back(@NotNull TextScanner scanner) {
      scanner.back();
      buffer.setLength(Math.max(0, buffer.length() - 1));
    }

    /**
     * Probably shouldn't use this if you are reading this.
     * @return
     */
    @Override
    @NotNull
    public TextScanner.Method clone() {
      try { return (Method) super.clone(); }
      catch (CloneNotSupportedException failure) {
        throw new Exception("unable to create method object", failure);
      }
    }

  }

}
