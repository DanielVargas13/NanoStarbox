package box.star.text.basic;

public interface ScanControl {
  boolean collect(Scanner scanner, char character);
  interface WithExpansionPort extends ScanControl {
    boolean expand(Scanner scanner);
  }
}
