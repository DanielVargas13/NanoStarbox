package box.star.state;

import box.star.contract.Nullable;

import java.util.IdentityHashMap;
import java.util.Map;

/**
 * An interface to expose runtime objects as context sensitive strings
 */

public interface RuntimeObjectMapping<HOST> {
  interface ConfigurationPort<HOST> {
    HOST resolveLookupsWith(HOST source);
    HOST copyObjectLabels(Map<Object, String> map);
  }
  /**
   * @param constVal
   * @return the object label or null
   */
  @Nullable String getObjectLabel(Object constVal);
  HOST setObjectLabel(Object constVal, String label);
  HOST clearObjectLabel(Object constVal);
  interface WithConfigurationPort<HOST> extends RuntimeObjectMapping<HOST>, ConfigurationPort<HOST>{}

  /**
   * An object implements this interface to advertise it's own runtime label
   */
  interface ObjectWithLabel { String getRuntimeLabel();}

  class Dictionary extends IdentityHashMap<Object, String> {}

}
