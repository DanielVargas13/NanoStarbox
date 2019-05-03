package box.star.text;

import java.util.ArrayList;
import java.util.List;

public class TextScannerRangeMap {

  public final int start, end;
  public TextScannerRangeMap(int start, int end){
    this.start = TextScanner.normalizeRangeValue(start); this.end = TextScanner.normalizeRangeValue(end);
  }
  public boolean match(char character) {
    return character < start || character > end;
  }
  public char[] compile(){
    return TextScanner.selectCharList(this);
  }

  public static class Assembler {
    List<Character> chars = new ArrayList<>();
    public Assembler(TextScannerRangeMap map){
      this(map.compile());
    }
    public Assembler(char... map){
      this.merge(map);
    }
    public Assembler(int start, int end){
      merge(new TextScannerRangeMap(start, end));
    }
    public Assembler(int... integer){
      for (int i:integer) chars.add((char)TextScanner.normalizeRangeValue(i));
    }
    public Assembler merge(int... integer){
      for (int i:integer) chars.add((char)TextScanner.normalizeRangeValue(i));
      return this;
    }
    public Assembler merge(int start, int end){
      return merge(new TextScannerRangeMap(start, end));
    }
    public Assembler merge(TextScannerRangeMap map){
      return merge(map.compile());
    }
    public Assembler merge(char... map){
      for (char c: map) chars.add(c);
      return this;
    }
    public Assembler filter(int... integer){
      for (int i:integer) chars.remove((char)TextScanner.normalizeRangeValue(i));
      return this;
    }
    public Assembler filter(int start, int end){
      return filter(new TextScannerRangeMap(start, end));
    }
    public Assembler filter(TextScannerRangeMap map){
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
