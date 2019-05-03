package box.star.text;

import box.star.Tools;
import box.star.contract.Nullable;
import box.star.io.SourceReader;

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TextScanner implements Iterable<Character>, TextScannerContext {

  private final static int UBER_MAX = '\uffff';

  private ExceptionMarshal exceptionMarshal = new ExceptionMarshal();

  public static int atLeastZero(int val){ return (val < 0)?0:val; }
  public static int atMostCharMax(int val){ return (val > UBER_MAX)?'\uffff':val; }
  public static int normalizeRangeValue(int val){ return atLeastZero(atMostCharMax(val));}

  public static boolean charMapContains(char[] range, char search){
    for (char c: range)
      if (search == c) return true;
    return false;
  }

  public static char[] buildRangeMap(RangeMap range){
    List<Character> list = new ArrayList<>(normalizeRangeValue(range.end) - normalizeRangeValue(range.start));
    for (int i = range.start; i <= range.end; i++) list.add((char)i);
    Character[] out = new Character[list.size()];
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

  private String sourceLabel;

  /**
   * Construct a SourceProcessor from a Reader. The caller must close the Reader.
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
    this(source.getPath(), SourceReader.getRuntimeFileOrUrlInputStream(source.toString()));
  }
  public TextScanner(URI source) {
    this(source.getPath(), SourceReader.getRuntimeFileOrUrlInputStream(source.toString()));
  }
  public TextScanner(File source) {
    this(source.getPath(), SourceReader.getRuntimeFileOrUrlInputStream(source.getPath()));
  }

  /**
   * Construct a SourceProcessor from an InputStream. The caller must close the input stream.
   *
   * @param inputStream The source.
   */
  public TextScanner(InputStream inputStream) {
    this(inputStream.getClass().getName(), new InputStreamReader(inputStream));
  }

  /**
   * Construct a SourceProcessor from an InputStream. The caller must close the input stream.
   *
   * @param sourceLabel The label for the source.
   * @param inputStream The source.
   */
  public TextScanner(String sourceLabel, InputStream inputStream) {
    this(sourceLabel, new InputStreamReader(inputStream));
  }

  /**
   * Construct a SourceProcessor from a string.
   *
   * @param source     A source string.
   */
  public TextScanner(String source) {
    this(source.getClass().getName(), new StringReader(source));
  }

  /**
   * Construct a SourceProcessor from a string.
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
      throw raiseException("Stepping back two steps is not supported");
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
      this.column =this.characterPreviousLine ;
    } else if(this.column > 0){
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
      throw raiseException("Unable to preserve stream position", e);
    }
    try {
      // -1 is EOF, but next() can not consume the null character '\0'
      if(this.reader.read() <= 0) {
        this.eof = true;
        return false;
      }
      this.reader.reset();
    } catch (IOException e) {
      throw raiseException("Unable to read the next character from the stream", e);
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
        throw raiseException(exception);
      }
    }
    if (c <= 0) { // End of stream
      this.eof = true;
      return 0;
    }
    this.incrementIndexes(c);
    this.previous = (char) c;
    return this.previous;
  }

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
        throw this.syntaxError("Failure while scanning: '"+c+"'; current-value: '"+n+"'");
      }
      throw this.syntaxError("Failure while scanning: '" + c + "'; current-value: ''");
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
        throw this.syntaxError("Substring bounds error");
      }
      pos += 1;
    }
    return new String(chars);
  }

  /**
   * Scans text until the TextScannerControl signals task complete.
   *
   * if the text stream ends before the control returns a syntax error will be thrown.
   * if the controller signals early exit with a control character match or 1 based position equality with {@link Method#max}, scanning will stop, and the next
   * stream token will be the current token.
   *
   * @param scanMethod the scan controller to use.
   * @return the scanned text
   * @throws SyntaxError
   */
  public String scan(Method scanMethod) throws SyntaxError {
    char c; int i = 0;
    char lookbehind = 0;
    boolean backslash = false, matched = false;
    StringBuilder scanned = new StringBuilder();
    do {
      if ((c = this.scanNext()) == 0) throw this.syntaxError("Expected '"+scanMethod+"'");
      if (c == '\\') backslash = ! backslash;
      if (scanMethod.max == ++i || (matched = scanMethod.matchBoundary(c))){
        if (matched && scanMethod.eatEscapes && lookbehind == '\\');
        else {
          if (! scanMethod.acceptBoundary ) this.back();
          break;
        }
      }
      if (lookbehind == '\\') {
        if (matched) scanned.setLength(scanned.length() - 1);
        backslash = false;
      }
      scanned.append(lookbehind = c);
    } while (scanMethod.continueScanning(scanned, this));
    return scanned.toString();
  }

  /**
   * Works like scan, but restores the stream and returns nothing if the operation fails.
   *
   * @param seekMethod
   * @return the text up to but not including the control break.
   * @throws Exception if an IOException occurs
   */
  public String seek(Method seekMethod) throws Exception {
    char c = 0;
    StringBuilder scanned = new StringBuilder();
    try {
      long startIndex = this.index;
      long startCharacter = this.column;
      long startLine = this.line;
      this.reader.mark(1000000);
      int i = 0; boolean backslash = false, matched = false; char lookBehind = 0;
      do {
        c = this.scanNext();
        if (c == '\\') backslash = ! backslash;
        if (seekMethod.max == ++i) c = 0;
        if ((matched = seekMethod.matchBoundary(c)) && (seekMethod.eatEscapes?! backslash:true)) c = 0;
        if (c == 0) {
          // in some readers, reset() may throw an exception if
          // the remaining portion of the input is greater than
          // the mark size (1,000,000 above).
          this.reader.reset();
          this.index = startIndex;
          this.column = startCharacter;
          this.line = startLine;
          scanned.setLength(0);
          return "";
        }
        if (lookBehind == '\\') {
          if (matched) scanned.setLength(scanned.length() - 1);
          backslash = false;
        }
        scanned.append(lookBehind = c);
      } while (seekMethod.continueScanning(scanned, this));
      this.reader.mark(1);
    } catch (IOException exception) { throw raiseException(exception); }
    if (! seekMethod.acceptBoundary) {
      scanned.setLength(scanned.length() - 1);
      this.back();
    }
    return scanned.toString();
  }

  public Exception raiseException(String message, Throwable exception){
    return exceptionMarshal.raiseException(message, exception);
  }

  public Exception raiseException(String message){
    return exceptionMarshal.raiseException(message);
  }

  public Exception raiseException(Throwable exception){
    return exceptionMarshal.raiseException(exception);
  }

  /**
   * Make a SourceProcessorException to signal a syntax error.
   *
   * @param message The error message.
   * @return  A SourceProcessorException object, suitable for throwing
   */
  public SyntaxError syntaxError(String message) {
    return exceptionMarshal.raiseSyntaxError(message + this.toTraceString());
  }

  /**
   * Make a SourceProcessorException to signal a syntax error.
   *
   * @param message The error message.
   * @param causedBy The throwable that caused the error.
   * @return  A SourceProcessorException object, suitable for throwing
   */
  public SyntaxError syntaxError(String message, Throwable causedBy) {
    return exceptionMarshal.raiseSyntaxError(message + this.toTraceString(), causedBy);
  }

  /**
   * Make a printable string of this SourceProcessor.
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

  void setExceptionMarshal(ExceptionMarshal marshal){
    this.exceptionMarshal = marshal;
  }

  public static class ExceptionMarshal {

    public Exception raiseException(String message) {
      return new Exception(message);
    }
    public Exception raiseException(Throwable causedBy){
      return new Exception(causedBy);
    }
    public Exception raiseException(String message, Throwable causedBy){
      return new Exception(message, causedBy);
    }

    public SyntaxError raiseSyntaxError(String message, Throwable causedBy) {
      return new SyntaxError(message, causedBy);
    }
    public SyntaxError raiseSyntaxError(String message){
      return new SyntaxError(message);
    }

  }

  public static final class ASCII{

    public final static char[] MAP_WHITE_SPACE = new RangeMap.Assembler(9, 13).merge(20).compile();
    public final static char[] MAP_LETTERS = new RangeMap.Assembler(65, 90).merge(97, 122).compile();
    public final static char[] MAP_NUMBERS = new RangeMap(48, 57).compile();
    public final static char[] MAP_CONTROL = new RangeMap.Assembler(0, 32).filter(MAP_WHITE_SPACE).compile();
    public final static char[] MAP_EXTENDED = new RangeMap(127, UBER_MAX).compile();

    public final static char[] MAP_SYMBOLS = new RangeMap.Assembler(33, 47)
        .merge(58, 64)
        .merge(91, 96)
        .merge(123, 126)
        .compile();

  }

  public static class RangeMap {

    public final int start, end;
    public RangeMap(int start, int end){
      this.start = normalizeRangeValue(start); this.end = normalizeRangeValue(end);
    }
    public boolean match(char character) {
      return character < start || character > end;
    }
    public char[] compile(){
      return buildRangeMap(this);
    }

    public static class Assembler {
      List<Character> chars = new ArrayList<>();
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
        for (int i:integer) chars.add((char) normalizeRangeValue(i));
      }
      public Assembler merge(int... integer){
        for (int i:integer) chars.add((char) normalizeRangeValue(i));
        return this;
      }
      public Assembler merge(int start, int end){
        return merge(new RangeMap(start, end));
      }
      public Assembler merge(RangeMap map){
        return merge(map.compile());
      }
      public Assembler merge(char... map){
        for (char c: map) chars.add(c);
        return this;
      }
      public Assembler filter(int... integer){
        for (int i:integer) chars.remove((char) normalizeRangeValue(i));
        return this;
      }
      public Assembler filter(int start, int end){
        return filter(new RangeMap(start, end));
      }
      public Assembler filter(RangeMap map){
        return filter(map.compile());
      }
      public Assembler filter(char... map){
        for (char c: map) chars.remove(c);
        return this;
      }

      char[] compile(){
        return toString().toCharArray();
      }

      @Override
      public String toString() {
        Character[] out = new Character[chars.size()];
        chars.toArray(out);
        return out.toString();
      }

    }

  }

  public static class Method implements TextScannerDriver, CharacterBoundaryControl, Serializable, Cloneable {

    private static final long serialVersionUID = -7389459770461075270L;
    private static final String undefined = "undefined";

    protected int max = 0;
    protected boolean acceptBoundary, eatEscapes;
    protected final String expectation;

    public Method(){this(null);}
    public Method(@Nullable Object expectation){ this.expectation = String.valueOf(Tools.makeNotNull(expectation, undefined)); }

    @Override public boolean continueScanning(StringBuilder input, TextScannerContext textScanner) { return true; }
    @Override public boolean matchBoundary(char character) { return character != 0; }
    @Override public String toString() { return expectation; }

    @Override
    protected Object clone() {
      try /* bake-cookies ignoring exceptions with final closure */ {
        return super.clone();
      } catch (CloneNotSupportedException fatal){throw new RuntimeException(fatal);}
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

  public static interface CharacterBoundaryControl {
    boolean matchBoundary(char character);
  }
}
