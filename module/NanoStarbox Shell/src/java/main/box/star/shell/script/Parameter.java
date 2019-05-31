package box.star.shell.script;

import box.star.lang.SyntaxError;
import box.star.text.Char;
import box.star.text.basic.Scanner;

import static box.star.shell.runtime.parts.TextCommand.COMMAND_TERMINATOR_MAP;
import static box.star.text.Char.*;

public class Parameter extends Interpreter {

  public static final char[] PARAMETER_TERMINATOR_MAP =
      new Char.Assembler(Char.toMap(PIPE, '<', '>'))
          .merge(COMMAND_TERMINATOR_MAP).merge(MAP_ASCII_ALL_WHITE_SPACE).toMap();

  public static enum QuoteType { NOT_QUOTING, SINGLE_QUOTING, DOUBLE_QUOTING }
  protected QuoteType quoteType;
  protected String text;
  public Parameter(Scanner scanner) { super(scanner); }
  private final String extractText(){
    return text.substring(1, text.length()-1);
  }
  public String getText() {
    switch (quoteType) {
      case NOT_QUOTING: return text;
      default: return extractText();
    }
  }
  @Override public String toString() { return text; }
  @Override protected void start() {
    scanner.nextLineSpace();
    char c = scanner.next();
    switch (c) {
      case SINGLE_QUOTE: { parseSingleQuotedText(); break; }
      case DOUBLE_QUOTE: { parseDoubleQuotedText(); break; }
      default: parseLiteralText();
    }
    finish();
  }
  private void parseSingleQuotedText() {
    char delim = scanner.current();
    if (delim != SINGLE_QUOTE)
      throw new SyntaxError(this, "expected literal quotation mark");
    quoteType = QuoteType.SINGLE_QUOTING;
    text = delim+scanner.nextField(SINGLE_QUOTE);
    text += scanner.next(delim);
  }
  private void parseLiteralText() {
    // todo check for illegal characters
    quoteType = QuoteType.NOT_QUOTING;
    text = scanner.current()
        +scanner.nextField(PARAMETER_TERMINATOR_MAP) + SINGLE_QUOTE;
  }
  private void parseDoubleQuotedText(){
    if (scanner.current() != DOUBLE_QUOTE)
      throw new SyntaxError(this, "expected double quotation mark");
    quoteType = QuoteType.DOUBLE_QUOTING;
    text = scanner.nextField(PARAMETER_TERMINATOR_MAP);
  }
}
