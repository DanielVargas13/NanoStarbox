package box.star.content;

import box.star.state.Settings;
import org.junit.jupiter.api.Test;

import static box.star.content.SettingsTest.Configuration.*;
import static org.junit.jupiter.api.Assertions.*;

class SettingsTest {

  static enum Configuration {
    HOST_NAME, HOST_IP, HOST_PORT
  }

  Settings appSettings = new Settings(SettingsTest.class.getSimpleName());

  @Test void main(){
    appSettings.set(HOST_NAME, "localhost");
    assertEquals("localhost", appSettings.get(HOST_NAME));
    assertEquals(1, appSettings.size());
  }

}