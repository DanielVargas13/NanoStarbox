package box.star.shell;

import box.star.contract.NotNull;
import box.star.text.Char;
import box.star.text.SyntaxError;
import box.star.text.basic.Scanner;
import box.star.text.basic.ScannerMethod;

import java.io.File;
import java.util.Hashtable;
import java.util.Map;
import java.util.Stack;

import static box.star.text.Char.*;

/**
 * <p>Shell Code Reference</p>
 * <pre>
 *   identifier=value ... COMMAND
 *   identifier+=value ... COMMAND
 *   identifier-=value ... COMMAND PARAMETERS IO
 * </pre>
 */
public class Host {

  private final static char
      ENTER_SHELL = '(', EXIT_SHELL = ')',
      ENTER_OBJECT = '{', EXIT_OBJECT = '}',
      MACRO_TRIGGER = '$';

  private static final char[] COMMAND_TERMINATOR =
      new Char.Assembler(Char.toMap('\0', '\n', '\r', '#', ';', '&', '(', ')', '{', '}')).toArray();

  private static final char[] BREAK_PARAMETER_MAP =
      new Char.Assembler(PIPE, '<', '>').map(COMMAND_TERMINATOR).map(MAP_ASCII_ALL_WHITE_SPACE).toArray();

  private static final char[] BREAK_MACRO_OR_PARAMETER_MAP =
      new Char.Assembler(MACRO_TRIGGER).map(BREAK_PARAMETER_MAP).toArray();

  Environment environment;
  StreamTable streams;
  Main macroRunner;
  Stack<String> parameters;

  public Host(){
    environment = new Environment();
    streams = new StreamTable();
    macroRunner = new Main(this);
  }

  public String start(File file, String... parameters) {
    this.parameters = new Stack<>();
    this.parameters.push(file.getPath());
    for (int i = 0; i < parameters.length; i++) this.parameters.push(parameters[i]);
    Scanner scanner = new Scanner(file);
    return scanner.run(macroRunner);
  }

  private String nextMacroBody(Scanner scanner, char closure) {
    String data = scanner.nextField(closure);
    scanner.nextCharacter(closure);
    return data;
  }

  private String getMacroText(Scanner scanner){
    char next = scanner.next();
    if (Char.mapContains(next, Char.MAP_ASCII_NUMBERS)){
      return next+scanner.nextMap(Char.MAP_ASCII_NUMBERS); }
    switch (next) {
      case '_': case '#': return Char.toString(next);
      case ENTER_OBJECT: return (nextMacroBody(scanner, EXIT_OBJECT)); }
    scanner.back();
    scanner.nextCharacter("macro", '\0', true);
    return null;
  }

  private String doMacro(Scanner scanner) {
    char next = scanner.next();
    if (Char.mapContains(next, Char.MAP_ASCII_NUMBERS)){
      int index = Integer.parseInt(next+scanner.nextMap(Char.MAP_ASCII_NUMBERS));
      return parameters.get(index);
    }
    switch (next) {
      case '#': { return parameters.size()+""; }
      case '_': { return parameters.peek(); }
      case ENTER_OBJECT: {
        return environment.get(nextMacroBody(scanner, EXIT_OBJECT)).toString(); }
      default: scanner.back();scanner.nextCharacter(ENTER_OBJECT); }
    return Char.toString(MACRO_TRIGGER);
  }

  private String doCommand(Scanner scanner, Stack<String> parameters) {
    StringBuilder out = new StringBuilder();
    for(String p: parameters) out.append(p).append(" ");
    return out.substring(0, Math.max(0, out.length() - 1));
  }

  public static class Main extends ScannerMethod {

    static class TextCommandEntry {
      String source;
      Stack<String[]> environmentOperations;
      Stack<String> parameters;
      Map<Integer, String> redirects = new Hashtable<>();
      char terminator; // whatever terminated this command
      TextCommandEntry next; // if terminator == pipe
      TextCommandEntry(String source){this.source = source;}
    }

