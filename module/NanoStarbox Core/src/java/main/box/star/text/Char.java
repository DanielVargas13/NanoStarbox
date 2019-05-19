package box.star.text;

import box.star.Tools;
import box.star.contract.NotNull;

import java.io.Serializable;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Locale;
import java.util.stream.IntStream;

public final class Char {

  public final static int CHAR_MAX = '\uffff';
  public final static char NULL_CHARACTER = 0;
  public final static char META_DOCUMENT_TAG_START = '<';
  public final static char META_DOCUMENT_TAG_END = '>';
  public final static char BACKSLASH = '\\';
  public static final char PIPE = '|';
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
  public final static char[] MAP_ASCII = new Assembler(NULL_CHARACTER, 255).toArray();
  public final static char[] MAP_ASCII_EXTENDED = new Assembler.RangeMap(128, 255).compile();
  public final static char[] MAP_ASCII_ALL_WHITE_SPACE = new Assembler(9, 13).map(SPACE).toArray();
  public final static char[] MAP_ASCII_LINE_WHITE_SPACE = new Assembler(MAP_ASCII_ALL_WHITE_SPACE).filter(LINE_FEED, CARRIAGE_RETURN).toArray();
  public final static char[] MAP_ASCII_LETTERS = new Assembler(65, 90).merge(97, 122).toArray();
  public final static char[] MAP_ASCII_NUMBERS = new Assembler.RangeMap(48, 57).compile();
  public final static char[] MAP_ASCII_CONTROL = new Assembler(NULL_CHARACTER, 31).map(DELETE).filterMap(MAP_ASCII_ALL_WHITE_SPACE).toArray();
  public final static char[] MAP_ASCII_SYMBOLS = new Assembler(33, 47).merge(58, 64).merge(91, 96).merge(123, 127).toArray();
  public final static char[] MAP_ASCII_HEX = new Assembler(MAP_ASCII_NUMBERS).merge('a', 'f').merge('A', 'F').toArray();
  public final static char[] MAP_ASCII_OCTAL = new Assembler('0', '8').toArray();
  private final static Hashtable<Locale, Hashtable<Character, String>> TRANSLATIONS = new Hashtable<>(3);
  private static Locale locale;
  private static Hashtable<Character, String> TRANSLATION;

  static {
    setLocale(Locale.ENGLISH);
    importLocaleEnglishASCII();
  }

  private Char() {}

  public static void setLocale(Locale locale) {
    Char.locale = locale;
    if (!TRANSLATIONS.containsKey(locale)) TRANSLATIONS.put(locale, new Hashtable<>((int) SPACE));
    TRANSLATION = TRANSLATIONS.get(locale);
  }

  public static char[] toLowerCase(String source) {
    return source.toLowerCase(locale).toCharArray();
  }

  public static char[] toUpperCase(String source) {
    return source.toUpperCase(locale).toCharArray();
  }

  public static void importLocaleEnglishASCII() {
    translate(DELETE, "delete (\\d)");
    translate(ESCAPE, "escape (\\e)");
    translate(BELL, "bell");
    translate(BACKSPACE, "backspace (\\b)");
    translate(FORM_FEED, "form-feed (\\f)");
    translate(VERTICAL_TAB, "vertical-tab (\\v)");
    translate(HORIZONTAL_TAB, "tab (\\t)");
    translate(BACKSLASH, "backslash (\\)");
    translate(LINE_FEED, "line-feed (\\n)");
    translate(CARRIAGE_RETURN, "carriage-return (\\r)");
    translate(SPACE, "space");
  }

  public static String translate(char c, String translation) {
    Char.TRANSLATION.put(c, translation);
    return translation;
  }

  public static String translate(char c) {
    if (c == 0) return "null";
    else if (TRANSLATION.containsKey(c)) return TRANSLATION.get(c);
    else return String.valueOf(c);
  }

  public static int atLeastZero(int val) { return (val < 0) ? 0 : val; }

  public static int atMostCharMax(int val) { return (val > CHAR_MAX) ? CHAR_MAX : val; }

  public static int sanitizeRangeValue(int val) { return atLeastZero(atMostCharMax(val));}

