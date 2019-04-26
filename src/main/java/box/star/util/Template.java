package box.star.util;

import box.star.io.Streams;

import java.io.File;
import java.io.FileInputStream;
import java.io.Serializable;
import java.util.Date;
import java.util.Map;
import java.util.Stack;

public class Template implements Serializable {

  private static final long serialVersionUID = -2777838851672624261L;
  private static final char LINE_ENDING = "\n".charAt(0);
  private final static int START_MARKER = 1;
  private final static int STOP_MARKER = 2;
  private static String[] marker = {null, "<*", "*>"};
  private static int LL = marker[STOP_MARKER].length(), LY = marker[STOP_MARKER].length();
  private File source;
  private String sourceData;
  private TagData[] tagData;
  private long creationTime;

  public Template(File source) {
    this.source = source;
    try {
      scanSourceTags();
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private void addSourceTag(String file, TagData tag, Stack<TagData> sourceTags) {
    TagData previousTag = (sourceTags.size() > 0) ? sourceTags.peek() : null;
    if (previousTag != null) {
      tag.record = new SourceData(file, getLineNumber(tag.start, previousTag.end, previousTag.record), getColumnNumber(tag.start));
    } else {
      tag.record = new SourceData(file, getLineNumber(tag.start), getColumnNumber(tag.start));
    }
    sourceTags.push(tag);
  }

  private void scanSourceTags() throws Exception {
    this.sourceData = Streams.readWholeString(new FileInputStream(source));
    Stack<TagData> sourceTags = new Stack<>();
    String file = source.getPath();
    TagData find = new TagData(sourceData, 0);
    while (find.haveStart()) {
      if (!find.haveEnd()) break;
      addSourceTag(file, find, sourceTags);
      find = new TagData(sourceData, find.end);
    }
    tagData = new TagData[sourceTags.size()];
    sourceTags.toArray(tagData);
    this.creationTime = new Date().getTime();
  }

  private int getLineNumber(int start) {
    int line = 1;
    for (int i = start; i > 0; i--) {
      if (sourceData.charAt(i) == LINE_ENDING) line++;
    }
    return line;
  }

  private int getLineNumber(int start, int stop, SourceData hint) {
    int line = hint.line;
    for (int i = start; i > stop; i--) {
      if (sourceData.charAt(i) == LINE_ENDING) line++;
    }
    return line;
  }

  private int getColumnNumber(int start) {
    int y = 0;
    for (int i = start; i > -1 && sourceData.charAt(i) != LINE_ENDING; i--, y++) ;
    return y;
  }

  public String map(Map<String, String> source) {
    Filler mapTemplate = new Filler() {
      @Override
      public String replace(String data, SourceData record) {
        return source.get(data);
      }
    };
    return fill(mapTemplate);
  }

  public String fill(Filler filler) {

    if (source.lastModified() > creationTime) {
      try {
        scanSourceTags();
      }
      catch (Exception e) {
        throw new RuntimeException(e);
      }
    }

    if (tagData.length == 0) return sourceData;

    int head = 0;
    StringBuilder sb = new StringBuilder();
    String file = source.getPath();
    SourceData record = null;
    try {
      for (TagData tag : tagData) {
        record = tag.record;
        sb.append(sourceData, head, tag.start);
        sb.append(filler != null ?
            filler.replace(tag.getSelection(sourceData), record)
            :
            "REPLACEMENT-TEXT"
        );
        head = tag.end;
      }
      TagData find = tagData[tagData.length - 1];
      sb.append(sourceData.substring(find.end));
    }
    catch (Exception ee) {
      throw ee;
    }

    return sb.toString();

  }

  public interface Filler {
    String replace(String data, SourceData record);
  }

  public class SourceData implements Serializable {
    private static final long serialVersionUID = -7141697755450816386L;
    public String file;
    public int line, column;

    SourceData(String file, int line, int column) {
      this.file = file;
      this.line = line;
      this.column = column;
    }
  }

  private class TagData implements Serializable {

    private static final long serialVersionUID = -4893689094902204808L;

    int start = 0, end = 0;
    SourceData record;

    TagData(String source, int start) {
      this.start = findTag(marker[START_MARKER], start, source);
      if (haveStart()) {
        this.end = findTag(marker[STOP_MARKER], this.start + LL, source);
        if (haveEnd()) this.end += LY;
      }
    }

    private int findTag(String tag, int start, String source) {
      return source.indexOf(tag, start);
    }

    String getSelection(String source) {
      String data = sourceData.substring(start, end);
      return data.substring(LL, data.length() - LY);
    }

    boolean haveStart() {return start != -1;}

    boolean haveEnd() {return end != -1;}

    boolean haveSelection() {return haveStart() && haveEnd();}

  }

}
