package box.star.text.basic;

public interface ScannerDriver {
  interface WithSimpleControlPort extends ScannerDriver {
    boolean collect(Scanner scanner, char character);
  }
  interface WithExpansionControlPort extends ScannerDriver {
    boolean expand(Scanner scanner);
  }
  interface WithBufferControlPort extends ScannerDriver {
    boolean collect(Scanner scanner, StringBuilder buffer, char character);
  }
  interface WithMasterControlPorts extends WithExpansionControlPort, WithBufferControlPort {}
}
