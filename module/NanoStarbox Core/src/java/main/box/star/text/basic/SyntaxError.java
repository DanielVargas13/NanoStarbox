package box.star.text.basic;

import box.star.contract.NotNull;

public class SyntaxError extends RuntimeException {

  protected Object host;

  private @NotNull String tag(){
    return host.getClass().getName()+".SyntaxError: ";
  }
  @Override
  public @NotNull String toString() {
    return tag() + super.getMessage();
  }

  SyntaxError(@NotNull String sourceTag, @NotNull String message) {
    super("\n\n"+message+":\n\n   "+sourceTag+"\n");
  }

  SyntaxError(@NotNull String sourceTag, @NotNull String message, @NotNull Throwable cause) {
    super("\n\n"+message+":\n\n   "+sourceTag+"\n", cause);
  }

  SyntaxError(Bookmark location, String message) {
    this(location.toString(), message);
  }
  SyntaxError(Bookmark location, String message, Throwable cause) {
    this(location.toString(), message, cause);
  }

  public SyntaxError(@NotNull CancellableTask action, @NotNull String message){
    this(action.cancel(), message);
    this.host = action;
  }

  public SyntaxError(@NotNull CancellableTask action, @NotNull String message, Throwable cause){
    this(action.cancel(), message, cause);
    this.host = action;
  }

  public SyntaxError(@NotNull Scanner source, @NotNull String message){
    this(source.createBookmark(), message);
    this.host = source;
  }

  public SyntaxError(@NotNull Scanner source, @NotNull String message, Throwable cause){
    this(source.createBookmark(), message, cause);
    this.host = source;
  }

}
