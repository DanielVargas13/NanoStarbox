package box.star.text;

import box.star.contract.NotNull;
import jdk.nashorn.internal.objects.NativeRangeError;
import org.w3c.dom.ranges.RangeException;

import java.io.Serializable;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.stream.IntStream;

public final class Char {

  public final static int CHAR_MAX = '\uffff';

  public final static char NULL_CHARACTER = 0;

  public final static char META_DOCUMENT_TAG_START = '<';
  public final static char META_DOCUMENT_TAG_END = '>';

  public final static char BACKSLASH = '\\';
  public final static char SINGLE_QUOTE = '\'';
  public final static char DOUBLE_QUOTE = '"';
  public final static char SOLIDUS = '/';
  public final static char SPACE = ' ';
  public final static char
  START_OF_HEADING = 1,
  START_OF_TEXT = 2,
  END_OF_TEXT = 3,
  END_OF_TRANSMISSION = 4,
  ENQUIRY = 5,
  ACKNOWLEDGEMENT = 6,
  BELL = 7,
  BACKSPACE = 8,
  HORIZONTAL_TAB = 9,
  LINE_FEED = 10,
  VERTICAL_TAB = 11,
  FORM_FEED = 12,
  CARRIAGE_RETURN = 13,
  SHIFT_OUT = 14,
  SHIFT_IN = 15,
  DATA_LINK_ESCAPE = 16,
  DEVICE_CONTROL_1 = 17,
  DEVICE_CONTROL_2 = 18,
  DEVICE_CONTROL_3 = 19,
  DEVICE_CONTROL_4 = 20,
  NEGATIVE_ACKNOWLEDGEMENT = 21,
  SYNCHRONOUS_IDLE = 22,
  END_OF_TRANSMISSION_BLOCK = 23,
  CANCEL = 24,
  END_OF_MEDIUM = 25,
  SUBSTITUTE = 26,
  ESCAPE = 27,
  FILE_SEPARATOR = 28,
  GROUP_SEPARATOR = 29,
  RECORD_SEPARATOR = 30,
  UNIT_SEPARATOR = 31,
  DELETE = 127;

  public final static char[] MAP = new Assembler(NULL_CHARACTER, CHAR_MAX).toArray();
  public final static char[] MAP_ALL_WHITE_SPACE = new Assembler(9, 13).merge(SPACE).toArray();
  public final static char[] MAP_LINE_WHITE_SPACE = new Assembler(MAP_ALL_WHITE_SPACE).filter(LINE_FEED, CARRIAGE_RETURN).toArray();
  public final static char[] MAP_LETTERS = new Assembler(65, 90).merge(97, 122).toArray();
  public final static char[] MAP_NUMBERS = new Assembler.RangeMap(48, 57).compile();
  public final static char[] MAP_CONTROL = new Assembler(NULL_CHARACTER, 31).merge(DELETE).filter(MAP_ALL_WHITE_SPACE).toArray();
  public final static char[] MAP_EXTENDED = new Assembler.RangeMap(128, CHAR_MAX).compile();
  public final static char[] MAP_SYMBOLS = new Assembler(33, 47).merge(58, 64).merge(91, 96).merge(123, 126).toArray();

  private Char() {}

  private final static Hashtable<Character, String> TRANSLATION = new Hashtable<>(10);

  public static String translate(char c, String translation){
    Char.TRANSLATION.put(c, translation);
    return translation;
  }

  public static String translate(char c){
    if (c == 0) return "null";
    if (TRANSLATION.containsKey(c)){
      return TRANSLATION.get(c);
    }
    return String.valueOf(c);
  }

  static {
    translate(ESCAPE, "escape");
    translate(BELL, "bell");
    translate(BACKSPACE, "backspace");
    translate(FORM_FEED, "form-feed (\\f)");
    translate(VERTICAL_TAB, "vertical-tab (\\v)");
    translate(HORIZONTAL_TAB, "tab (\\t)");
    translate(BACKSLASH, "backslash (\\)");
    translate(LINE_FEED, "line-feed (\\n)");
    translate(CARRIAGE_RETURN, "carriage-return (\\r)");
    translate(SPACE, "space");
  }

  public static int atLeastZero(int val) { return (val < 0) ? 0 : val; }

  public static int atMostCharMax(int val) { return (val > CHAR_MAX) ? '\uffff' : val; }

  public static int sanitizeRangeValue(int val) { return atLeastZero(atMostCharMax(val));}

  public static boolean mapContains(char search, char[] range) {
    for (int i = 0; i < range.length; i++) if (range[i] == search) return true;
    return false;
  }

