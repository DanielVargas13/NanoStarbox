package box.star.text;

import java.io.Serializable;
import java.util.regex.Pattern;

/**
 * Serializable Pattern Matcher with Label
 */
public class TextPattern implements Serializable {

  private static final long serialVersionUID = 6063966247343433103L;

  protected final String label;
  protected final Pattern pattern;

  /**
   * Creates a serializable TextPattern for optimized searching and load time.
   *
   * @param label A name for this TextPattern.
   * @param pattern The Java regex string.
   * @param flags The Java Pattern Flags.
   *              <br><br><p>Usage:</p>
   *              <code>import java.util.regex.Pattern;</code>
   *              <br><br><p>Java Pattern Flags:</p>
   *         {@link java.util.regex.Pattern#CASE_INSENSITIVE}, {@link java.util.regex.Pattern#MULTILINE}, {@link java.util.regex.Pattern#DOTALL},
   *         {@link java.util.regex.Pattern#UNICODE_CASE}, {@link java.util.regex.Pattern#CANON_EQ}, {@link java.util.regex.Pattern#UNIX_LINES},
   *         {@link java.util.regex.Pattern#LITERAL}, {@link java.util.regex.Pattern#UNICODE_CHARACTER_CLASS}
   *         and {@link java.util.regex.Pattern#COMMENTS}
   */
  public TextPattern(String label, String pattern, int flags){
    this.label = label;
    this.pattern = Pattern.compile(pattern, flags);
  }

  /**
   * Gets the label for this TextPattern.
   *
   * @return
   */
  public String getLabel() {
    return label;
  }

  /**
   * Performs a full service text match.
   *
   * @param source
   * @return
   */
  public boolean match(CharSequence source){
    return pattern.matcher(source).matches();
  }

  /**
   * Provides a way to throw custom syntax errors for match failure.
   * @param input
   * @param length
   * @param textScanner
   * @return
   */
  public boolean continueScanning(StringBuilder input, int length, TextPatternControlPort textScanner){
    return true;
  }

  public static interface TextPatternControlPort {
    RuntimeException syntaxError(String message, Throwable causedBy);
    RuntimeException syntaxError(String message);
    boolean hasNext();
    boolean end();
    long index();
    long line();
    long column();
    String sourceLabel();
  }

}
