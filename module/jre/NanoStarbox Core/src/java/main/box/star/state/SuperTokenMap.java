package box.star.state;

import box.star.text.token.TokenGenerator;

import java.util.Hashtable;
import java.util.Map;

public class SuperTokenMap<T> implements MapProvider<String, T> {

  protected int[] tokenFormat;

  protected TokenGenerator tokenGenerator = new TokenGenerator();
  protected Map<String, T> map;

  @Override
  public Map<String, T> createMap() {
    return new Hashtable<String, T>();
  }

  public SuperTokenMap(int... lengths){
    tokenFormat = lengths;
    map = createMap();
  }

  public SuperTokenMap(int tokenLength){
    tokenFormat = new int[]{tokenLength};
    map = createMap();
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
