package box.star.shell;

import java.util.Map;

public interface EnvironmentExporter {

  /**
   * @return exports in runtime object form, for internal operations
   */
  Environment getExports();

  /**
   * @return exports in pure form, for external operations
   */
  Map<String, String> getSerializedExports();

}
