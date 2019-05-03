package box.star.text;

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
  public char[] merge(char[] list){
    return TextScanner.mergeCharLists(list, compile());
  }
  public char[] filter(char[] list){
    return TextScanner.filterCharList(list, compile());
  }
}