  static char[] buildRangeMap(Assembler.RangeMap range) {
    StringBuilder out = new StringBuilder();
    for (int i = range.start; i <= range.end; i++) out.append((char) i);
    return out.toString().toCharArray();
  }

  public static int parseInt(char c) {
    return Integer.parseInt(c + "");
  }

  public static int parseInt(char c, int base) {
    return Integer.parseInt(c + "", base);
  }

  /**
   * Get the hex value of a character (base16).
   *
   * @param c A character between '0' and '9' or between 'A' and 'F' or
   *          between 'a' and 'f'.
   * @return An int between 0 and 15, or -1 if c was not a hex digit.
   */
  public static int parseHex(char c) {
    if (c >= '0' && c <= '9') {
      return c - '0';
    }
    if (c >= 'A' && c <= 'F') {
      return c - ('A' - 10);
    }
    if (c >= 'a' && c <= 'f') {
      return c - ('a' - 10);
    }
    return -1;
  }

  public interface Scanner<MainClass extends Scanner> {

    /**
     * Get a snapshot of the current scanner.
     * <p>
     * A snapshot stores the current state of the scanner, and manipulates
     * the underlying reader, to create a virtual read-back-buffer.
     *
     * @return {@link Snapshot}
     */
    <ANY extends Snapshot> ANY getSnapshot();

    /**
     * Determine if the source string still contains characters that next()
     * can consume.
     *
     * @return true if not yet at the end of the source.
     * @throws Exception thrown if there is an error stepping forward
     *                   or backward while checking for more data.
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
     *
     * @return
     * @throws Exception
     */
    char next() throws Exception;

    char nextCharacter(char character, boolean caseSensitive);

    /**
     * Read the given source or throw an error.
     *
     * @param source
     * @return
     */
    @NotNull String nextString(@NotNull String source, boolean caseSensitive);

    /**
     * Assemble characters while character match map.
     *
     * @param map
     * @return
     */
    @NotNull String nextMap(char... map);

    /**
     * Return the characters up to the next close quote character.
     * Backslash processing is done.
     *
     * @param quote The quoting character, either
     *              <code>"</code>&nbsp;<small>(double quote)</small> or
     *              <code>'</code>&nbsp;<small>(single quote)</small>.
     * @return A String.
     * @throws Exception Unterminated string.
     */
    @NotNull String nextQuote(char quote, boolean multiLine) throws Exception;

    /**
     * Make a printable string of this Scanner.
     *
     * @return " at {index} [character {character} line {line}]"
     */
    String scope();

    String getPath();

    long getIndex();

    long getLine();

    long getColumn();

    /**
     * Returns true if this character is the start of a backslash escape.
     *
     * @return
     */
    boolean haveEscape();

    @NotNull String run(@NotNull Char.Scanner.Method<MainClass> method, Object... parameters);

    /**
     * Make a Exception to signal a syntax error.
     *
     * @param message The error message.
     * @return A Exception object, suitable for throwing
     */
    SyntaxError syntaxError(@NotNull String message);

    /**
     * Make a Exception to signal a syntax error.
     *
     * @param message  The error message.
     * @param causedBy The throwable that caused the error.
     * @return A Exception object, suitable for throwing
     */
    SyntaxError syntaxError(@NotNull String message, @NotNull Throwable causedBy);

    /**
     * Determines if the given character starts, continues or ends a quote stream.
     *
     * @param character
     * @return
     */
    boolean parseQuotation(char character);

    @NotNull String nextField(@NotNull char... map) throws java.lang.Exception;

    @NotNull String nextLength(int n) throws java.lang.Exception;

    boolean isQuoting();

    interface Method<MainClass extends Scanner> extends Cloneable {

      void collect(TextScanner scanner, char character);

      /**
       * Return true to break processing on this character.
       *
       * @param character
       * @return false to continue processing.
       */
      boolean terminator(@NotNull MainClass context, char character);

      void reset();

      /**
       * Called by the TextScanner to signal that a new method call is beginning.
       *
       * @param parameters the parameters given by the caller.
       */
      void start(@NotNull MainClass context, Object[] parameters);

      /**
       * Compiles the scanned character buffer.
       *
       * @param context the host context.
       * @return the scanned data as a string.
       */
      String compile(@NotNull MainClass context);

      /**
       * Signals whether or not the process should continue reading input.
       *
       * @return true if the TextScanner should read more input.
       */
      boolean scanning(@NotNull MainClass context);

