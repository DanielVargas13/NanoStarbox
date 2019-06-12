package box.star.unix.shell.script;

import java.util.ArrayList;

public class FunctionDefinition implements ScriptElement {
  public String name;
  public Command.Shell body;
}
