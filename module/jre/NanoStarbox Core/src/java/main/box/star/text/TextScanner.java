package box.star.text;

import box.star.Tools;
import box.star.contract.Nullable;
import box.star.io.SourceConnector;

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import static box.star.text.TextScanner.ASCII.*;

public class TextScanner implements Iterable<Character>, TextScannerMethodContext, Closeable {

  public final static int CHAR_MAX = '\uffff';
  private SyntaxErrorMarshal syntaxErrorMarshal = new SyntaxErrorMarshal();
  private boolean backslashModeActive;

  public static int atLeastZero(int val){ return (val < 0)?0:val; }
  public static int atMostCharMax(int val){ return (val > CHAR_MAX)?'\uffff':val; }
  public static int sanitizeRangeValue(int val){ return atLeastZero(atMostCharMax(val));}

  public static boolean charMapContains(char search, char[] range){
    for (char c: range)
      if (search == c) return true;
    return false;
  }

  public static char[] buildRangeMap(RangeMap range){
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
      if (scanMethod.matchBoundary(this, c)) break;
    } while (scanMethod.continueScanning(this, scanned));
    if (scanMethod.boundaryCeption);
    else {
      this.back();
      scanned.setLength(scanned.length() - 1);
    }
    return scanMethod.returnScanned(this, scanned);
  }

  private void startMethod(Method method, Object... parameters){
    method.reset_quoting();
    method.beginScanning(this, parameters);
  }

  /**
   * Works like scan, but restores the stream and returns nothing if the operation fails.
   *
   * @param seekMethod
   * @return the text up to but not including the control break.
   * @throws Exception if an IOException occurs
   */
  public String seek(Method seekMethod, Object... parameters) throws Exception {
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
          return "";
        } else {
          scanned.append(c);
          if (seekMethod.matchBoundary(this, c)) break;
        }
      } while (seekMethod.continueScanning(this, scanned));
      this.reader.mark(1);
    } catch (IOException exception) { throw new Exception(exception); }
    if (seekMethod.boundaryCeption);
    else {
      this.back();
      scanned.setLength(scanned.length() - 1);
    }
    return seekMethod.returnScanned(this, scanned);
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
   * @return " location: source character-position: {@link #index} = {line: {@link #line}, column: {@link #column}}"
   */
  public String toTraceString() {
    String source = "; location: " + Tools.makeNotNull(sourceLabel, "source") + " character-position: ";
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

    public SyntaxError raiseSyntaxError(String message, Throwable causedBy) {
      return new SyntaxError(message, causedBy);
    }
    public SyntaxError raiseSyntaxError(String message){
      return new SyntaxError(message);
    }

  }

  public static final class ASCII{

    public final static char NULL_CHARACTER = 0;

    public final static char META_DOCUMENT_TAG_START = '<';
    public final static char META_DOCUMENT_TAG_END = '>';

    public final static char BACKSLASH = '\\';
    public final static char SINGLE_QUOTE = '\'';
    public final static char DOUBLE_QUOTE = '"';

    public final static char[] MAP_WHITE_SPACE = new RangeMap.Assembler(9, 13).merge(20).compile();
    public final static char[] MAP_LETTERS = new RangeMap.Assembler(65, 90).merge(97, 122).compile();
    public final static char[] MAP_NUMBERS = new RangeMap(48, 57).compile();
    public final static char[] MAP_CONTROL = new RangeMap.Assembler(0, 32).filter(MAP_WHITE_SPACE).compile();
    public final static char[] MAP_EXTENDED = new RangeMap(127, CHAR_MAX).compile();

    public final static char[] MAP_SYMBOLS = new RangeMap.Assembler(33, 47)
        .merge(58, 64)
        .merge(91, 96)
        .merge(123, 126)
        .compile();

  }

  public static class RangeMap {

    public final int start, end;
    public RangeMap(int start, int end){
      this.start = sanitizeRangeValue(start); this.end = sanitizeRangeValue(end);
    }
    public boolean match(char character) {
      return character < start || character > end;
    }
    public char[] compile(){
      return buildRangeMap(this);
    }

    public static class Assembler {
      StringBuilder chars = new StringBuilder();
      public Assembler(RangeMap map){
        this(map.compile());
      }
      public Assembler(char... map){
        this.merge(map);
      }
      public Assembler(int start, int end){
        merge(new RangeMap(start, end));
      }
      public Assembler(int... integer){
        for (int i:integer) chars.append((char) sanitizeRangeValue(i));
      }
      public Assembler merge(int... integer){
        for (int i:integer) chars.append((char) sanitizeRangeValue(i));
        return this;
      }
      public Assembler merge(int start, int end){
        return merge(new RangeMap(start, end));
      }
      public Assembler merge(RangeMap map){
        return merge(map.compile());
      }
      public Assembler merge(char... map){
        for (char c: map) chars.append(c);
        return this;
      }
      public Assembler filter(int... integer){
        StringBuilder map = new StringBuilder();
        for (int i : integer) map.append((char) i);
        char[] chars = map.toString().toCharArray();
        filter(chars);
        return this;
      }
      public Assembler filter(int start, int end){
        return filter(new RangeMap(start, end));
      }
      public Assembler filter(RangeMap map){
        return filter(map.compile());
      }
      public Assembler filter(char... map){
        StringBuilder filter = new StringBuilder();
        for (char c: filter.toString().toCharArray()){
          if (charMapContains(c, map)) continue;
          filter.append(c);
        }
        this.chars = filter;
        return this;
      }

      public char[] compile(){
        return chars.toString().toCharArray();
      }

      @Override
      public String toString() {
        return chars.toString();
      }

    }

  }

  public static class Method implements TextScannerMethodDriver, CharacterBoundaryControl, Serializable, Cloneable {

    private static final long serialVersionUID = -7389459770461075270L;
    private static final String undefined = "undefined";

    protected char quote;
    protected int bufferLimit = 0;
    protected boolean boundaryCeption, eofCeption, bufferLimitCeption;
    protected String claim;

    private void reset_quoting(){
      quote = NULL_CHARACTER;
    }

    public Method(){this(null);}
    public Method(@Nullable Object claim){ this.claim = String.valueOf(Tools.makeNotNull(claim, undefined)); }

    @Override
    public void beginScanning(TextScannerMethodContext context, Object[] parameters) {}

    @Override
    public String returnScanned(TextScanner scanner, StringBuilder scanned) {
      return scanned.toString();
    }

    @Override public boolean continueScanning(TextScannerMethodContext context, StringBuilder input) { return true; }
    @Override public boolean matchBoundary(TextScannerMethodContext context, char character) { return character == 0; }
    @Override public String toString() { return claim; }

    @Override
    protected Object clone() {
      try /* bake-cookies ignoring exceptions with final closure */ {
        return super.clone();
      } catch (CloneNotSupportedException fatal){throw new RuntimeException(fatal);}
    }

    public boolean isQuoting(){
      return quote != NULL_CHARACTER;
    }

    public boolean matchQuote(char character){
      // handle quoting
      if (isQuoting()){
        // deactivate quoting if applicable
        if (character == quote) quote = NULL_CHARACTER;
        return true;
      }
      // activate quoting if applicable
      if (character == DOUBLE_QUOTE || character == SINGLE_QUOTE){
        quote = character;
        return true;
      }
      return false;
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
  
  public static interface CharacterBoundaryControl {
    boolean matchBoundary(TextScannerMethodContext context, char character);
  }

  public static class FindUnquotedStringMethod extends Method {
    protected String comparisonClaim;
    protected boolean checkMatch, caseSensitive;
    protected char quote, finalMatchCharacter;
    protected int findLength, sourceLength;
    @Override
    public void beginScanning(TextScannerMethodContext context, Object... parameters) {
      claim = String.valueOf(parameters[0]);
      finalMatchCharacter = claim.charAt(claim.length() - 1);
      findLength = claim.length();
      checkMatch = false;
      boundaryCeption = true;
      caseSensitive = (parameters.length > 1) && (boolean) parameters[1];
      if (! caseSensitive) comparisonClaim = claim.toLowerCase(Locale.ENGLISH);
      sourceLength = 0;
      quote = NULL_CHARACTER;
    }
    boolean quoting(){
      return quote != NULL_CHARACTER;
    }
    @Override
    public boolean continueScanning(TextScannerMethodContext context, StringBuilder input) {
      if (checkMatch) {
        String match = input.substring(Math.max(0, sourceLength - findLength));
        // IF this matches STOP scanning by returning false.
        if (caseSensitive) { if (match.endsWith(claim)) return false; }
        else if (match.toLowerCase(Locale.ENGLISH).equals(comparisonClaim)) return false;
      }
      return true;
    }
    @Override
    public boolean matchBoundary(TextScannerMethodContext context, char character) {

      sourceLength++;

      // since this is a string-match-operation, every branch returns false.
      final boolean matchBoundary = false;

      // handle escapes
      if (context.haveEscapeWarrant()) return matchBoundary;
      // handle quoting
      if (matchQuote(character)) return matchBoundary;

      // activate matching if this is the last character to match and our buffer is large enough.
      checkMatch = (character == finalMatchCharacter) && sourceLength >= findLength;

      return matchBoundary;

    }

  }
}
