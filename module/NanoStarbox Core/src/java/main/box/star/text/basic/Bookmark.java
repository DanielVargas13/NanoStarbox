package box.star.text.basic;

import box.star.text.Char;

import java.util.ArrayList;
import java.util.List;

import static box.star.text.Char.BACKSLASH;
import static box.star.text.Char.DOUBLE_QUOTE;

/**
 * An optimally compiled source tag, with notebook function
 */
public class Bookmark {
  public final long line, column, index;
  public final String origin, quote;
  Enum subType;
  public final List<Object> notes = new ArrayList<>();
  Bookmark(Scanner source){
   this.line = source.state.line;
   this.column = source.state.column;
   this.index = source.state.index;
   this.quote = quoteSource(source.getPath());
   this.origin = compileToString();
  }
  public Bookmark setSubType(Enum type){
    if (hasSubType())
      throw new IllegalStateException("you can't do that,"+
          " the underlying property is marked read only for clients");
    this.subType = type;
    return this;
  }
  public Enum getSubType() {
    return subType;
  }
  public boolean hasSubType(){
    return subType != null;
  }
  private static String quoteSource(String source){
    return source
        .replaceAll(Char.toString(BACKSLASH, BACKSLASH), Char.toString(BACKSLASH, BACKSLASH))
        .replaceAll(Char.toString(BACKSLASH, BACKSLASH, DOUBLE_QUOTE), Char.toString(BACKSLASH, DOUBLE_QUOTE));
  }
  private String compileToString() {
    return " at location = " + "{line: " + line + ", column: " + column + ", index: " + index + ", source: \"" + quote + "\"};";
  }
  @Override
  public String toString() {
    return origin;
  }
}
