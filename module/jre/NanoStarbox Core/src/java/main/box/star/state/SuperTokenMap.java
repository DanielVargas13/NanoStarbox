package box.star.state;

import box.star.text.token.TokenGenerator;

import java.io.Serializable;
import java.util.Hashtable;
import java.util.Map;

public class SuperTokenMap<V> implements MapFactory<Serializable, V> {

  protected int[] tokenFormat;

  protected TokenGenerator tokenGenerator = new TokenGenerator();
  protected Map<Serializable, V> map;

  @Override
  public Map<Serializable, V> createMap() {
    return new Hashtable<Serializable, V>();
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

  synchronized public String put(V value){
    String token = getNextToken();
    map.put(token, value);
    return token;
  }

  public void set(Serializable key, V value){
    if ( ! map.containsKey(key) ) throw new RuntimeException(new IllegalAccessException("trying to set foreign key data"));
    map.put(key, value);
  }

  public V get(String token){ return map.get(token); }

  public void eraseToken(String token){ map.remove(token); }

}
