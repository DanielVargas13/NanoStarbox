package box.star.text;

import box.star.Tools;
import box.star.contract.Nullable;
import box.star.io.SourceConnector;
import box.star.state.SuperTokenMap;

import javax.naming.OperationNotSupportedException;
import java.io.*;
import java.net.URI;
import java.net.URL;
import java.util.*;
import java.util.regex.Pattern;

import static box.star.text.TextScanner.ASCII.*;

public class TextScanner implements Iterable<Character>, Closeable {

  public final static int CHAR_MAX = '\uffff';
  private SyntaxErrorMarshal syntaxErrorMarshal = new SyntaxErrorMarshal();
  {
    syntaxErrorMarshal.scanner = this;
  }
  private boolean backslashModeActive;

  public SyntaxErrorMarshal getSyntaxErrorMarshal() {
    return syntaxErrorMarshal;
  }

  public static int atLeastZero(int val){ return (val < 0)?0:val; }
  public static int atMostCharMax(int val){ return (val > CHAR_MAX)?'\uffff':val; }
  public static int sanitizeRangeValue(int val){ return atLeastZero(atMostCharMax(val));}

  public static boolean charMapContains(char search, char[] range){
    for (int i = 0; i < range.length; i++) if (range[i] == search) return true;
    return false;
  }

  private static char[] buildRangeMap(CharacterClass.RangeMap range){
    StringBuilder out = new StringBuilder();
    for(int i = range.start; i <= range.end; i++)out.append((char)i);
    return out.toString().toCharArray();
  }

  /** current read character position on the current line. */
  private long column;
  /** flag to indicate if the end of the input has been found. */
  private boolean eof;
  /** current read index of the input. */
  private long index;
  /** current line of the input. */
  private long line;
  /** previous character read from the input. */
  private char previous;
  /** Reader for the input. */
  private final Reader reader;
  /** flag to indicate that a previous character was requested. */
  private boolean usePrevious;
  /** the number of characters read in the previous line. */
  private long characterPreviousLine;
  private boolean closeReader, closed;
  private String sourceLabel;

  /**
   * Construct a TextScanner from a Reader. The caller must close the Reader.
   *
   * @param sourceLabel a label for this text processor such as a file or url.
   * @param reader     A reader.
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
   * @param source     A source string.
   */
  public TextScanner(String source) {
    this(source.getClass().getName(), new StringReader(source));
    this.closeReader = true;
  }

