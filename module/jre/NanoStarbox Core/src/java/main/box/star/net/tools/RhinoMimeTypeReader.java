package box.star.net.tools;

import box.star.content.MagicMimeTypeReader;
import box.star.text.basic.Scanner;

import java.io.BufferedInputStream;
import java.io.BufferedReader;

import static box.star.net.tools.RhinoMacroDriver.RHINO_MACRO_DRIVER_MIME_TYPE;
import static box.star.text.Char.LINE_FEED;

public class RhinoMimeTypeReader implements MagicMimeTypeReader {
  @Override
  public String getMimeType(BufferedInputStream source) {
    String line;
    try {
      source.mark(128);
      line = new Scanner("mime-type-magic-reader", source).nextField('>');
      source.reset();
      if (
          line.equals("<mime type=\"javascript/x-nano-starbox-rhino-macro-document\"") ||
          line.equals("<mime type='javascript/x-nano-starbox-rhino-macro-document'")
      ) {
        source.skip(line.length()+1);
        return RHINO_MACRO_DRIVER_MIME_TYPE;
      }
      return null;
    }
    catch (Exception e) { throw new RuntimeException(e); }
  }
}
