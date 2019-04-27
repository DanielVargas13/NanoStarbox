package box.star.bin.sh;

import box.star.bin.sh.promise.StreamCatalog;
import box.star.bin.sh.promise.VariableCatalog;

import java.io.*;
import java.util.*;

public class Command implements VariableCatalog<Command>, StreamCatalog<Command> {

  @Override
  public Command applyVariables(Map<String, String> variables) {
    locals.putAll(variables);
    return this;
  }

  @Override
  public Command clearVariables() {
    locals.clear();
    return this;
  }

  @Override
  public Command resetVariables() {
    locals.clear();
    return this;
  }

  @Override
  public String get(String key) {
    return locals.get(key);
  }

  @Override
  public Command set(String key, String value) {
    locals.put(key, value);
    return this;
  }

  @Override
  public Command remove(String key) {
    locals.remove(key);
    return this;
  }

  @Override
  public List<String> variables() {
    return new ArrayList<>(locals.keySet());
  }

  @Override
  public boolean haveVariable(String key) {
    return locals.containsKey(key);
  }

  @Override
  public SharedMap<String, String> exportVariables() {
    return locals;
  }

  String[] parameters;
  Shell shell;
  SharedMap<String, String> locals = new SharedMap<>();
  Streams streams;

  Stack<Command> pipeChain = new Stack<>();

  public Command(Shell shell, String... parameters) {
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

  public Command pipe(Command cmd) {
    pipeChain.push(cmd);
    return this;
  }

  @Override
  public Command readInputFrom(InputStream is) {
    streams.set(0, is);
    return this;
  }

  @Override
  public Command remove(Integer key) {
    streams.remove(key);
    return this;
  }

  @Override
  public Command resetStreams() {
    streams = new Streams(shell.exportStreams());
    return this;
  }

  @Override
  public Command applyStreams(Streams overlay) {
    streams.layer(overlay);
    return this;
  }

  @Override
  public Command set(Integer key, Closeable stream) {
    streams.set(key, stream);
    return this;
  }

  @Override
  public <ANY> ANY get(Integer key) {
    return streams.get(key);
  }

  @Override
  public List<Integer> streams() {
    return new ArrayList<>(streams.keyList());
  }

  @Override
  public boolean haveStream(Integer key) {
    return streams.hasStream(key);
  }

  @Override
  public SharedMap<Integer, Closeable> exportStreams() {
    return streams.export();
  }

  @Override
  public Command writeOutputTo(OutputStream os) {
    streams.set(1, os);
    return this;
  }

  public ByteArrayOutputStream writeCapture() {
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    writeOutputTo(os);
    return os;
  }

  @Override
  public Command writeErrorTo(OutputStream os) {
    streams.set(2, os);
    return this;
  }

  public ByteArrayOutputStream errorCapture() {
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    writeErrorTo(os);
    return os;
  }

  public List<String[]> getPipeChain() {
    List<String[]>out = new ArrayList<>();
    for (Command c: pipeChain){
      out.add(c.parameters);
    }
    return out;
  }

  public Command build(String... parameters) {
    return new Command(this, parameters);
  }

  public int run() {
    return shell.status = exec().exitValue();
  }

  public Executive exec() {
    if (pipeChain.size() > 1) return shell.execPipe(locals, streams, getPipeChain());
    return shell.exec(locals, streams, pipeChain.get(0).parameters);
  }

  public Executive execPipe() {
    Streams common_streams = new Streams(streams.get(0), null, streams.get(2));
    Stack<Command>chain = (Stack<Command>) pipeChain.clone();
    Stack<Executive>executives = new Stack<>();
    Command link = chain.remove(0);
    executives.add(shell.exec(link.locals, common_streams, link.parameters));
    common_streams.set(0, null);
    for (Command command: chain){
      Executive next = shell.exec(command.locals, common_streams, command.parameters);
      next.readInputFrom(executives.peek().get(0));
      executives.add(next);
    }
    return executives.peek().writeOutputTo(streams.get(1));
  }

}
