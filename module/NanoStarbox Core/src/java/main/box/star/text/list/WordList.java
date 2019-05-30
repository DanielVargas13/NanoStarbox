package box.star.text.list;

import box.star.state.RuntimeObjectMapping;
import box.star.text.basic.Scanner;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Comparator;

public class WordList implements Serializable, RuntimeObjectMapping.ObjectWithLabel {
  private static final long serialVersionUID = 7943841258072204166L;
  /**
   * <p>A word-list short-circuit is a condition, where a word list fails to correctly
   * match an item because a shorter item matches the longer item first. This method
   * sorts the array from longest to shortest, to ensure that a short-circuit
   * is not possible.</p>
   * <br>
   * @param words
   */
  static private void preventWordListShortCircuit(String[] words){
    boolean longestFirst = true;
    Arrays.sort(words, new Comparator<String>() {
      @Override
      public int compare(String o1, String o2) {
        return (longestFirst)?
            Integer.compare(o2.length(), o1.length()):
            Integer.compare(o1.length(), o2.length());
      }
    });
  }
  final String label;
  final String[] words;
  final int minLength, maxLength;
  public WordList(String label, String... words){
    this.label = label;
    this.words = new String[words.length];
    System.arraycopy(words, 0, words, 0, words.length);
    preventWordListShortCircuit(this.words);
    int min = Integer.MAX_VALUE, max = 0, l;
    for (String w: words) {
      l = w.length();
      if (l < min) min = l;
      if (l > max) max = l;
    }
    minLength = min;
    maxLength = max;
  }
  @Override
  public String getRuntimeLabel() {
    return label;
  }
  @Override
  public String toString() {
    return getRuntimeLabel();
  }
  public boolean contains(String string){
    for (String word:words) if (word.equals(string)) return true;
    return false;
  }
  public boolean containsIgnoreCase(String string){
    for(String word:words) if (word.equalsIgnoreCase(string))return true;
    return false;
  }
  public int getMaxLength() {
    return maxLength;
  }
  public int getMinLength() {
    return minLength;
  }

  public int size(){
    return words.length;
  }

}
