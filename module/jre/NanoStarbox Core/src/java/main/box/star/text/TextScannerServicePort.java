package box.star.text;

public interface TextScannerServicePort {
  RuntimeException syntaxError(String message, Throwable causedBy);
  RuntimeException syntaxError(String message);
  boolean hasNext();
  boolean end();
  long index();
  long line();
  long column();
  String sourceLabel();
}
