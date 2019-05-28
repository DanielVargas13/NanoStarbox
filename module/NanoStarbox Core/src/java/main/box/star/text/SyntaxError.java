package box.star.text;

/**
 * The TextScanner.Exception is thrown by the TextScanner interface classes when things are amiss.
 */
@Deprecated public class SyntaxError extends RuntimeException {
  /**
   * Serialization ID
   */
  private static final long serialVersionUID = 0;

  /**
   * Constructs a TextScanner.Exception with an explanatory message.
   *
   * @param message Detail about the reason for the exception.
   */
  public SyntaxError(final String message) {
    super(message);
  }

  /**
   * Constructs a TextScanner.Exception with an explanatory message and cause.
   *
   * @param message Detail about the reason for the exception.
   * @param cause   The cause.
   */
  public SyntaxError(final String message, final Throwable cause) {
    super(message, cause);
  }

}
