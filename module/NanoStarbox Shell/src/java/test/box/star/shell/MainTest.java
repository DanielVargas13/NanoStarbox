package box.star.shell;

import org.junit.jupiter.api.Test;

class MainTest {

  @Test void main(){
    Main shell = new Main("hi");
    System.out.println(shell.getShellBaseDirectory());
  }

}