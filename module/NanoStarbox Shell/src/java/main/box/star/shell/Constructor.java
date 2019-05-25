package box.star.shell;

/**
 * Any object can implement this to offer object constructor services.
 * @param <Kind>
 */
public interface Constructor<Kind> {
  /**
   * <p>Custom Object Constructor</p>
   *
   * @param context the context
   * @param parameters the parameters for the construction
   * @return the newly constructed object
   */
 Kind construct(Context context, Object... parameters);

}
