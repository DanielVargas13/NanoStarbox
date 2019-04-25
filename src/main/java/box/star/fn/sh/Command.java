package box.star.fn.sh;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

public class Command implements Closeable {

  String[] parameters;
  Shell shell;
  SharedMap<String, String>locals = new SharedMap<>();
  Streams streams;
  Executive executive;

  Stack<Command> pipeChain = new Stack<>();

  public Command pipe(Command cmd) {
    pipeChain.peek().set(1, cmd);
    pipeChain.push(cmd);
    return this;
  }

  public Command set(int stream, Closeable value) {
    streams.set(stream, value);
    return this;
  }

  public Stack<Command> getPipeChain() {
    return (Stack<Command>) pipeChain.clone();
  }

  public Command(Shell shell, String... parameters){
    this.shell = shell;
    this.streams = shell.streams.copy();
    this.parameters = parameters;
    this.pipeChain.push(this);
  }

  private Command(Command command, String... parameters) {
    this.shell = command.shell;
    this.locals.merge(command.locals);
    List<String> p = new ArrayList<>();
    p.addAll(Arrays.asList(command.parameters));
    p.addAll(Arrays.asList(parameters));
    String[] n = new String[p.size()];
    this.parameters = p.toArray(n);
    this.streams = shell.streams.copy();
    this.pipeChain.push(this);
  }

  @Override
  public void close() throws IOException {
    if (executive.isAlive()) {
      //pipe.close(0);
      //pipe.close(1);
      //pipe.close(2);
    }
  }

  public Command build(String... parameters) {
    return new Command(this, parameters);
  }

  public int getExitValue() {
    try {
      return pipeChain.peek().executive.waitFor();
    }
    catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  public int run(){
    exec();
    return shell.status = getExitValue();
  }

  public Command exec(){
    shell.exec(this);
    return this;
  }

}
