package box.star.tag;

import java.io.Serializable;
import java.util.Random;

public class TokenGenerator implements Serializable {

  private static final long serialVersionUID = -3724811841134376499L;

  private static final Random RANDOM = new Random();
  private static final char[] HEX = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
  private static int TOKEN_SIZE = 16;

  public TokenGenerator() {}

  private static String generateToken(int size) {
    StringBuilder sb = new StringBuilder(size);
    for (int i = 0; i < size; i++) sb.append(HEX[RANDOM.nextInt(HEX.length)]);
    return sb.toString();
  }

  public String createNewToken(int size) { return generateToken(size); }

  public String createNewToken(int[] sizes) {
    String[] parts = new String[sizes.length];
    for (int i = 0; i < sizes.length; i++) parts[i] = generateToken(sizes[i]);
    return (String.join("-", parts));
  }

}
