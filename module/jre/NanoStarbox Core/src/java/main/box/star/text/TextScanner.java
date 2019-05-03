package box.star.text;

import box.star.Tools;
import box.star.contract.Nullable;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TextScanner implements Iterable<Character>, TextScannerServicePort {

  private ExceptionMarshal exceptionMarshal = new ExceptionMarshal();

  public static int atLeastZero(int val){ return (val < 0)?0:val; }
  public static int atMostCharMax(int val){ return (val > '\uffff')?'\uffff':val; }
  public static int normalizeRangeValue(int val){ return atLeastZero(atMostCharMax(val));}

  public static boolean charListContains(char search, char[] range){
    for (char c: range)
      if (search == c) return true;
    return false;
  }

  public static char[] filterCharList(char[] source, char[] filter){
    List<Character> build = new ArrayList<>(source.length);
    for (char c:source){
      if (charListContains(c, filter)) continue;
      build.add(c);
    }
    Character[] out = new Character[build.size()];
    return out.toString().toCharArray();
  }

  public static char[] mergeCharLists(char[] a, char[] b){
    int i = 0;
    char[] out = new char[a.length + b.length];
    for(char c:a) out[i++] = c;
    for(char c:b) out[i++] = c;
    return out;
  }

  public static char[] selectCharList(TextScannerRangeMap range){
    List<Character> list = new ArrayList<>(range.end - range.start);
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
   * @throws TextScannerException Thrown if trying to step back more than 1 step
   *  or if already at the start of the string
   */
  protected void back() throws TextScannerException {
    if (this.usePrevious || this.index <= 0) {
      throw exceptionMarshal.raiseException("Stepping back two steps is not supported");
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
   * @throws TextScannerException thrown if there is an error stepping forward
   *  or backward while checking for more data.
   */
  public boolean hasNext() throws TextScannerException {
    if(this.usePrevious) {
      return true;
    }
    try {
      this.reader.mark(1);
    } catch (IOException e) {
      throw exceptionMarshal.raiseException("Unable to preserve stream position", e);
    }
    try {
      // -1 is EOF, but next() can not consume the null character '\0'
      if(this.reader.read() <= 0) {
        this.eof = true;
        return false;
      }
      this.reader.reset();
    } catch (IOException e) {
      throw exceptionMarshal.raiseException("Unable to read the next character from the stream", e);
    }
    return true;
  }

  /**
   * Get the next character in the source string.
   *
   * @return The next character, or 0 if past the end of the source string.
   * @throws TextScannerException Thrown if there is an error reading the source string.
   */
  public char scanNext() throws TextScannerException {
    int c;
    if (this.usePrevious) {
      this.usePrevious = false;
      c = this.previous;
    } else {
      try {
        c = this.reader.read();
      } catch (IOException exception) {
        throw exceptionMarshal.raiseException(exception);
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
   * @throws TextScannerException if the character does not match.
   */
  public char scanExact(char c) throws TextScannerException {
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
   * @throws TextScannerException
   *   Substring bounds error if there are not
   *   n characters remaining in the source string.
   */
  public String scanLength(int n) throws TextScannerException {
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

  public String scanRange(int floor, int ceiling){
    StringBuilder sb = new StringBuilder();
    int f = normalizeRangeValue(floor), m = normalizeRangeValue(ceiling);
    for (;;) {
      char c = this.scanNext();
      if (c == 0 || c < f || c > m) {
        if (c != 0) {
          this.back();
        }
        return sb.toString().trim();
      }
      sb.append(c);
    }
  }

  public String scanFloor(int floor){ return scanRange(floor, '\uffff'); }
  public String scanCeiling(int ceiling){ return scanRange(0, Math.min(ceiling, '\uffff')); }

//  /**
//   * Return the characters up to the next close quote character.
//   * Backslash processing is done. The formal JSON format does not
//   * allow strings in single quotes, but an implementation is allowed to
//   * accept them.
//   * @param quote The quoting character, either
//   *      <code>"</code>&nbsp;<small>(double quote)</small> or
//   *      <code>'</code>&nbsp;<small>(single quote)</small>.
//   * @return      A String.
//   * @throws TextScannerSyntaxError Unterminated string.
//   */
//  public String scanQuotedString(char quote) throws TextScannerSyntaxError {
//    char c;
//    StringBuilder sb = new StringBuilder();
//    for (;;) {
//      c = this.next();
//      switch (c) {
//        case 0:
//        case '\n':
//        case '\r':
//          throw this.syntaxError("Unterminated string");
//        case '\\':
//          c = this.next();
//          switch (c) {
//            case 'b':
//              sb.append('\b');
//              break;
//            case 't':
//              sb.append('\t');
//              break;
//            case 'n':
//              sb.append('\n');
//              break;
//            case 'f':
//              sb.append('\f');
//              break;
//            case 'r':
//              sb.append('\r');
//              break;
//            case 'u':
//              try {
//                sb.append((char)Integer.parseInt(this.select(4), 16));
//              } catch (NumberFormatException e) {
//                throw this.syntaxError("Illegal escape.", e);
//              }
//              break;
//            case '"':
//            case '\'':
//            case '\\':
//            case '/':
//              sb.append(c);
//              break;
//            default:
//              throw this.syntaxError("Illegal escape.");
//          }
//          break;
//        default:
//          if (c == quote) {
//            return sb.toString();
//          }
//          sb.append(c);
//      }
//    }
//  }

//  /**
//   * Get the text up but not including the specified delimiter match
//   * or the specified TextPattern delimiter escape sequence or the end of the text stream, whichever comes first.
//   * @param delimiter A character delimiter TextPattern.
//   * @return A string.
//   * @throws TextScannerException Thrown if there is an error while searching
//   *  for the delimiter
//   */
//  public String scanField(TextPattern delimiter) throws TextScannerException {
//    String c;
//    StringBuilder sb = new StringBuilder();
//    for (;;) {
//      c = this.next() + "";
//      if (delimiter.match(c) || ! delimiter.continueScanning(sb, this)) {
//        if (c.indexOf(0) != 0) {
//          this.back();
//        }
//        return sb.toString();
//      }
//      sb.append(c);
//    }
//  }
//
//  /**
//   * Scan characters until the TextPattern matches the next char.
//   * If the TextPattern declines to continue scanning, the operation will be aborted.
//   *
//   * @param control A single-character TextPattern.
//   * @return The text up to but not including delimiter, or an empty string if matching fails or is aborted.
//   *
//   * @throws TextScannerException
//   */
//  public String scanSeek(TextPattern control) throws TextScannerException {
//    char c;
//    StringBuilder scanned = new StringBuilder();
//    try {
//      long startIndex = this.index;
//      long startCharacter = this.column;
//      long startLine = this.line;
//      this.reader.mark(1000000);
//      do {
//        c = this.next();
//        if (this.index > startIndex)
//          if (! control.continueScanning(scanned, this)) c = 0;
//        if (c == 0) {
//          // in some readers, reset() may throw an exception if
//          // the remaining portion of the input is greater than
//          // the mark size (1,000,000 above).
//          this.reader.reset();
//          this.index = startIndex;
//          this.column = startCharacter;
//          this.line = startLine;
//          return "";
//        }
//        scanned.append(c);
//      } while (! control.match(String.valueOf(c)) );
//      scanned.setLength(scanned.length() - 1);
//      this.reader.mark(1);
//    } catch (IOException exception) {
//      throw exceptionMarshal.raiseException(exception);
//    }
//    this.back();
//    return scanned.toString();
//  }
//
//
//  /**
//   * Scans text until pattern matches the input buffer.
//   * if the TextPattern declines to continue scanning, the current buffer is returned.
//   *
//   * if the text stream ends before the match succeeds a syntax error will be thrown.
//   *
//   * @param  textPattern use: {@link TextPattern}
//   *
//   * @return the matched text
//   * @throws TextScannerSyntaxError
//   */
//  public String scanMatch(TextPattern textPattern) throws TextScannerSyntaxError {
//    char c; int length = 0;
//    StringBuilder scanned = new StringBuilder();
//    do {
//      if ((c = this.next()) == 0) throw this.syntaxError("Expected '"+textPattern.getLabel()+"'");
//      scanned.append(c); ++length;
//      if (textPattern.match(scanned.subSequence(0, length))) break;
//    } while (textPattern.continueScanning(scanned, this));
//    return scanned.toString();
//  }

  /**
   * Scans text until the TextScannerControl signals task complete.
   *
   * if the text stream ends before the control returns a syntax error will be thrown.
   * if the controller signals early exit with a control character match or 1 based position equality with {@link TextScannerPort#max}, scanning will stop, and the next
   * stream token will be the current token.
   *
   * @param control the scan controller to use.
   * @return the scanned text
   * @throws TextScannerSyntaxError
   */
  public String scan(TextScannerPort control) throws TextScannerSyntaxError {
    char c; int i = 0;
    StringBuilder scanned = new StringBuilder();
    do {
      if ((c = this.scanNext()) == 0) throw this.syntaxError("Expected '"+control.getExpectation()+"'");
      ++i;
      if (control.max == i || control.matchBreak(c)){ this.back(); break; }
      scanned.append(c);
    } while (control.continueScanning(scanned, this));
    return scanned.toString();
  }

  /**
   * Works like scan, but restores the stream and returns nothing if the operation fails.
   *
   * @param control
   * @return the text up to but not including the control break.
   * @throws TextScannerException if an IOException occurs
   */
  public String seek(TextScannerPort control) throws TextScannerException {
    char c;
    StringBuilder scanned = new StringBuilder();
    try {
      long startIndex = this.index;
      long startCharacter = this.column;
      long startLine = this.line;
      this.reader.mark(1000000);
      int i = 0;
      do {
        c = this.scanNext();
        ++i;
        if (control.max == i || control.matchBreak(c)) c = 0;
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
        scanned.append(c);
      } while (control.continueScanning(scanned, this));
      scanned.setLength(scanned.length() - 1);
      this.reader.mark(1);
    } catch (IOException exception) {
      throw exceptionMarshal.raiseException(exception);
    }
    this.back();
    return scanned.toString();
  }

  /**
   * Make a SourceProcessorException to signal a syntax error.
   *
   * @param message The error message.
   * @return  A SourceProcessorException object, suitable for throwing
   */
  public TextScannerSyntaxError syntaxError(String message) {
    return exceptionMarshal.raiseSyntaxError(message + this.toTraceString());
  }

  /**
   * Make a SourceProcessorException to signal a syntax error.
   *
   * @param message The error message.
   * @param causedBy The throwable that caused the error.
   * @return  A SourceProcessorException object, suitable for throwing
   */
  public TextScannerSyntaxError syntaxError(String message, Throwable causedBy) {
    return exceptionMarshal.raiseSyntaxError(message + this.toTraceString(), causedBy);
  }

  /**
   * Make a printable string of this SourceProcessor.
   *
   * @return " at source character-position: {@link #index} = {line: {@link #line}, column: {@link #column}}"
   */
  public String toTraceString() {
    String source = " at " + Tools.makeNotNull(sourceLabel, "source") + " character-position: ";
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

    public TextScannerException raiseException(String message) {
      return new TextScannerException(message);
    }
    public TextScannerException raiseException(Throwable causedBy){
      return new TextScannerException(causedBy);
    }
    public TextScannerException raiseException(String message, Throwable causedBy){
      return new TextScannerException(message, causedBy);
    }

    public TextScannerSyntaxError raiseSyntaxError(String message, Throwable causedBy) {
      return new TextScannerSyntaxError(message, causedBy);
    }
    public TextScannerSyntaxError raiseSyntaxError(String message){
      return new TextScannerSyntaxError(message);
    }

  }

}