    Host context;

    public Main(Host context) { this.context = context; }

    TextCommandEntry processCommandLine(Scanner scanner) {
      scanner.scanAllWhiteSpace();
      scanner.next();
      TextCommandEntry textCommand = new TextCommandEntry(scanner.toString().substring(1));
      scanner.back();
      textCommand.environmentOperations = processEnvironmentOperations(scanner);
      textCommand.parameters = processParameters(scanner);
      return processRedirects(scanner, textCommand);
    }

    Stack<String[]> processEnvironmentOperations(Scanner scanner) {
      Stack<String[]> operations = new Stack<>();
      do {
        long start = scanner.getIndex();
        scanner.scanAllWhiteSpace();
        String[] op = processEnvironmentOperation(scanner);
        if (op == null){ scanner.walkBack(start); break; }
        operations.push(op); } while (true);
      return operations;
    }

    String processEnvironmentLabel(Scanner scanner){
      StringBuilder output = new StringBuilder();
      char[] okay1 = new Char.Assembler(Char.MAP_ASCII_LETTERS).map('-','_').toArray();
      do { char c = scanner.next();
        if (c == 0) return null;
        else if (c == '=') break;
        else if (!Char.mapContains(c, okay1)) return null;
        else output.append(c); } while (true);
      scanner.back();
      return output.toString();
    }

    String[] processEnvironmentOperation(Scanner scanner) {
      String[] operation = new String[3];
      operation[0] = processEnvironmentLabel(scanner);
      if (operation[0] == null) return null;
      try { operation[1] = Char.toString(scanner.nextCharacter('='));
      } catch (Exception e){ return null; }
      operation[2] = scanner.nextBoundField(new Char.Assembler(MAP_ASCII_ALL_WHITE_SPACE).map(';', '&', '#').toArray());
      return operation;
    }

    String processLiteralText(Scanner scanner) {
      return scanner.nextField(BREAK_PARAMETER_MAP);
    }
    String processQuotedLiteralText(Scanner scanner) {
      return scanner.nextField('\'');
    }
    String processQuotedMacroText(Scanner scanner) {
      return scanner.nextBoundField('"');
    }

    String processParameter(Scanner scanner) {
      scanner.scanLineWhiteSpace();
      StringBuilder builder = new StringBuilder();
      long start = scanner.getIndex();
      char c;
      do { c = scanner.next();
        if (Char.mapContains(c, MAP_ASCII_ALL_WHITE_SPACE)) break;
        switch (c){
          case '<': case '>': {
            boolean notAnumber = false;
            try { int v = Integer.parseInt(builder.toString());
            } catch (NumberFormatException nfe){ notAnumber = true; }
            if (notAnumber == false){ scanner.walkBack(start); return null; }
            scanner.back();
            return null; }
          case '\'': { builder.append(c).append(processQuotedLiteralText(scanner));
            scanner.nextCharacter(c);
            builder.append(c);
            break; }
          case '"': { builder.append(c).append(processQuotedMacroText(scanner));
            scanner.nextCharacter(c);
            builder.append(c);
            break; }
          default: { if (Char.mapContains(c, BREAK_PARAMETER_MAP)){
              scanner.back();
              if (builder.length() == 0) return null;
              return builder.toString(); }
            builder.append(c).append(processLiteralText(scanner)); }
        }
      } while (!Char.mapContains(c, BREAK_PARAMETER_MAP));
      return builder.toString();
    }

    Stack<String> processParameters(Scanner scanner) {
      Stack<String> parameters = new Stack<>();
      do { long start = scanner.getIndex();
        String p = processParameter(scanner);
        if (p == null){
          scanner.walkBack(start);
          if (parameters.isEmpty()) { return null; }
          break; }
        parameters.push(p); } while (true);
      return parameters;
    }