      void back(TextScanner scanner);
    }

    /**
     * Scanner Snapshot Interface
     * <p>
     * An interface to cancel or close a snapshot.
     * <p>
     * A snapshot allows code to implement a rewind interface.
     * <p>
     * to rewind use {@link Snapshot#cancel()}
     * to save use {@link Snapshot#close()}
     */
    interface Snapshot {
      /**
       * Cancel the current snapshot.
       *
       * @throws Exception if the operation fails
       */
      void cancel() throws Exception;

      /**
       * Close the current snapshot.
       */
      void close();
    }

    /**
     * The TextScanner.Exception is thrown by the TextScanner interface classes when things are amiss.
     *
     * @author Hypersoft-Systems: USA
     * @version 2015-12-09
     */
    class Exception extends RuntimeException {
      /**
       * Serialization ID
       */
      private static final long serialVersionUID = 0;

      /**
       * Constructs a TextScanner.Exception with an explanatory message.
       *
       * @param message Detail about the reason for the exception.
       */
      public Exception(final String message) {
        super(message);
      }

      /**
       * Constructs a TextScanner.Exception with an explanatory message and cause.
       *
       * @param message Detail about the reason for the exception.
       * @param cause   The cause.
       */
      public Exception(final String message, final Throwable cause) {
        super(message, cause);
      }

      /**
       * Constructs a new TextScanner.Exception with the specified cause.
       *
       * @param cause The cause.
       */
      public Exception(final Throwable cause) {
        super(cause.getMessage(), cause);
      }

    }

    /**
     * The TextScanner.Exception is thrown by the TextScanner interface classes when things are amiss.
     *
     * @author Hypersoft-Systems: USA
     */
    class SyntaxError extends RuntimeException {
      /**
       * Serialization ID
       */
      private static final long serialVersionUID = 0;

      /**
       * Constructs a TextScanner.Exception with an explanatory message.
       *
       * @param message Detail about the reason for the exception.
       */
      public SyntaxError(final String message) {
        super(message);
      }

      /**
       * Constructs a TextScanner.Exception with an explanatory message and cause.
       *
       * @param message Detail about the reason for the exception.
       * @param cause   The cause.
       */
      public SyntaxError(final String message, final Throwable cause) {
        super(message, cause);
      }

      /**
       * Constructs a new TextScanner.Exception with the specified cause.
       *
       * @param cause The cause.
       */
      public SyntaxError(final Throwable cause) {
        super(cause.getMessage(), cause);
      }

    }
  }

  public static class Assembler implements Serializable, Iterable<Character>, CharSequence {

    private static final long serialVersionUID = 8454376662352328447L;
    StringBuilder chars = new StringBuilder();

    public Assembler(){};

    public Assembler(CharSequence sequence){
      merge(sequence.toString());
    }

    public Assembler(Iterable<Character> stream){
      merge(stream);
    }

    public Assembler(char start, char finish){
      this((int) start, (int) finish);
    }

    public Assembler(RangeMap map) {
      this(map.compile());
    }

    public Assembler(char... map) {
      merge(map);
    }

    public Assembler(int start, int end) {
      if (start > end) throw new RuntimeException("RangeError: start is greater than end");
      merge(new RangeMap(start, end));
    }

    public Assembler(int... integer) {
      merge(integer);
    }

    @Override
    public Iterator<Character> iterator() {
      final char[] data = this.toArray();
      return new Iterator<Character>() {
        int i = 0;
        @Override
        public boolean hasNext() { return i < data.length; }

        @Override
        public Character next() { return data[i++]; }
      };
    }

    public Assembler merge(CharSequence sequence){
      return merge(sequence.toString());
    }

    public Assembler merge(int... integer) {
      for (int i: integer){
        char c = (char) sanitizeRangeValue(i);
        if (! contains(c)) chars.append(c);
      }
      return this;
    }

    public Assembler merge(char... map) {
      for (char c : map) if (chars.indexOf(String.valueOf(c)) == -1) chars.append(c);
      return this;
    }

    public Assembler merge(Iterable<Character> stream){
      for (char c: stream) if (chars.indexOf(String.valueOf(c)) == -1) chars.append(c);
      return this;
    }

    public Assembler merge(String source){
      return merge(source.toCharArray());
    }

    public Assembler merge(int start, int end) {
      return merge(new RangeMap(start, end));
    }

    public Assembler merge(RangeMap map) {
      return merge(map.compile());
    }

    public Assembler filter(String source){
      return filter(source.toCharArray());
    }

