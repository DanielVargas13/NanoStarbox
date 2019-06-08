package box.star.shell.script;

import box.star.lang.SyntaxError;
import box.star.shell.script.content.DoubleQuotedText;
import box.star.text.Char;
import box.star.text.basic.Parser;
import box.star.text.basic.Scanner;

import java.util.regex.Pattern;

import static box.star.shell.script.Command.COMMAND_TERMINATOR_MAP;
import static box.star.text.Char.*;

import static box.star.shell.script.Parameter.QuoteType.*;

public class Parameter extends Interpreter {

  public static final char[] PARAMETER_TERMINATOR_MAP =
      new Char.Assembler(Char.toMap(PIPE, '<', '>'))
          .merge(COMMAND_TERMINATOR_MAP).merge(MAP_ASCII_ALL_WHITE_SPACE).toMap();

  public static final char[] LITERAL_PARAMETER_TERMINATOR_MAP =
      new Char.Assembler(PARAMETER_TERMINATOR_MAP)
          .merge(SINGLE_QUOTE, DOUBLE_QUOTE, BACKSLASH).toMap();

  public static enum QuoteType {
    NOT_QUOTING, SINGLE_QUOTING, DOUBLE_QUOTING, COMPOUND_QUOTING
  }

  public QuoteType quoteType = NOT_QUOTING;
  protected StringBuilder buffer;
  public String text;
  public Parameter(Scanner scanner) { super(scanner); }
  public String getText() {
    return text;
  }
  public String getPlainText(){
    if (quoteType.equals(SINGLE_QUOTING) || quoteType.equals(DOUBLE_QUOTING))
      return text.substring(0, text.length() - 1).substring(1);
    else if (quoteType.equals(NOT_QUOTING)) return text;
    throw new RuntimeException("cannot convert quote type: `"+quoteType+"' to plain text");
  }
  @Override public String toString() { return text; }
  @Override protected void start() {
    scanner.nextLineSpace();
    if (scanner.endOfSource()) {
      cancel();
      return;
    }
    char c = scanner.next();
    if (mapContains(c, MAP_ASCII_NUMBERS)) {
      if (mapContains(scanner.next(), Redirect.ARROWS)) {
        cancel(); return;
      } else scanner.back();
    }
    if (scanner.endOfSource() || mapContains(c, PARAMETER_TERMINATOR_MAP)) {
      cancel();
    } else {
      buffer = new StringBuilder();
      switch (c) {
        case SINGLE_QUOTE: { parseSingleQuotedText(); break; }
        case DOUBLE_QUOTE: { parseDoubleQuotedText(); break; }
        default: parseLiteralText();
      }
      text = buffer.toString();
      buffer = null;
      finish();
    }
  }

  private void parseContinuation(){
    if (scanner.current() == BACKSLASH) {
      quoteType = QuoteType.COMPOUND_QUOTING;
      do {
        buffer.append(scanner.current())
            .append(scanner.next())
            .append(scanner.nextField(LITERAL_PARAMETER_TERMINATOR_MAP));
      } while (scanner.current() == BACKSLASH);
    }
    if (scanner.endOfSource()) return;
    long start = scanner.getIndex();
    char c = scanner.next();
    if (scanner.endOfSource()) return;
    if (mapContains(c, PARAMETER_TERMINATOR_MAP)) {
      scanner.back();
      return;
    }
    else {
      quoteType = QuoteType.COMPOUND_QUOTING;
      switch (c) {
        case SINGLE_QUOTE: { parseSingleQuotedText(); break; }
        case DOUBLE_QUOTE: { parseDoubleQuotedText(); break; }
        default: parseLiteralText();
      }
    }
    if (scanner.getIndex() == start)
      throw new IllegalStateException(
          "endless loop condition aborted"+Parser.PARSER_CODE_QUALITY_BUG);
    parseContinuation();
  }

  // eat line continuation-sequence: backslash, optional carriage return, line-feed and all following line space
  private void eatLineContinuation(){
    long start = scanner.getIndex();
    if (scanner.current() == BACKSLASH) {
      scanner.next();
      if (scanner.current() == '\r') scanner.next();
      if (scanner.current() == '\n') { scanner.next();
        scanner.nextLineSpace();
        return;
      }
    }
    scanner.walkBack(start);
  }

  private void parseLiteralText() {
    // todo check for illegal characters
    if (NOT_QUOTING.equals(quoteType)) quoteType = QuoteType.NOT_QUOTING;
    eatLineContinuation();
    buffer.append(scanner.current())
        .append(scanner.nextField(LITERAL_PARAMETER_TERMINATOR_MAP));
    char c = scanner.current();
    if (c != BACKSLASH && mapContains(c, LITERAL_PARAMETER_TERMINATOR_MAP)) scanner.escape();
    parseContinuation();
  }

  private void parseSingleQuotedText() {
    if (scanner.current() != SINGLE_QUOTE)
      throw new SyntaxError(this, "expected literal quotation mark");
    if (NOT_QUOTING.equals(quoteType)) quoteType = QuoteType.SINGLE_QUOTING;
    buffer.append(SINGLE_QUOTE)
        .append(scanner.nextField(SINGLE_QUOTE))
        .append(SINGLE_QUOTE);
    parseContinuation();
  }

  private void parseDoubleQuotedText(){
    if (scanner.current() != DOUBLE_QUOTE)
      throw new SyntaxError(this, "expected double quotation mark");
    if (NOT_QUOTING.equals(quoteType)) quoteType = QuoteType.DOUBLE_QUOTING;
    buffer.append(DOUBLE_QUOTE)
        .append(scanner.run(doubleQuotedTextDriver))
        .append(DOUBLE_QUOTE);
    parseContinuation();
  }

  private final DoubleQuotedText doubleQuotedTextDriver = new DoubleQuotedText();

}
