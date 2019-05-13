package box.star.state;

import box.star.text.TokenGenerator;

public class TokenCache<T> extends TokenMap<T> {

  protected int[] tokenFormat;
  protected TokenGenerator tokenGenerator = new TokenGenerator();
  protected CacheMap<String, T> map;

  public void setCacheMonitor(CacheMapMonitor<String, T> cacheMonitor){
    map.setMonitor(cacheMonitor);
  }

  public TokenCache(long duration, int... lengths){
    tokenFormat = lengths;
    map = new CacheMap<>(duration, true);
  }

  public TokenCache(long duration, int tokenLength){
    tokenFormat = new int[]{tokenLength};
    map = new CacheMap<>(duration, true);
  }

  private String getNextToken(){
    String token;
    do { token = tokenGenerator.createNewToken(tokenFormat); }
    while (map.containsKey(token));
    return token;
  }

  public String put(T value){
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
