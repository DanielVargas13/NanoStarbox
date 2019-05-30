package box.star.text;

import box.star.Tools;
import box.star.contract.NotNull;
import box.star.io.Streams;

import java.io.InputStream;
import java.io.Serializable;
import java.util.*;
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

  private static char[] loadResourceMap(String name){
    InputStream stream = Streams.getResourceAsStream("box/star/text/optimization/"+name);
    char[] map = new char[0];
    try {
      map = (char[]) Streams.readSerializable(stream);
      stream.close();
    }
    catch (java.lang.Exception ignored) {}
    return map;
  }

  public final static char[] MAP_ASCII = loadResourceMap("MAP_ASCII");
  public final static char[] MAP_ASCII_EXTENDED = loadResourceMap("MAP_ASCII_EXTENDED");
  public final static char[] MAP_ASCII_ALL_WHITE_SPACE = loadResourceMap("MAP_ASCII_ALL_WHITE_SPACE");
  public final static char[] MAP_ASCII_LINE_WHITE_SPACE = loadResourceMap("MAP_ASCII_LINE_WHITE_SPACE");
  public final static char[] MAP_ASCII_LETTERS = loadResourceMap("MAP_ASCII_LETTERS");
  public final static char[] MAP_ASCII_NUMBERS = loadResourceMap("MAP_ASCII_NUMBERS");
  public final static char[] MAP_ASCII_CONTROL = loadResourceMap("MAP_ASCII_CONTROL");
  public final static char[] MAP_ASCII_SYMBOLS = loadResourceMap("MAP_ASCII_SYMBOLS");
  public final static char[] MAP_ASCII_HEX = loadResourceMap("MAP_ASCII_HEX");
  public final static char[] MAP_ASCII_OCTAL = loadResourceMap("MAP_ASCII_OCTAL");

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
    translate(DELETE, "delete");
    translate(ESCAPE, "escape");
    translate(BELL, "bell");
    translate(BACKSPACE, "backspace");
    translate(FORM_FEED, "form-feed");
    translate(VERTICAL_TAB, "vertical-tab");
    translate(HORIZONTAL_TAB, "tab");
    translate(BACKSLASH, "backslash");
    translate(LINE_FEED, "line-feed");
    translate(CARRIAGE_RETURN, "carriage-return");
    translate(SPACE, "space");
  }

  public static String translate(char c, String translation) {
    Char.TRANSLATION.put(c, translation);
    return translation;
  }

  public static String translate(char c) {
    if (c == 0) return "null";
    else return Char.TRANSLATION.getOrDefault(c, String.format("`%s'", c));
  }

  public static int min(int val) { return (val < 0) ? 0 : val; }

  public static int max(int val) { return (val > CHAR_MAX) ? CHAR_MAX : val; }

  public static int normalize(int val) { return min(max(val));}

  public static boolean mapContains(char search, char... map) {
    for (int i = 0; i < map.length; i++) if (map[i] == search) return true;
    return false;
  }

  public static boolean stringContains(String search, char... range) {
    char[] data = search.toCharArray();
    for (char c : data) if (mapContains(c, range)) return true;
    return false;
  }

  static char[] buildRangeMap(RangeMap range) {
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

    public Assembler(RangeMap sequence) {
      this(sequence.toMap());
    }

    public Assembler(Char.Map map){
      this(map.toMap());
    }

    public Assembler(char... map) {
      chars = new StringBuilder(map.length);
      merge(map);
    }

    @Override
    public Iterator<Character> iterator() {
      final char[] data = this.toMap();
      return new Iterator<Character>() {
        int i = 0;

        @Override
        public boolean hasNext() { return i < data.length; }

        @Override
        public Character next() { return data[i++]; }
      };
    }

    public Assembler merge(Char.Map map){
      return this.merge(map.toMap());

    }
    public Assembler merge(CharSequence sequence) {
      return merge(sequence.toString());
    }

    public Assembler merge(char... map) {
      for (char c : map) if (chars.indexOf(String.valueOf(c)) == -1) chars.append(c);
      return this;
    }

    public Assembler merge(Iterable<Character> stream) {
      for (char c : stream) if (chars.indexOf(String.valueOf(c)) == -1) chars.append(c);
      return this;
    }

    public Assembler merge(String source) {
      return merge(source.toCharArray());
    }

    public Assembler merge(RangeMap map) {
      return merge(map.toMap());
    }

    public Assembler filter(String source) {
      return filter(source.toCharArray());
    }

    public Assembler filter(CharSequence sequence) {
      return filter(sequence.toString());
    }

    public Assembler filter(Iterable<Character> stream) {
      StringBuilder out = new StringBuilder(chars.length());
      for (char c : stream) out.append(c);
      return filter(out.toString().toCharArray());
    }

    public Assembler filter(RangeMap map) {
      return filter(map.toMap());
    }

    public Assembler filter(char... map) {
      StringBuilder filter = new StringBuilder(chars.length());
      for (char c : chars.toString().toCharArray()) {
        if (mapContains(c, map)) continue;
        filter.append(c);
      }
      this.chars = filter;
      return this;
    }

    public char[] toMap() {
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

  }

  public static class RangeMap {
    public final int start, end;
    public RangeMap(int start, int end) {
      this.start = normalize(start);
      this.end = normalize(end);
    }

    public boolean match(char character) {
      return character >= start || character <= end;
    }
    public char[] toMap() {
      return buildRangeMap(this);
    }
  }

  public static String mapToTranslation(String conjunction, char... map){
    if (map.length == 1) return translate(map[0]);
    String[] out = new String[map.length];
    int i; for (i = 0; i < map.length - 1; i++) out[i] = translate(map[i]);
    out[i++] = conjunction+" "+translate(map[map.length - 1]);
    return String.join(", ", out);
  }

  public static class Map {
    private char[] map, lowerCaseMap;
    private String label;
    public Map(String label, Assembler assembler){
      this(label, assembler.toMap());
    }
    public Map(String label, char... map){
      this.label = label; this.map = map;
    }
    public boolean contains(char c){
      for (char t: map) if (c == t) return true;
      return false;
    }
    public boolean containsIgnoreCase(char c){
      if (lowerCaseMap == null) {
        lowerCaseMap = new char[map.length];
        for (int i = 0; i < lowerCaseMap.length; i++)
          lowerCaseMap[i] = Char.toLowerCase(map[i]);
      }
      char a = Char.toLowerCase(c);
      for (char b: lowerCaseMap) if (a == b) return true;
      return false;
    }
    @Override public String toString() { return label; }
    public char[] toMap(){
      return map;
    }
  }

  public static class List extends StandardList<Character> {
    private static final long serialVersionUID = -6565154605439853891L;
    public List(String label, Character... chars){
      super(label, chars);
    }
  }

}
