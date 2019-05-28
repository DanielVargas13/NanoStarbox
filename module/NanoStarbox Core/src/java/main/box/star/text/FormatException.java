package box.star.text;

public class FormatException extends IllegalStateException {
  public FormatException(String s) { super(s); }
  public FormatException(String message, Throwable cause) {
    super(message, cause);
  }
}
