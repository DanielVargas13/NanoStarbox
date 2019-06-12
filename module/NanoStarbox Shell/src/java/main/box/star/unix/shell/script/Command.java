package box.star.unix.shell.script;

public class Command implements ScriptElement {
  public static class Group extends Command {}
  public static class Shell extends Command {}
}
