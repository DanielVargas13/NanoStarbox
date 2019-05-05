package box.star.text;

import box.star.Tools;
import box.star.contract.Nullable;
import box.star.io.SourceConnector;
import box.star.state.SuperTokenMap;

import javax.naming.OperationNotSupportedException;
import java.io.*;
import java.net.URI;
import java.net.URL;
import java.util.Iterator;
import java.util.Locale;
import java.util.Stack;
import java.util.regex.Pattern;

import static box.star.text.Char.*;

public class TextScanner implements Iterable<Character>, Closeable {

  /**
   * Reader for the input.
   */
  private final Reader reader;
  private boolean backslashModeActive;
  /**
   * current read character position on the current line.
   */
  private long column;
  /**
   * flag to indicate if the end of the input has been found.
   */
  private boolean eof;
  /**
   * current read index of the input.
   */
  private long index;
  /**
   * current line of the input.
   */
  private long line;
  /**
   * previous character read from the input.
   */
  private char previous;
  /**
   * flag to indicate that a previous character was requested.
   */
  private boolean usePrevious;
  /**
   * the number of characters read in the previous line.
   */
  private long characterPreviousLine;
  private boolean closeReader, closed;
  private String sourceLabel;
  private boolean seeking;

  /**
   * Construct a TextScanner from a Reader. The caller must close the Reader.
   *
   * @param sourceLabel a label for this text processor such as a file or url.
   * @param reader      A reader.
   */
  public TextScanner(@Nullable String sourceLabel, Reader reader) {
    this.sourceLabel = sourceLabel;
    this.reader = reader.markSupported()
        ? reader
        : new BufferedReader(reader);
    this.eof = false;
    this.usePrevious = false;
    this.previous = 0;
    this.index = 0;
    this.column = 1;
    this.characterPreviousLine = 0;
    this.line = 1;
  }
  public TextScanner(URL source) {
    this(source.getPath(), SourceConnector.getRuntimeFileOrUrlInputStream(source.toString()));
    this.closeReader = true;
  }
  public TextScanner(URI source) {
    this(source.getPath(), SourceConnector.getRuntimeFileOrUrlInputStream(source.toString()));
    this.closeReader = true;
  }
  public TextScanner(File source) {
    this(source.getPath(), SourceConnector.getRuntimeFileOrUrlInputStream(source.getPath()));
    this.closeReader = true;
  }
  /**
   * Construct a TextScanner from an InputStream. The caller must close the input stream.
   *
   * @param inputStream The source.
   */
  public TextScanner(InputStream inputStream) {
    this(inputStream.getClass().getName(), new InputStreamReader(inputStream));
  }

  /**
   * Construct a TextScanner from an InputStream. The caller must close the input stream.
   *
   * @param sourceLabel The label for the source.
   * @param inputStream The source.
   */
  public TextScanner(String sourceLabel, InputStream inputStream) {
    this(sourceLabel, new InputStreamReader(inputStream));
  }

  /**
   * Construct a TextScanner from a string.
   *
   * @param source A source string.
   */
  public TextScanner(String source) {
    this(source.getClass().getName(), new StringReader(source));
    this.closeReader = true;
  }

  /**
   * Construct a TextScanner from a string.
   *
   * @param source A source string.
   */
  public TextScanner(String sourceLabel, String source) {
    this(sourceLabel, new StringReader(source));
  }


  public String sourceLabel() {
    return sourceLabel;
  }

  public long column() {
    return column;
  }

  public long line() {
    return line;
  }

  public long index() {
    return index;
  }

  /**
   * Back up one character. This provides a sort of lookahead capability,
   * so that you can test for a digit or letter before attempting to parse
   * the next number or identifier.
   *
   * @throws Exception Thrown if trying to step back more than 1 step
   *                   or if already at the start of the string
   */
  protected void back() throws Exception {
    if (this.usePrevious || this.index <= 0) {
      throw new Exception("Stepping back two steps is not supported");
    }
    this.decrementIndexes();
    this.usePrevious = true;
    this.eof = false;
  }