    TextCommandEntry processRedirects(Scanner scanner, TextCommandEntry commandEntry) {
      char c = scanner.next();
      commandEntry.redirects = new Hashtable<>();
      while (Char.mapContains(c, '<', '>', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9')) {
        switch (c) {
          case '<': { scanner.scanAllWhiteSpace();
            commandEntry.redirects.put(0, c+scanner.nextBoundField(MAP_ASCII_ALL_WHITE_SPACE));
            break; }
          case '>': { scanner.scanAllWhiteSpace();
            commandEntry.redirects.put(1, c+scanner.nextBoundField(MAP_ASCII_ALL_WHITE_SPACE));
            break; }
          default: { String scan = c + scanner.nextMap(MAP_ASCII_NUMBERS);
            int v = Integer.parseInt(scan);
            c = scanner.next();
            if (!Char.mapContains(c, '<','>')) {
              scanner.back();
              scanner.nextCharacter("< or >", '\0', true); }
            scanner.scanAllWhiteSpace();
            commandEntry.redirects.put(v, c+scanner.nextBoundField(MAP_ASCII_ALL_WHITE_SPACE)); }
        }
        c = scanner.next();
      }
      if (c == PIPE){
        commandEntry.terminator = c;
        commandEntry.next = processCommandLine(scanner);
        return commandEntry; }
      commandEntry.terminator = processCommandEnding(scanner);
      return commandEntry;
    }

    char processCommandEnding(Scanner scanner) {
      char c;
      switch (c = scanner.next()){
        case ';': case '#': case '\0': case '\n': return c;
        case '\r': return processCommandEnding(scanner);
      }
      scanner.back();
      scanner.nextString("semi-colon, hash-mark, carriage-return, line-feed or end of source", true);
      return 0;
    }

    @Override
    protected boolean terminate(@NotNull Scanner scanner, char character) {
      if (Char.mapContains(character, Char.MAP_ASCII_ALL_WHITE_SPACE)) {return false;}
      switch (character) {
        case 0: { if (scanner.escapeMode()) throw new SyntaxError("escaped end of stream");
          return true; }
        case '#': { swap(character + scanner.nextField('\n'));
          return false; }
      }
      backStep(scanner);
      TextCommandEntry tce = processCommandLine(scanner);
      //swap(processCommandLine(scanner));
      return false;
    }

//    public static class CommandScanner extends ScannerMethod {
//      Host context;
//      ParameterScanner parameterScanner = new ParameterScanner();
//      @Override
//      protected void start(@NotNull Scanner scanner, Object[] parameters) {
//        context = (Host) parameters[0];
//      }
//      @Override
//      protected boolean terminate(@NotNull Scanner scanner, char character) {
//        Stack<String> parameters = new Stack<>();
//        scanner.branch(parameterScanner, character, context, parameters);
//        char c = scanner.next();
//        do {
//          switch (c){
//            case '&':{
//              parameters.add("\\&");
//              break;
//            }
//            case ';':{
//              parameters.add("\\;");
//              break;
//            }
//            default:{
//              scanner.back();
//              break;
//            }
//          }
//          break;
//        } while((c = scanner.next()) != 0);
//        swap(context.doCommand(scanner, parameters));
//        return true;
//      }
//    }
//
//    public static class ParameterScanner extends ScannerMethod {
//      Host context;
//      Stack<String> parameters;
//      // if this is false, no text will be expanded by this scanner
//      boolean doEvaluation = true;
//      @Override
//      protected void start(@NotNull Scanner scanner, Object[] parameters) {
//        context = (Host) parameters[0];
//        this.parameters = (Stack<String>) parameters[1];
//        scanner.nextMap(Char.MAP_ASCII_ALL_WHITE_SPACE);
//        if (parameters.length > 2) this.doEvaluation = (boolean) parameters[2];
//      }
//      @Override
//      protected boolean scan(@NotNull Scanner scanner) {
//        scanner.nextMap(Char.MAP_ASCII_ALL_WHITE_SPACE);
//        return true;
//      }
//      @Override
//      protected boolean terminate(@NotNull Scanner scanner, char character) {
//        if (Char.mapContains(character, '(', '{')) {
//          scanner.back();
//          scanner.nextString("parameter", true);
//          return true; // ^ throws
//        }
//        if (character == MACRO_TRIGGER){
//          String origin = scanner.toString();
//          String source = getParameter(scanner, character);
//          try {
//            parameters.addAll(split("parameter["+(parameters.size()-1)+"]", source));
//          } catch (SyntaxError se){
//            throw new SyntaxError("parameter expansion failure"+origin+"\n\nparameter-expansion-text:\n\n"+source+"\n\nCause: "+se.getMessage());
//          }
//          return false;
//        }
//        if (Char.mapContains(character, BREAK_PARAMETER_MAP)) {
//          scanner.back();
//          return true;
//        }
//        String p = getParameter(scanner, character);
//        parameters.push(p);
//        return false;
//      }
//      protected Stack<String> split(String name, String source) {
//        Scanner scanner = new Scanner(name, source + EXIT_SHELL);
//        Stack<String> parameters = new Stack<>();
//        scanner.run(this, context, parameters, false);
//        return parameters;
//      }
//
//      /**
//       * A hack on nextBoundField that allows us to seek-beyond quotes within macro functions.
//       *
//       * @param scanner
//       * @return
//       * @throws SyntaxError
//       */
//      private String extractQuote(Scanner scanner) throws SyntaxError {
//
//        StringBuilder sb = new StringBuilder();
//
//        while (true) {
//
//          char c = scanner.next();
//
//          if (c == BACKSLASH && !scanner.escapeMode() && doEvaluation) continue;
//
//          if (c == 0) {
//            if (scanner.escapeMode()) {
//              throw scanner.syntaxError("expected character escape sequence, found end of stream");
//            }
//            return sb.toString();
//          }
//
//          if (scanner.escapeMode()) {
//            String swap = (doEvaluation)?scanner.expand(c):Char.toString(c);
//            sb.append(swap);
//            continue;
//          }
//
//          if (c == context.MACRO_TRIGGER) {
//            if (doEvaluation) sb.append(context.doMacro(scanner));
//            else sb.append(context.getMacroText(scanner));
//            continue;
//          }
//
//          if (c == Char.DOUBLE_QUOTE) {
//            scanner.back();
//            break;
//          }
//
//          sb.append(c);
//
//        }
//        return sb.toString();
//      }
//
//      @Override
//      protected @NotNull String compile(@NotNull Scanner scanner) { return Tools.EMPTY_STRING; }
//      private String getParameter(Scanner scanner, char character) {
//        char c;
//        StringBuilder data = new StringBuilder();
//        if (character == MACRO_TRIGGER) {
//          if (doEvaluation) data.append(context.doMacro(scanner));
//          else data.append(context.getMacroText(scanner));
//        } else if (character == Char.DOUBLE_QUOTE) {
//          data.append(this.extractQuote(scanner));
//          scanner.nextCharacter(character);
//        } else if (character == Char.SINGLE_QUOTE) {
//          data.append(scanner.nextField(character));
//          scanner.nextCharacter(character);
//        } else {
//          if (doEvaluation) data.append(character).append(scanner.nextBoundField(BREAK_MACRO_OR_PARAMETER_MAP));
//          else data.append(character).append(scanner.nextField(BREAK_MACRO_OR_PARAMETER_MAP));
//        }
//        while (true) {
//          c = scanner.next();
//          if (!Char.mapContains(c, BREAK_PARAMETER_MAP)) {
//            data.append(getParameter(scanner, c));
//          } else {
//            scanner.back();
//            break;
//          }
//        }
//        return data.toString();
//      }
//
//    }
//
//  }
  }
}
