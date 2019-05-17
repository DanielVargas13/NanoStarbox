package box.star.state;

import box.star.text.TokenGenerator;

import java.io.File;

public class TokenCache<T> extends TokenMap<T> {

  protected int[] tokenFormat;
  protected TokenGenerator tokenGenerator = new TokenGenerator();
  protected CacheMap<String, T> map;
  private boolean configuredForDiskSynchronization;
  public TokenCache(long duration, int... lengths) {
    tokenFormat = lengths;
    map = new CacheMap<>(duration, true);
  }

  public TokenCache(long duration, int tokenLength) {
    tokenFormat = new int[]{tokenLength};
    map = new CacheMap<>(duration, true);
  }

  public boolean isConfiguredForDiskSynchronization() {
    return configuredForDiskSynchronization;
  }

  public TokenCache<T> setMonitor(CacheMapMonitor<String, T> cacheMonitor) {
    map.setMonitor(cacheMonitor);
    return this;
  }

  public TokenCache<T> synchronize() {
    map.synchronize();
    return this;
  }

  public TokenCache<T> setSynchronization(File synchronization, CacheMapLoader<String, T> cacheMapLoader) {
    map.setSynchronization(synchronization, cacheMapLoader);
    configuredForDiskSynchronization = true;
    return this;
  }

  private String getNextToken() {
    String token;
    do { token = tokenGenerator.createNewToken(tokenFormat); }
    while (map.containsKey(token));
    return token;
  }

  public String put(T value) {
    String token = getNextToken();
    map.put(token, value);
    return token;
  }

  public void set(String key, T value) {
    if (!map.containsKey(key)) throw new RuntimeException(new IllegalAccessException("trying to set foreign key data"));
    map.put(key, value);
  }

  public T get(String token) { return map.get(token); }

  public void eraseToken(String token) { map.remove(token); }

}
