package box.star.state;

import box.star.text.TokenGenerator;

import java.util.concurrent.ConcurrentHashMap;

public class SharedTokenMap<T> {

  protected int[] tokenFormat;

  protected TokenGenerator tokenGenerator = new TokenGenerator();
  protected ConcurrentHashMap<String, T> map;

  public SharedTokenMap(int... lengths){
    tokenFormat = lengths;
    map = new ConcurrentHashMap<>();
  }

  public SharedTokenMap(int tokenLength){
    tokenFormat = new int[]{tokenLength};
    map = new ConcurrentHashMap<>();
  }

  private String getNextToken(){
    String token;
    do { token = tokenGenerator.createNewToken(tokenFormat); }
    while (map.containsKey(token));
    return token;
  }

  synchronized public String put(T value){
    String token = getNextToken();
    map.put(token, value);
    return token;
  }

  public void set(String key, T value){
    if ( ! map.containsKey(key) ) throw new RuntimeException(new IllegalAccessException("trying to set foreign key data"));
    map.put(key, value);
  }

  public T get(String token){ return map.get(token); }

  public void eraseToken(String token){ map.remove(token); }

}