  public static boolean mapContains(char search, char... range) {
    for (int i = 0; i < range.length; i++) if (range[i] == search) return true;
    return false;
  }

  public static boolean stringContains(String search, char... range) {
    char[] data = search.toCharArray();
    for (char c : data) if (mapContains(c, range)) return true;
    return false;
  }

  static char[] buildRangeMap(Assembler.RangeMap range) {
    StringBuilder out = new StringBuilder();
    for (int i = range.start; i <= range.end; i++) out.append((char) i);
    return out.toString().toCharArray();
  }

  public static int parseInt(char c) {
    return Integer.parseInt(c + Tools.EMPTY_STRING);
  }

  public static int parseInt(char c, int base) {
    return Integer.parseInt(c + Tools.EMPTY_STRING, base);
  }

  public static char valueOf(int c) {
    return (char) c;
  }

  public static char[] valueOf(@NotNull String source) {
    return source.toCharArray();
  }

  public static char toLowerCase(char c) {
    return toLowerCase(c + Tools.EMPTY_STRING)[0];
  }

  public static char toUpperCase(char c) {
    return toUpperCase(c + Tools.EMPTY_STRING)[0];
  }

  public static char[] toMap(char... elements) {
    return elements;
  }

  public static String toString(char... elements) {
    return String.valueOf(elements);
  }

  public static class Assembler implements Serializable, Iterable<Character>, CharSequence {

    private static final long serialVersionUID = 8454376662352328447L;
    StringBuilder chars = new StringBuilder();

    public Assembler() {
      chars = new StringBuilder((int) SPACE);
    }

    public Assembler(int capacity) {
      chars = new StringBuilder(capacity);
    }

    public Assembler(CharSequence sequence) {
      chars = new StringBuilder(sequence.length());
      merge(sequence.toString());
    }

    public Assembler(Iterable<Character> stream) {
      merge(stream);
    }

    private Assembler(RangeMap map) {
      this(map.start, map.end);
    }

    public Assembler(char[] map) {
      chars = new StringBuilder(map.length);
      map(map);
    }

    public Assembler(int start, int end) {
      if (start > end) throw new RuntimeException("RangeError: start is greater than end");
      chars = new StringBuilder((end - start) + 2);
      merge(new RangeMap(start, end));
    }

    public Assembler(int... integer) {
      chars = new StringBuilder(integer.length);
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

    public Assembler merge(CharSequence sequence) {
      return merge(sequence.toString());
    }

    public Assembler merge(int... integer) {
      for (int i : integer) {
        char c = (char) sanitizeRangeValue(i);
        if (!contains(c)) chars.append(c);
      }
      return this;
    }

    public Assembler map(char... map) {
      for (char c : map) if (chars.indexOf(String.valueOf(c)) == -1) chars.append(c);
      return this;
    }

    public Assembler merge(Iterable<Character> stream) {
      for (char c : stream) if (chars.indexOf(String.valueOf(c)) == -1) chars.append(c);
      return this;
    }

    public Assembler merge(String source) {
      return map(source.toCharArray());
    }

    public Assembler merge(int start, int end) {
      return merge(new RangeMap(start, end));
    }

    private Assembler merge(RangeMap map) {
      return map(map.compile());
    }

    public Assembler filter(String source) {
      return filterMap(source.toCharArray());
    }

    public Assembler filter(int... integer) {
      StringBuilder map = new StringBuilder(chars.length());
      for (int i : integer) map.append((char) i);
      char[] chars = map.toString().toCharArray();
      return filterMap(chars);
    }

    public Assembler filter(CharSequence sequence) {
      return filter(sequence.toString());
    }

    public Assembler filter(Iterable<Character> stream) {
      StringBuilder out = new StringBuilder(chars.length());
      for (char c : stream) out.append(c);
      return filterMap(out.toString().toCharArray());
    }

    public Assembler filter(int start, int end) {
      return filter(new RangeMap(start, end));
    }

    public Assembler filter(RangeMap map) {
      return filterMap(map.compile());
    }

    public Assembler filterMap(char... map) {
      StringBuilder filter = new StringBuilder(chars.length());
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
      return chars.indexOf(character + Tools.EMPTY_STRING) != -1;
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
