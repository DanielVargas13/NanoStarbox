package box.star.text;

/**
 * The TextScanner.Exception is thrown by the TextScanner interface classes when things are amiss.
 *
 * @author Hypersoft-Systems: USA
 * @version 2015-12-09
 */
public class Exception extends RuntimeException {
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