    public Assembler filter(int... integer) {
      StringBuilder map = new StringBuilder();
      for (int i : integer) map.append((char) i);
      char[] chars = map.toString().toCharArray();
      return filter(chars);
    }

    public Assembler filter(CharSequence sequence){
      return filter(sequence.toString());
    }

    public Assembler filter(Iterable<Character> stream){
      StringBuilder out = new StringBuilder();
      for (char c: stream) out.append(c);
      return filter(out.toString().toCharArray());
    }

    public Assembler filter(int start, int end) {
      return filter(new RangeMap(start, end));
    }

    public Assembler filter(RangeMap map) {
      return filter(map.compile());
    }

    public Assembler filter(char... map) {
      StringBuilder filter = new StringBuilder();
      for (char c : chars.toString().toCharArray()) {
        if (mapContains(c, map)) continue;
        filter.append(c);
      }
      this.chars = filter;
      return this;
    }

    public char[] toArray() {
      return chars.toString().toCharArray();
    }

    /**
     * Returns the length of this character sequence.  The length is the number
     * of 16-bit <code>char</code>s in the sequence.
     *
     * @return the number of <code>char</code>s in this sequence
     */
    @Override
    public int length() {
      return chars.length();
    }

    /**
     * Returns the <code>char</code> value at the specified index.  An index ranges from zero
     * to <tt>length() - 1</tt>.  The first <code>char</code> value of the sequence is at
     * index zero, the next at index one, and so on, as for array
     * indexing.
     *
     * <p>If the <code>char</code> value specified by the index is a
     * <a href="{@docRoot}/java/lang/Character.html#unicode">surrogate</a>, the surrogate
     * value is returned.
     *
     * @param index the index of the <code>char</code> value to be returned
     * @return the specified <code>char</code> value
     * @throws IndexOutOfBoundsException if the <tt>index</tt> argument is negative or not less than
     *                                   <tt>length()</tt>
     */
    @Override
    public char charAt(int index) {
      return chars.charAt(index);
    }

    /**
     * Returns a <code>CharSequence</code> that is a subsequence of this sequence.
     * The subsequence starts with the <code>char</code> value at the specified index and
     * ends with the <code>char</code> value at index <tt>end - 1</tt>.  The length
     * (in <code>char</code>s) of the
     * returned sequence is <tt>end - start</tt>, so if <tt>start == end</tt>
     * then an empty sequence is returned.
     *
     * @param start the start index, inclusive
     * @param end   the end index, exclusive
     * @return the specified subsequence
     * @throws IndexOutOfBoundsException if <tt>start</tt> or <tt>end</tt> are negative,
     *                                   if <tt>end</tt> is greater than <tt>length()</tt>,
     *                                   or if <tt>start</tt> is greater than <tt>end</tt>
     */
    @Override
    public CharSequence subSequence(int start, int end) {
      return chars.subSequence(start, end);
    }

    @Override
    public String toString() {
      return chars.toString();
    }

    /**
     * Returns a stream of {@code int} zero-extending the {@code char} values
     * from this sequence.  Any char which maps to a <a
     * href="{@docRoot}/java/lang/Character.html#unicode">surrogate code
     * point</a> is passed through uninterpreted.
     *
     * <p>If the sequence is mutated while the stream is being read, the
     * result is undefined.
     *
     * @return an IntStream of char values from this sequence
     * @since 1.8
     */
    @Override
    public IntStream chars() {
      return chars.chars();
    }

    /**
     * Returns a stream of code point values from this sequence.  Any surrogate
     * pairs encountered in the sequence are combined as if by {@linkplain
     * Character#toCodePoint Character.toCodePoint} and the result is passed
     * to the stream. Any other code units, including ordinary BMP characters,
     * unpaired surrogates, and undefined code units, are zero-extended to
     * {@code int} values which are then passed to the stream.
     *
     * <p>If the sequence is mutated while the stream is being read, the result
     * is undefined.
     *
     * @return an IntStream of Unicode code points from this sequence
     * @since 1.8
     */
    @Override
    public IntStream codePoints() {
      return chars.codePoints();
    }

    public boolean contains(char character) {
      return chars.indexOf(character + "") != -1;
    }

    private static class RangeMap {
      public final int start, end;

      RangeMap(int start, int end) {
        this.start = sanitizeRangeValue(start);
        this.end = sanitizeRangeValue(end);
      }

      public boolean match(char character) {
        return character >= start || character <= end;
      }

      public char[] compile() {
        return buildRangeMap(this);
      }
    }

  }

}
