package box.star.text;

import java.io.Serializable;

public final class Char {

  public final static char NULL_CHARACTER = 0;

  public final static char META_DOCUMENT_TAG_START = '<';
  public final static char META_DOCUMENT_TAG_END = '>';

  public final static char BACKSLASH = '\\';
  public final static char SINGLE_QUOTE = '\'';
  public final static char DOUBLE_QUOTE = '"';

  public final static int CHAR_MAX = '\uffff';
  public final static char[] MAP = new MapAssembler(0, CHAR_MAX).assemble();
  public final static char[] MAP_WHITE_SPACE = new MapAssembler(9, 13).merge(' ').assemble();
  public final static char[] MAP_LETTERS = new MapAssembler(65, 90).merge(97, 122).assemble();
  public final static char[] MAP_NUMBERS = new MapAssembler.RangeMap(48, 57).compile();
  public final static char[] MAP_CONTROL = new MapAssembler(0, 31).filter(MAP_WHITE_SPACE).assemble();
  public final static char[] MAP_EXTENDED = new MapAssembler.RangeMap(127, CHAR_MAX).compile();

  public final static char[] MAP_SYMBOLS = new MapAssembler(33, 47)
      .merge(58, 64)
      .merge(91, 96)
      .merge(123, 126)
      .assemble();

  public static int atLeastZero(int val) { return (val < 0) ? 0 : val; }

  public static int atMostCharMax(int val) { return (val > CHAR_MAX) ? '\uffff' : val; }

  public static int sanitizeRangeValue(int val) { return atLeastZero(atMostCharMax(val));}

  public static boolean charMapContains(char search, char[] range) {
    for (int i = 0; i < range.length; i++) if (range[i] == search) return true;
    return false;
  }

  static char[] buildRangeMap(MapAssembler.RangeMap range) {
    StringBuilder out = new StringBuilder();
    for (int i = range.start; i <= range.end; i++) out.append((char) i);
    return out.toString().toCharArray();
  }

  public static class MapAssembler implements Serializable {
    private static final long serialVersionUID = 8454376662352328447L;
    StringBuilder chars = new StringBuilder();

    public MapAssembler(RangeMap map) {
      this(map.compile());
    }

    public MapAssembler(char... map) {
      merge(map);
    }

    public MapAssembler(int start, int end) {
      merge(new RangeMap(start, end));
    }

    public MapAssembler(int... integer) {
      merge(integer);
    }

    public MapAssembler merge(int... integer) {
      char[] current = assemble();
      for (int i : integer) {
        char c = (char) sanitizeRangeValue(i);
        if (!charMapContains(c, current)) chars.append(c);
      }
      return this;
    }

    public MapAssembler merge(int start, int end) {
      return merge(new RangeMap(start, end));
    }

    public MapAssembler merge(RangeMap map) {
      return merge(map.compile());
    }

    public MapAssembler merge(char... map) {
      char[] current = assemble();
      for (char c : map) if (!charMapContains(c, current)) chars.append(c);
      return this;
    }

    public MapAssembler filter(int... integer) {
      StringBuilder map = new StringBuilder();
      for (int i : integer) map.append((char) i);
      char[] chars = map.toString().toCharArray();
      filter(chars);
      return this;
    }

    public MapAssembler filter(int start, int end) {
      return filter(new RangeMap(start, end));
    }

    public MapAssembler filter(RangeMap map) {
      return filter(map.compile());
    }

    public MapAssembler filter(char... map) {
      StringBuilder filter = new StringBuilder();
      for (char c : chars.toString().toCharArray()) {
        if (charMapContains(c, map)) continue;
        filter.append(c);
      }
      this.chars = filter;
      return this;
    }

    public char[] assemble() {
      return chars.toString().toCharArray();
    }

    @Override
    public String toString() {
      return chars.toString();
    }

    public boolean match(char character) {
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
