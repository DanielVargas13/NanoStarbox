package box.star.text.optimization;

import box.star.io.Streams;
import box.star.text.Char;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileOutputStream;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * This "test" is used to precompile character maps, to reduce static
 * loading time of character assemblies.
 */
public class MapResourceGenerator {

  public final static char NULL_CHARACTER = 0;
  public final static char SPACE = ' ';
  public final static char
      LINE_FEED = 10,
      CARRIAGE_RETURN = 13,
      DELETE = 127;

  public final static char[] MAP_ASCII = new Char.RangeMap(NULL_CHARACTER, 255).toMap();
  public final static char[] MAP_ASCII_EXTENDED = new Char.RangeMap(128, 255).toMap();
  public final static char[] MAP_ASCII_ALL_WHITE_SPACE = new Char.Assembler(new Char.RangeMap(9, 13)).merge(SPACE).toMap();
  public final static char[] MAP_ASCII_LINE_WHITE_SPACE = new Char.Assembler(MAP_ASCII_ALL_WHITE_SPACE).filter(LINE_FEED, CARRIAGE_RETURN).toMap();
  public final static char[] MAP_ASCII_LETTERS = new Char.Assembler(new Char.RangeMap(65, 90)).merge(new Char.RangeMap(97, 122)).toMap();
  public final static char[] MAP_ASCII_NUMBERS = new Char.RangeMap(48, 57).toMap();
  public final static char[] MAP_ASCII_CONTROL = new Char.Assembler(new Char.RangeMap(NULL_CHARACTER, 31)).merge(DELETE).filter(MAP_ASCII_ALL_WHITE_SPACE).toMap();
  public final static char[] MAP_ASCII_SYMBOLS = new Char.Assembler(new Char.RangeMap(33, 47))
      .merge(new Char.RangeMap(58, 64))
      .merge(new Char.RangeMap(91, 96))
      .merge(new Char.RangeMap(123, 127)
      ).toMap();
  public final static char[] MAP_ASCII_HEX = new Char.Assembler(MAP_ASCII_NUMBERS).merge(new Char.RangeMap('a', 'f')).merge(new Char.RangeMap('A', 'F')).toMap();
  public final static char[] MAP_ASCII_OCTAL = new Char.Assembler(new Char.RangeMap('0', '8')).toMap();
  File resDir = new File("src/java/resource/main/box/star/text/optimization");

  void generate_static_map(String dest, char[] map) throws Exception {
    FileOutputStream fosMapDest = new FileOutputStream(new File(resDir, dest));
    Streams.writeSerializable(fosMapDest, map);
    fosMapDest.close();
  }

  @Test void generate_files() throws Exception {
    assertTrue(resDir.exists());
    generate_static_map("MAP_ASCII", MAP_ASCII);
    generate_static_map("MAP_ASCII_EXTENDED", MAP_ASCII_EXTENDED);
    generate_static_map("MAP_ASCII_ALL_WHITE_SPACE", MAP_ASCII_ALL_WHITE_SPACE);
    generate_static_map("MAP_ASCII_LINE_WHITE_SPACE", MAP_ASCII_LINE_WHITE_SPACE);
    generate_static_map("MAP_ASCII_LETTERS", MAP_ASCII_LETTERS);
    generate_static_map("MAP_ASCII_NUMBERS", MAP_ASCII_NUMBERS);
    generate_static_map("MAP_ASCII_CONTROL", MAP_ASCII_CONTROL);
    generate_static_map("MAP_ASCII_SYMBOLS", MAP_ASCII_SYMBOLS);
    generate_static_map("MAP_ASCII_HEX", MAP_ASCII_HEX);
    generate_static_map("MAP_ASCII_OCTAL", MAP_ASCII_OCTAL);
  }

}