  /**
   * Decrements the indexes for the {@link #back()} method based on the previous character read.
   */
  private void decrementIndexes() {
    this.index--;
    if (this.previous == '\r' || this.previous == '\n') {
      this.line--;
      this.column = this.characterPreviousLine;
    } else if (this.column > 0) {
      if (this.previous == '\\') this.backslashModeActive = !this.backslashModeActive;
      else this.backslashModeActive = false;
      this.column--;
    }
  }

  /**
   * Checks if the end of the input has been reached.
   *
   * @return true if at the end of the file and we didn't step back
   */
  public boolean end() {
    return this.eof && !this.usePrevious;
  }

  /**
   * Determine if the next character is a match with a specified character.
   *
   * @return true if not yet at the end of the source.
   * @throws Exception thrown if there is an error stepping forward
   *                   or backward while checking for more data.
   */
  public boolean hasNext() throws Exception {
    if (this.usePrevious) {
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
        this.eof = true;
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
   * Get the next character in the source string.
   *
   * @return The next character, or 0 if past the end of the source string.
   * @throws Exception Thrown if there is an error reading the source string.
   */
  public char scanNext() throws Exception {
    int c;
    if (this.usePrevious) {
      this.usePrevious = false;
      c = this.previous;
    } else {
      try {
        c = this.reader.read();
      }
      catch (IOException exception) {
        throw new Exception(exception);
      }
    }
    if (c <= 0) { // End of stream
      this.eof = true;
      //close();
      return 0;
    }
    this.incrementIndexes(c);
    this.previous = (char) c;
    return this.previous;
  }

  public boolean haveEscapeWarrant() { return (previous == Char.BACKSLASH) && this.backslashModeActive; }

  /**
   * Increments the internal indexes according to the previous character
   * read and the character passed as the current character.
   *
   * @param c the current character read.
   */
  private void incrementIndexes(int c) {
    if (c > 0) {
      this.index++;
      if (c == '\r') {
        this.line++;
        this.characterPreviousLine = this.column;
        this.column = 0;
      } else if (c == '\n') {
        if (this.previous != '\r') {
          this.line++;
          this.characterPreviousLine = this.column;
        }
        this.column = 0;
      } else {
        if (c == '\\') this.backslashModeActive = !this.backslashModeActive;
        else this.backslashModeActive = false;
        this.column++;
      }
    }
  }

  /**
   * Consume the next character, and check that it matches a specified
   * character.
   *
   * @param c The character to match.
   * @return The character.
   * @throws Exception if the character does not match.
   */
  public char scanExact(char c) throws Exception {
    char n = this.scanNext();
    if (n != c) {
      if (n > 0) {
        throw this.syntaxError("Failure while scanning: '" + c + "'; current-value: '" + n + "'");
      }
      throw this.syntaxError("Failure while scanning: '" + c + "'; current-value: ''");
    }
    return n;
  }

  /**
   * Get the next n characters.
   *
   * @param n The number of characters to take.
   * @return A string of n characters.
   * @throws Exception Substring bounds error if there are not
   *                   n characters remaining in the source string.
   */
  public String scanLength(int n) throws Exception {
    if (n == 0) {
      return "";
    }
    char[] chars = new char[n];
    int pos = 0;
    while (pos < n) {
      chars[pos] = this.scanNext();
      if (this.end()) {
        throw this.syntaxError("Substring bounds error");
      }
      pos += 1;
    }
    return new String(chars);
  }

  private void startMethod(Method method, Object... parameters) {
    if (method.methodScanner != null)
      throw new Exception(new OperationNotSupportedException("method instance already running"));
    method.method_initialize(this);
    method.startMethod(parameters);
  }

  public boolean isSeeking() {
    return seeking;
  }

  /**
   * Scans text until the TextScannerControl signals task complete.
   * <p>
   * if the text stream ends before the control returns a syntax error will be thrown.
   * if the controller signals early exit with a control character match or 1 based position equality with {@link Method#bufferLimit}, scanning will stop, and the next
   * stream token will be the current token.
   *
   * @param scanMethod the scan controller to use.
   * @return the scanned text
   * @throws SyntaxError
   */
  public String scan(Method scanMethod, Object... parameters) throws SyntaxError {
    char c;
    int i = 0;
    startMethod(scanMethod, parameters);
    StringBuilder scanned = new StringBuilder(scanMethod.bufferLimit);
    do {
      if ((c = this.scanNext()) == 0) {
        if (scanMethod.eofCeption) break;
        throw syntaxError("Expected '" + scanMethod + "'");
      }
      if (scanMethod.bufferLimit == ++i) {
        if (scanMethod.bufferLimitCeption) break;
        throw new Exception("Buffer overflow: " + scanMethod);
      }
      scanned.append(c);
      if (scanMethod.exitMethod(c)) break;
    } while (scanMethod.continueScanning(scanned));
    if (scanMethod.boundaryCeption) ;
    else {
      this.back();
      scanned.setLength(scanned.length() - 1);
    }
    scanMethod.methodScanner = null;
    return scanMethod.computeMethodCall(this, scanned);
  }

  /**
   * <p>Works like {@link #scan(Method, Object...)}, but restores the stream and
   * returns nothing if the operation fails.</p>
   * <br>
   * <p>seek cannot be called recursively, but can be called concurrently with 
   * scan methods.</p>
   * <br>
   * <p>Use the {@link #isSeeking()} method to determine if you should scan or
   * seek from within a nested method call.</p>
   *
   * @param seekMethod the Method to use
   * @return the text up to but not including the control break.
   * @throws Exception if an {@link java.io.IOException} occurs
   * @see #scanExact(char)
   * @see #scanNext()
   * @see #scanLength(int)
   * @see #scan(Method, Object...)
   */
  public String seek(Method seekMethod, Object... parameters) throws Exception {
    if (seeking) throw new Exception(new OperationNotSupportedException("scanner is already in seek mode"));
    seeking = true;
    char c = 0;
    startMethod(seekMethod, parameters);
    StringBuilder scanned = new StringBuilder(seekMethod.bufferLimit);
    boolean success = false;
    long startIndex = this.index;
    long startCharacter = this.column;
    long startLine = this.line;
    boolean escapeFlag = this.backslashModeActive;
    try {
      this.reader.mark(1000000);
      int i = 0;
      boolean bufferLimitReached;
      do {
        c = this.scanNext();
        bufferLimitReached = seekMethod.bufferLimit == ++i;
        if (bufferLimitReached || c == 0) {
          if (bufferLimitReached && seekMethod.bufferLimitCeption) break;
          if (seekMethod.eofCeption) break;
          throw new IllegalStateException();
        } else {
          scanned.append(c);
          if (seekMethod.exitMethod(c)) break;
        }
      } while (seekMethod.continueScanning(scanned));
      success = true;
    }
    catch (IllegalStateException signal){}
    finally {
      if (success == false){
        // in some readers, reset() may throw an exception if
        // the remaining portion of the input is greater than
        // the mark size (1,000,000 above).
        try { this.reader.reset(); }
        catch (IOException e) { throw new Exception(e); }
        this.index = startIndex;
        this.column = startCharacter;
        this.line = startLine;
        this.backslashModeActive = escapeFlag;
        scanned.setLength(0);
        seeking = false;
        seekMethod.methodScanner = null;
        return "";
      } else {
        try /*  ignoring exceptions */ {
          this.reader.mark(1);
        } catch (IOException ignored){}
        if (seekMethod.boundaryCeption) ;
        else {
          this.back();
          scanned.setLength(scanned.length() - 1);
        }
        seekMethod.methodScanner = null;
        seeking = false;
        return seekMethod.computeMethodCall(this, scanned);
      }
    }
  }

  /**
   * Make a TextScanner.Exception to signal a syntax error.
   *
   * @param message The error message.
   * @return A TextScanner.Exception object, suitable for throwing
   */
  public SyntaxError syntaxError(String message) {
    return new SyntaxError(message + toTraceString());
  }

  /**
   * Make a printable string of this TextScanner.
   *
   * @return " at source: position: {@link #index} = {line: {@link #line}, column: {@link #column}}"
   */
  public String toTraceString() {
    String source = " at " + Tools.makeNotNull(sourceLabel, "source") + ": position: ";
    return source + this.index + " = {line: " + this.line + ", column: " + this.column + "}";
  }

  @Override
  public Iterator<Character> iterator() {
    TextScanner host = this;
    return new Iterator<Character>() {
      @Override
      public boolean hasNext() {
        return host.hasNext();
      }

      @Override
      public Character next() {
        return host.scanNext();
      }
    };
  }

  @Override
  public void close() {
    if (closeReader) try /* close stream ignoring exceptions with final closure */ {
      reader.close();
      closed = true;
    }
    catch (IOException ignored) {}
  }

  @Override
  protected void finalize() throws Throwable {
    close();
    super.finalize();
  }

  /**
   * Text Scanner Method Implementation
   *
   * Creates a method for scanning text.
   *
   */
  public static class Method implements TextScannerMethod, Serializable, Cloneable {

    long backup_index, backup_line, backup_column;

    private static final long serialVersionUID = -7389459770461075270L;
    private static final String undefined = "undefined";
    // user managed
    protected int bufferLimit = 0;
    protected boolean boundaryCeption, eofCeption, bufferLimitCeption;
    protected String claim;
    TextScanner methodScanner;
    // implementation managed
    private char methodQuote;
    private SuperTokenMap<Serializable> methodTokenMap;
    private Stack<String> methodContext;

    public Method() {this(null);}

    public Method(@Nullable Object claim) { this.claim = String.valueOf(Tools.makeNotNull(claim, undefined)); }

    public Method BoundaryCeption(){
      if (methodScanner != null) throw new Exception(new OperationNotSupportedException());
      this.boundaryCeption = true;
      return this;
    }

    public Method EOFCeption(){
      if (methodScanner != null) throw new Exception(new OperationNotSupportedException());
      this.eofCeption = true;
      return this;
    }
    public Method BufferLimitCeption(){
      if (methodScanner != null) throw new Exception(new OperationNotSupportedException());
      this.bufferLimitCeption = true;
      return this;
    }

    final public SyntaxError syntaxError(String message){
      popErrorLocation();
      return methodScanner.syntaxError(message);
    }

    final public char scanNext() throws Exception {
      if (methodScanner == null) throw new Exception(new OperationNotSupportedException());
      return methodScanner.scanNext();
    }

    final public char scanExact(char c) throws Exception {
      if (methodScanner == null) throw new Exception(new OperationNotSupportedException());
      return methodScanner.scanExact(c);
    }

    final public String scanLength(int n) throws Exception {
      if (methodScanner == null) throw new Exception(new OperationNotSupportedException());
      return methodScanner.scanLength(n);
    }

    final public String scan(Method scanMethod, Object... parameters) throws SyntaxError {
      if (methodScanner == null) throw new Exception(new OperationNotSupportedException());
      return methodScanner.scan(scanMethod, parameters);
    }

    final public boolean isSeeking() {
      if (methodScanner == null) throw new Exception(new OperationNotSupportedException());
      return methodScanner.isSeeking();
    }

    final public String seek(Method seekMethod, Object... parameters) throws Exception {
      if (methodScanner == null) throw new Exception(new OperationNotSupportedException());
      return methodScanner.seek(seekMethod, parameters);
    }

    public void close() {
      if (methodScanner == null) return;
      methodScanner.close();
    }

    // implementation prep
    void method_initialize(TextScanner textScanner) {
      methodQuote = NULL_CHARACTER;
      methodTokenMap = new SuperTokenMap();
      methodContext = new Stack<>();
      methodScanner = textScanner;
      saveErrorLocation();
    }

    void saveErrorLocation() {
      if (methodScanner == null) throw new Exception(new OperationNotSupportedException());
      backup_index = index();
      backup_line = line();
      backup_column = column();
    }

    void popErrorLocation() {
      if (methodScanner == null) throw new Exception(new OperationNotSupportedException());
      methodScanner.line = backup_line;
      methodScanner.index = backup_index;
      methodScanner.column = backup_column;
    }

    /**
     * Enters a sub-context.
     * <p>
     * Virtual Stack Recursion Guardian.
     *
     * @param subContext the private data to associate with this token.
     * @return a context token.
     * @see #exitSubContext(String)
     */
    final protected String enterSubContext(Serializable subContext) {
      String token = methodTokenMap.put(subContext);
      methodContext.push(token);
      return token;
    }

    /**
     * Exits a sub-context as created by {@link #enterSubContext(Serializable)}
     * <p>
     * Virtual Stack Recursion Guardian.
     *
     * @param token the sub-context to exit
     * @param <ANY> the user format.
     * @return the user data.
     * @see #enterSubContext(Serializable)
     */
    final protected <ANY extends Serializable> ANY exitSubContext(String token) {
      if (methodContext.peek().equals(token)) {
        methodContext.pop();
        ANY v = (ANY) methodTokenMap.get(token);
        methodTokenMap.eraseToken(token);
        return v;
      } else {
        throw syntaxError("trying to exit wrong sub-context");
      }
    }

    @Override
    public void startMethod(Object[] parameters) {}

    @Override
    public String computeMethodCall(TextScanner scanner, StringBuilder scanned) {
      return scanned.toString();
    }

    @Override
    public boolean continueScanning(StringBuilder input) { return true; }

    @Override
    public boolean exitMethod(char character) { return character == 0; }

    @Override
    public String toString() { return claim; }

    @Override
    protected Object clone() {
      try /* bake-cookies ignoring exceptions with final closure */ {
        return super.clone();
      }
      catch (CloneNotSupportedException fatal) {throw new RuntimeException(fatal);}
    }

    final public boolean isQuoting() {
      return methodQuote != NULL_CHARACTER;
    }

    final public boolean quotedText(char character) {
      // handle quoting
      if (isQuoting()) {
        // deactivate quoting if applicable
        if (character == methodQuote) methodQuote = NULL_CHARACTER;
        return true;
      }
      // activate quoting if applicable
      if (character == DOUBLE_QUOTE || character == SINGLE_QUOTE) {
        methodQuote = character;
        return true;
      }
      return false;
    }

    final public boolean hasNext() {
      if (methodScanner == null) throw new Exception(new OperationNotSupportedException());
      return methodScanner.hasNext();
    }

    final public boolean haveEscapeWarrant() {
      if (methodScanner == null) throw new Exception(new OperationNotSupportedException());
      return methodScanner.haveEscapeWarrant();
    }

    final public boolean end() {
      if (methodScanner == null) throw new Exception(new OperationNotSupportedException());
      return methodScanner.end();
    }

    final public long index() {
      if (methodScanner == null) throw new Exception(new OperationNotSupportedException());
      return methodScanner.index();
    }

    final public long line() {
      if (methodScanner == null) throw new Exception(new OperationNotSupportedException());
      return methodScanner.line();
    }

    final public long column() {
      if (methodScanner == null) throw new Exception(new OperationNotSupportedException());
      return methodScanner.column();
    }

    final public String sourceLabel() {
      if (methodScanner == null) throw new Exception(new OperationNotSupportedException());
      return methodScanner.sourceLabel();
    }

    public static class FindString extends Method {

      protected String comparisonClaim;
      protected boolean checkMatch, caseSensitive = true;
      protected char[] finalMatchCharacter;
      protected int findLength, sourceLength;
      protected boolean handleQuoting = false;
      protected Locale locale = Locale.ENGLISH;
      public FindString() { super(); }
      public FindString(Object claim) { super(claim); }

      public FindString EscapeQuotes() {
        handleQuoting = true;
        return this;
      }

      public FindString AnyCase() {
        caseSensitive = false;
        return this;
      }

      public FindString AnyCase(Locale locale) {
        this.locale = locale;
        caseSensitive = false;
        return this;
      }

      @Override
      public void startMethod(Object... parameters) {
        claim = String.valueOf(parameters[0]);
        findLength = claim.length();
        checkMatch = false;
        finalMatchCharacter = new char[1];
        boundaryCeption = true;
        if (!caseSensitive) {
          comparisonClaim = claim.toLowerCase(locale);
          finalMatchCharacter[0] = comparisonClaim.charAt(claim.length() - 1);
        } else {
          finalMatchCharacter[0] = claim.charAt(claim.length() - 1);
        }
        sourceLength = 0;
      }

      @Override
      public boolean continueScanning(StringBuilder input) {
        if (checkMatch) {
          String match = input.substring(Math.max(0, sourceLength - findLength));
          // IF this matches STOP scanning by returning false.
          if (caseSensitive) {
            return !match.equals(claim);
          } else return !match.toLowerCase(locale).equals(comparisonClaim);
        }
        return true;
      }

      @Override
      public boolean exitMethod(char character) {

        // since this is a string-match-operation, every branch returns false.
        final boolean matchBoundary = false;

        if (handleQuoting) {
          // handle escapes
          if (haveEscapeWarrant()) return matchBoundary;
          // handle quoting
          if (quotedText(character)) return matchBoundary;
        }

        sourceLength++;
        if (!caseSensitive) character = String.valueOf(character).toLowerCase(locale).charAt(0);
        // activate matching if this is the last character to match and our buffer is large enough.
        checkMatch = (charMapContains(character, finalMatchCharacter)) && (sourceLength >= findLength);
        return matchBoundary;

      }

    }

    public static class MatchString extends Method {

      Pattern pattern;
      private int sourceLength, minimumLength, maximumLength;
      private boolean checkMatch, matchStart;

      public MatchString(String pattern){
        this(pattern, pattern.length(), pattern);
      }

      public MatchString(String pattern, int flags){
        this(pattern, pattern.length(), pattern, flags);
      }

      public MatchString(int minimumLength, String pattern){
        this(pattern, minimumLength, pattern);
      }

      public MatchString(int minimumLength, String pattern, int flags){
        this(pattern, minimumLength, pattern, flags);
      }

      public MatchString(String claim, int minimumLength, String pattern) {
        this(claim, minimumLength, pattern, 0);
      }

      public MatchString(String claim, int minimumLength, String pattern, int flags) {
        super(claim);
        this.minimumLength = minimumLength;
        this.pattern = Pattern.compile(pattern, flags);
      }

      public MatchString MatchStart(int maximumLength) {
        if (maximumLength != 0 || methodScanner != null) throw new Exception("maximum length already set", new OperationNotSupportedException());
        if (maximumLength < minimumLength)
          throw new IllegalArgumentException("maximum length is less than minimum length");
        this.maximumLength = maximumLength;
        return MatchStart();
      }

      public MatchString MatchStart() {
        if (matchStart || methodScanner != null) throw new Exception("match start configuration already set", new OperationNotSupportedException());
        matchStart = true;
        return this;
      }

      @Override
      public void startMethod(Object... parameters) {
        if (methodScanner == null) throw new Exception(new OperationNotSupportedException());
        checkMatch = false;
        boundaryCeption = true;
        sourceLength = 0;
        if (matchStart) saveErrorLocation();
      }

      @Override
      public boolean continueScanning(StringBuilder input) {
        if (methodScanner == null) throw new Exception(new OperationNotSupportedException());
        if (checkMatch) {
          String match;
          if (matchStart) {
            match = (maximumLength > 0 && sourceLength >= maximumLength) ?
                input.substring(0, maximumLength) : input.substring(0);
          }
          else match = input.substring(Math.max(0, sourceLength - minimumLength));
          if (pattern.matcher(match).matches()) return false;
          else if (matchStart && !isSeeking()) {
            popErrorLocation();
            throw syntaxError("Expected: " + this + "; Found: `" + input.charAt(0) + "'");
          }
        }
        return true;
      }

      @Override
      public boolean exitMethod(char character) {
        if (methodScanner == null) throw new Exception(new OperationNotSupportedException());
        sourceLength++;
        checkMatch = (sourceLength >= minimumLength);
        return false;
      }
    }

  }


}
