package box.star.lang;

import box.star.contract.Nullable;

import java.util.IdentityHashMap;
import java.util.Map;

/**
 * An interface to expose runtime objects as context sensitive strings
 */

public interface RuntimeObjectMapping<HOST> {
  interface ConfigurationPort<HOST> {
    HOST setRuntimeLabelResolver(HOST source);
    HOST loadRuntimeLabels(Map<Object, String> map);
  }
  /**
   * @param constVal
   * @return the object label or null
   */
  @Nullable String getRuntimeLabel(Object constVal);
  <T> T createRuntimeObject(String label, T constVal);
  HOST setRuntimeObjects(String label, Object... constVal);
  HOST setRuntimeLabel(String label, Object constVal);
  HOST deleteRuntimeLabel(Object constVal);
  interface WithConfigurationPort<HOST> extends RuntimeObjectMapping<HOST>, ConfigurationPort<HOST>{}

  /**
   * An object implements this interface to advertise it's own runtime label
   */
  interface ObjectWithLabel { String getRuntimeLabel();}

  class Dictionary extends IdentityHashMap<Object, String> {}

}