  /**
   * Construct a TextScanner from a string.
   *
   * @param source     A source string.
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
   * @throws Exception Thrown if trying to step back more than 1 step
   *  or if already at the start of the string
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
    if(this.previous=='\r' || this.previous == '\n') {
      this.line--;
      this.column = this.characterPreviousLine ;
    } else if(this.column > 0){
      if (this.previous == '\\') this.backslashModeActive = ! this.backslashModeActive;
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
   *  or backward while checking for more data.
   */
  public boolean hasNext() throws Exception {
    if(this.usePrevious) {
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
        this.eof = true;
        return false;
      }
      this.reader.reset();
    } catch (IOException e) {
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
      } catch (IOException exception) {
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

  public boolean haveEscapeWarrant(){ return (previous == ASCII.BACKSLASH) && this.backslashModeActive; }

  /**
   * Increments the internal indexes according to the previous character
   * read and the character passed as the current character.
   * @param c the current character read.
   */
  private void incrementIndexes(int c) {
    if(c > 0) {
      this.index++;
      if(c=='\r') {
        this.line++;
        this.characterPreviousLine = this.column;
        this.column =0;
      }else if (c=='\n') {
        if(this.previous != '\r') {
          this.line++;
          this.characterPreviousLine = this.column;
        }
        this.column =0;
      } else {
        if (c == '\\') this.backslashModeActive = ! this.backslashModeActive;
        else this.backslashModeActive = false;
        this.column++;
      }
    }
  }

  /**
   * Consume the next character, and check that it matches a specified
   * character.
   * @param c The character to match.
   * @return The character.
   * @throws Exception if the character does not match.
   */
  public char scanExact(char c) throws Exception {
    char n = this.scanNext();
    if (n != c) {
      if(n > 0) {
        throw this.claimSyntaxError("Failure while scanning: '"+c+"'; current-value: '"+n+"'");
      }
      throw this.claimSyntaxError("Failure while scanning: '" + c + "'; current-value: ''");
    }
    return n;
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
  public String scanLength(int n) throws Exception {
    if (n == 0) {
      return "";
    }
    char[] chars = new char[n];
    int pos = 0;
    while (pos < n) {
      chars[pos] = this.scanNext();
      if (this.end()) {
        throw this.claimSyntaxError("Substring bounds error");
      }
      pos += 1;
    }
    return new String(chars);
  }

  /**
   * Scans text until the TextScannerControl signals task complete.
   *
   * if the text stream ends before the control returns a syntax error will be thrown.
   * if the controller signals early exit with a control character match or 1 based position equality with {@link Method#bufferLimit}, scanning will stop, and the next
   * stream token will be the current token.
   *
   * @param scanMethod the scan controller to use.
   * @return the scanned text
   * @throws SyntaxError
   */
  public String scan(Method scanMethod, Object... parameters) throws SyntaxError {
    char c; int i = 0;
    startMethod(scanMethod, parameters);
    StringBuilder scanned = new StringBuilder(scanMethod.bufferLimit);
    do {
      if ((c = this.scanNext()) == 0){
        if (scanMethod.eofCeption) break;
        throw claimSyntaxError("Expected '"+scanMethod+"'");
      }
      if (scanMethod.bufferLimit == ++i){
        if (scanMethod.bufferLimitCeption) break;
        throw new Exception("Buffer overflow: "+scanMethod);
      }
      scanned.append(c);
      if (scanMethod.exitMethod(c)) break;
    } while (scanMethod.continueScanning(scanned));
    if (scanMethod.boundaryCeption);
    else {
      this.back();
      scanned.setLength(scanned.length() - 1);
    }
    scanMethod.methodScanner = null;
    return scanMethod.computeMethodCall(this, scanned);
  }

  private void startMethod(Method method, Object... parameters){
    if (method.methodScanner != null) throw new Exception(new OperationNotSupportedException("method instance already running"));
    method.method_initialize(this);
    method.startMethod(parameters);
  }

  private boolean seeking;

  public boolean isSeeking() {
    return seeking;
  }

  /**
   * Works like scan, but restores the stream and returns nothing if the operation fails.
   *
   * @param seekMethod
   * @return the text up to but not including the control break.
   * @throws Exception if an IOException occurs
   */
  public String seek(Method seekMethod, Object... parameters) throws Exception {
    if (seeking) throw new Exception(new OperationNotSupportedException("scanner is already in seek mode"));
    seeking = true;
    char c = 0;
    startMethod(seekMethod, parameters);
    StringBuilder scanned = new StringBuilder(seekMethod.bufferLimit);
    try {
      long startIndex = this.index;
      long startCharacter = this.column;
      long startLine = this.line;
      boolean escapeFlag = this.backslashModeActive;
      this.reader.mark(1000000);
      int i = 0;
      boolean bufferLimitReached;
      do {
        c = this.scanNext();
        bufferLimitReached = seekMethod.bufferLimit == ++i;
        if (bufferLimitReached || c == 0) {
          if (bufferLimitReached && seekMethod.bufferLimitCeption) break;
          if (seekMethod.eofCeption) break;
          // in some readers, reset() may throw an exception if
          // the remaining portion of the input is greater than
          // the mark size (1,000,000 above).
          this.reader.reset();
          this.index = startIndex;
          this.column = startCharacter;
          this.line = startLine;
          this.backslashModeActive = escapeFlag;
          scanned.setLength(0);
          seeking = false;
          seekMethod.methodScanner = null;
          return "";
        } else {
          scanned.append(c);
          if (seekMethod.exitMethod(c)) break;
        }
      } while (seekMethod.continueScanning(scanned));
      this.reader.mark(1);
    } catch (IOException exception) { throw new Exception(exception); }
    if (seekMethod.boundaryCeption);
    else {
      this.back();
      scanned.setLength(scanned.length() - 1);
    }
    seekMethod.methodScanner = null;
    seeking = false;
    return seekMethod.computeMethodCall(this, scanned);
  }
  
  /**
   * Make a TextScannerException to signal a syntax error.
   *
   * @param message The error message.
   * @return  A TextScannerException object, suitable for throwing
   */
  public SyntaxError claimSyntaxError(String message) {
    return syntaxErrorMarshal.raiseSyntaxError(message + this.toTraceString());
  }

  /**
   * Make a TextScannerException to signal a syntax error.
   *
   * @param message The error message.
   * @param causedBy The throwable that caused the error.
   * @return  A TextScannerException object, suitable for throwing
   */
  public SyntaxError claimSyntaxError(String message, Throwable causedBy) {
    return syntaxErrorMarshal.raiseSyntaxError(message + this.toTraceString(), causedBy);
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

  void setSyntaxErrorMarshal(SyntaxErrorMarshal marshal){
    marshal.scanner = this;
    this.syntaxErrorMarshal = marshal;
  }

  @Override
  public void close() {
    if (closeReader) try /* close stream ignoring exceptions with final closure */ {
      reader.close();
      closed = true;
    } catch (IOException ignored){}
  }

  public static class SyntaxErrorMarshal {
    private TextScanner scanner;
    public SyntaxError raiseSyntaxError(String message, Throwable causedBy) {
      return new SyntaxError(message+scanner.toTraceString(), causedBy);
    }
    public SyntaxError raiseSyntaxError(String message){
      return new SyntaxError(message+scanner.toTraceString());
    }

  }

  public static final class ASCII {

    public final static char NULL_CHARACTER = 0;

    public final static char META_DOCUMENT_TAG_START = '<';
    public final static char META_DOCUMENT_TAG_END = '>';

    public final static char BACKSLASH = '\\';
    public final static char SINGLE_QUOTE = '\'';
    public final static char DOUBLE_QUOTE = '"';

    public final static char[] MAP = new CharacterClass(0, CHAR_MAX).assemble();
    public final static char[] MAP_WHITE_SPACE = new CharacterClass(9, 13).merge(' ').assemble();
    public final static char[] MAP_LETTERS = new CharacterClass(65, 90).merge(97, 122).assemble();
    public final static char[] MAP_NUMBERS = new CharacterClass.RangeMap(48, 57).compile();
    public final static char[] MAP_CONTROL = new CharacterClass(0, 31).filter(MAP_WHITE_SPACE).assemble();
    public final static char[] MAP_EXTENDED = new CharacterClass.RangeMap(127, CHAR_MAX).compile();

    public final static char[] MAP_SYMBOLS = new CharacterClass(33, 47)
        .merge(58, 64)
        .merge(91, 96)
        .merge(123, 126)
        .assemble();
  }

  public static class Method implements TextScannerMethod, Serializable, Cloneable {

    private static final long serialVersionUID = -7389459770461075270L;
    private static final String undefined = "undefined";
    private long index, line, column;

    // implementation managed
    private char methodQuote;
    private SuperTokenMap<Serializable> methodTokenMap;
    private Stack<String> methodContext;

    final public SyntaxErrorMarshal getSyntaxErrorMarshal() {
      if (methodScanner == null) throw new RuntimeException(new OperationNotSupportedException());
      return methodScanner.getSyntaxErrorMarshal();
    }

    final public char scanNext() throws Exception {
      if (methodScanner == null) throw new RuntimeException(new OperationNotSupportedException());
      return methodScanner.scanNext();
    }

    final public char scanExact(char c) throws Exception {
      if (methodScanner == null) throw new RuntimeException(new OperationNotSupportedException());
      return methodScanner.scanExact(c);
    }

    final public String scanLength(int n) throws Exception {
      if (methodScanner == null) throw new RuntimeException(new OperationNotSupportedException());
      return methodScanner.scanLength(n);
    }

    final public String scan(Method scanMethod, Object... parameters) throws SyntaxError {
      if (methodScanner == null) throw new RuntimeException(new OperationNotSupportedException());
      return methodScanner.scan(scanMethod, parameters);
    }

    final public boolean isSeeking() {
      if (methodScanner == null) throw new RuntimeException(new OperationNotSupportedException());
      return methodScanner.isSeeking();
    }

    final public String seek(Method seekMethod, Object... parameters) throws Exception {
      if (methodScanner == null) throw new RuntimeException(new OperationNotSupportedException());
      return methodScanner.seek(seekMethod, parameters);
    }

    public void close() {
      if (methodScanner == null) return;
      methodScanner.close();
    }

    TextScanner methodScanner;

    protected void saveErrorLocation(){
      index = index();
      line = line();
      column = column();
    }

    protected void popErrorLocation(){
      methodScanner.line = line;
      methodScanner.index = index;
      methodScanner.column = column;
    }

    // implementation prep
    private void method_initialize(TextScanner textScanner){
      methodQuote = NULL_CHARACTER;
      methodTokenMap = new SuperTokenMap();
      methodContext = new Stack<>();
      methodScanner = textScanner;
    }

    // user managed
    protected int bufferLimit = 0;
    protected boolean boundaryCeption, eofCeption, bufferLimitCeption;
    protected String claim;

    /**
     * Enters a sub-context.
     *
     * Virtual Stack Recursion Guardian.
     *
     * @param subContext the private data to associate with this token.
     * @return a context token.
     * @see #exitSubContext(String)
     */
    final protected String enterSubContext(Serializable subContext){
      String token = methodTokenMap.put(subContext);
      methodContext.push(token);
      return token;
    }

    /**
     * Exits a sub-context as created by {@link #enterSubContext(Serializable)}
     *
     * Virtual Stack Recursion Guardian.
     *
     * @param token the sub-context to exit
     * @param <ANY> the user format.
     * @return the user data.
     * @see #enterSubContext(Serializable)
     */
    final protected <ANY extends Serializable> ANY exitSubContext(String token){
      if (methodContext.peek().equals(token)){
        methodContext.pop();
        ANY v = (ANY) methodTokenMap.get(token);
        methodTokenMap.eraseToken(token);
        return v;
      } else{
        throw methodScanner.getSyntaxErrorMarshal().raiseSyntaxError("trying to exit wrong sub-context");
      }
    }

    public Method(){this(null);}
    public Method(@Nullable Object claim){ this.claim = String.valueOf(Tools.makeNotNull(claim, undefined)); }

    @Override
    public void startMethod(Object[] parameters) {}

    @Override
    public String computeMethodCall(TextScanner scanner, StringBuilder scanned) {
      return scanned.toString();
    }

    @Override public boolean continueScanning(StringBuilder input) { return true; }
    @Override public boolean exitMethod(char character) { return character == 0; }
    @Override public String toString() { return claim; }

    @Override
    protected Object clone() {
      try /* bake-cookies ignoring exceptions with final closure */ {
        return super.clone();
      } catch (CloneNotSupportedException fatal){throw new RuntimeException(fatal);}
    }

    final public boolean isQuoting(){
      return methodQuote != NULL_CHARACTER;
    }

    final public boolean matchQuote(char character){
      // handle quoting
      if (isQuoting()){
        // deactivate quoting if applicable
        if (character == methodQuote) methodQuote = NULL_CHARACTER;
        return true;
      }
      // activate quoting if applicable
      if (character == DOUBLE_QUOTE || character == SINGLE_QUOTE){
        methodQuote = character;
        return true;
      }
      return false;
    }

    final public boolean hasNext() {
      if (methodScanner == null) throw new RuntimeException(new OperationNotSupportedException());
      return methodScanner.hasNext();
    }

    final public boolean haveEscapeWarrant() {
      if (methodScanner == null) throw new RuntimeException(new OperationNotSupportedException());
      return methodScanner.haveEscapeWarrant();
    }

    final public boolean end() {
      if (methodScanner == null) throw new RuntimeException(new OperationNotSupportedException());
      return methodScanner.end();
    }

    final public long index() {
      if (methodScanner == null) throw new RuntimeException(new OperationNotSupportedException());
      return methodScanner.index();
    }

    final public long line() {
      if (methodScanner == null) throw new RuntimeException(new OperationNotSupportedException());
      return methodScanner.line();
    }

    final public long column() {
      if (methodScanner == null) throw new RuntimeException(new OperationNotSupportedException());
      return methodScanner.column();
    }

    final public String sourceLabel() {
      if (methodScanner == null) throw new RuntimeException(new OperationNotSupportedException());
      return methodScanner.sourceLabel();
    }

    public static class FindString extends Method {

      public FindString() { super(); }
      public FindString(Object claim) { super(claim); }

      protected String comparisonClaim;
      protected boolean checkMatch, caseSensitive = true;
      protected char[] finalMatchCharacter;
      protected int findLength, sourceLength;
      protected boolean handleQuoting = false;
      protected Locale locale = Locale.ENGLISH;

      public FindString EscapeQuotes(){
        handleQuoting = true;
        return this;
      }

      public FindString AnyCase(){
        caseSensitive = false;
        return this;
      }

      public FindString AnyCase(Locale locale){
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
        if (! caseSensitive) {
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
          if (caseSensitive) { if (match.equals(claim)) return false; }
          else if (match.toLowerCase(locale).equals(comparisonClaim)) return false;
        }
        return true;
      }

      @Override
      public boolean exitMethod(char character) {

        // since this is a string-match-operation, every branch returns false.
        final boolean matchBoundary = false;

        if (handleQuoting){
          // handle escapes
          if (haveEscapeWarrant()) return matchBoundary;
          // handle quoting
          if (matchQuote(character)) return matchBoundary;
        }

        sourceLength++;
        if (! caseSensitive) character = String.valueOf(character).toLowerCase(locale).charAt(0);
        // activate matching if this is the last character to match and our buffer is large enough.
        checkMatch = (charMapContains(character, finalMatchCharacter)) && (sourceLength >= findLength);
        return matchBoundary;

      }

    }

    public static class MatchString extends Method {

      private final int findLength;
      Pattern pattern;
      private boolean checkMatch, matchStart;
      private int sourceLength;

      public MatchString MatchStart(){
        matchStart = true;
        return this;
      }

      public MatchString(String claim, int length, String pattern) {
        this(claim, length, pattern, 0);
      }
      public MatchString(String claim, int length, String pattern, int flags) {
        super(claim);
        this.findLength = length;
        this.pattern = Pattern.compile(pattern, flags);
      }

      @Override
      public void startMethod(Object... parameters) {
        checkMatch = false;
        boundaryCeption = true;
        sourceLength = 0;
        if (matchStart) saveErrorLocation();
      }

      @Override
      public boolean continueScanning(StringBuilder input) {
        if (checkMatch) {
          String match = input.substring(Math.max(0, sourceLength - findLength));
          if (pattern.matcher(match).matches()) return false;
          else if (matchStart) {
            popErrorLocation();
            throw getSyntaxErrorMarshal().raiseSyntaxError("Expected: " + this + "; Found: `" + input.charAt(0)+"'");
          };
        }
        return true;
      }

      @Override
      public boolean exitMethod(char character) {
        sourceLength++;
        if (matchStart) checkMatch = (sourceLength == findLength);
        else checkMatch = (sourceLength >= findLength);
        return false;
      }
    }

  }

  /**
   * The JSONException is thrown by the JSON.org classes when things are amiss.
   *
   * @author JSON.org
   * @version 2015-12-09
   */
  public static class Exception extends RuntimeException {
      /** Serialization ID */
      private static final long serialVersionUID = 0;

      /**
       * Constructs a JSONException with an explanatory message.
       *
       * @param message
       *            Detail about the reason for the exception.
       */
      public Exception(final String message) {
          super(message);
      }

      /**
       * Constructs a JSONException with an explanatory message and cause.
       *
       * @param message
       *            Detail about the reason for the exception.
       * @param cause
       *            The cause.
       */
      public Exception(final String message, final Throwable cause) {
          super(message, cause);
      }

      /**
       * Constructs a new JSONException with the specified cause.
       *
       * @param cause
       *            The cause.
       */
      public Exception(final Throwable cause) {
          super(cause.getMessage(), cause);
      }

  }

  /**
   * The JSONException is thrown by the JSON.org classes when things are amiss.
   *
   * @author JSON.org
   * @version 2015-12-09
   */
  public static class SyntaxError extends Exception {
      /** Serialization ID */
      private static final long serialVersionUID = 0;

      /**
       * Constructs a JSONException with an explanatory message.
       *
       * @param message
       *            Detail about the reason for the exception.
       */
      public SyntaxError(final String message) {
          super(message);
      }

      /**
       * Constructs a JSONException with an explanatory message and cause.
       *
       * @param message
       *            Detail about the reason for the exception.
       * @param cause
       *            The cause.
       */
      public SyntaxError(final String message, final Throwable cause) {
          super(message, cause);
      }

      /**
       * Constructs a new JSONException with the specified cause.
       *
       * @param cause
       *            The cause.
       */
      public SyntaxError(final Throwable cause) {
          super(cause.getMessage(), cause);
      }

  }

  @Override
  protected void finalize() throws Throwable {
    close();
    super.finalize();
  }

  public static class CharacterClass implements Serializable {
    private static final long serialVersionUID = 8454376662352328447L;
    StringBuilder chars = new StringBuilder();
    public CharacterClass(RangeMap map){
      this(map.compile());
    }
    public CharacterClass(char... map){
      merge(map);
    }
    public CharacterClass(int start, int end){
      merge(new RangeMap(start, end));
    }
    public CharacterClass(int... integer){
      merge(integer);
    }
    public CharacterClass merge(int... integer){
      char[] current = assemble();
      for (int i:integer) {
        char c = (char)sanitizeRangeValue(i);
        if (! charMapContains(c, current)) chars.append(c);
      }
      return this;
    }
    public CharacterClass merge(int start, int end){
      return merge(new RangeMap(start, end));
    }
    public CharacterClass merge(RangeMap map){
      return merge(map.compile());
    }
    public CharacterClass merge(char... map){
      char[] current = assemble();
      for (char c: map) if (! charMapContains(c, current)) chars.append(c);
      return this;
    }
    public CharacterClass filter(int... integer){
      StringBuilder map = new StringBuilder();
      for (int i : integer) map.append((char) i);
      char[] chars = map.toString().toCharArray();
      filter(chars);
      return this;
    }
    public CharacterClass filter(int start, int end){
      return filter(new RangeMap(start, end));
    }
    public CharacterClass filter(RangeMap map){
      return filter(map.compile());
    }
    public CharacterClass filter(char... map){
      StringBuilder filter = new StringBuilder();
      for (char c: chars.toString().toCharArray()){
        if (charMapContains(c, map)) continue;
        filter.append(c);
      }
      this.chars = filter;
      return this;
    }

    public char[] assemble(){
      return chars.toString().toCharArray();
    }

    @Override
    public String toString() {
      return chars.toString();
    }

    public boolean match(char character){
      return chars.indexOf(character+"") != -1;
    }

    private static class RangeMap {

      public final int start, end;

      private RangeMap(int start, int end){
        this.start = sanitizeRangeValue(start); this.end = sanitizeRangeValue(end);
      }

      public boolean match(char character) {
        return character >= start || character <= end;
      }

      public char[] compile(){
        return buildRangeMap(this);
      }

    }
  }
}
