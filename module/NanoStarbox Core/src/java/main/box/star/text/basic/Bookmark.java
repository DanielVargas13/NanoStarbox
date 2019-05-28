package box.star.text.basic;

import box.star.text.Char;

import static box.star.text.Char.BACKSLASH;
import static box.star.text.Char.DOUBLE_QUOTE;

/**
 * An optimally compiled source tag, with notebook function
 */
public class Bookmark {
  public final long line, column, index;
  public final String path;
  private String quotedOrigin;
  Bookmark(Scanner source){
   this.line = source.state.line;
   this.column = source.state.column;
   this.index = source.state.index;
   this.path = source.getPath();
  }
  final public String getQuotedOrigin(){
    if (quotedOrigin == null) this.quotedOrigin = quoteSource(path);
    return quotedOrigin;
  }
  private static String quoteSource(String source){
    return source
        .replaceAll(Char.toString(BACKSLASH, BACKSLASH), Char.toString(BACKSLASH, BACKSLASH))
        .replaceAll(Char.toString(BACKSLASH, BACKSLASH, DOUBLE_QUOTE), Char.toString(BACKSLASH, DOUBLE_QUOTE));
  }
  private String compileToString() {
    return " at location = " + "{line: " + line + ", column: " + column + ", index: " + index + ", source: \"" + getQuotedOrigin() + "\"}";
  }
  @Override
  public String toString() {
    return compileToString();
  }
}
