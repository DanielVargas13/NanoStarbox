package box.star.shell;

import box.star.Tools;
import box.star.state.Configuration;
import box.star.state.EnumSettings;

import java.io.Serializable;

import static box.star.shell.Main.Settings.SYSTEM_PROFILE;
import static box.star.shell.Main.Settings.USER_PROFILE;

class SettingsManager extends EnumSettings.Manager<Main.Settings, Serializable> {
  public SettingsManager(Environment environment) {
    super(SettingsManager.class.getSimpleName());
    set(SYSTEM_PROFILE, Tools.switchNull(environment.getString(Main.SHELL_SYSTEM_PROFILE_VARIABLE), System.getProperty(Main.SHELL_SYSTEM_PROFILE_PROPERTY)));
    set(USER_PROFILE, Tools.switchNull(environment.getString(Main.SHELL_USER_PROFILE_VARIABLE), System.getProperty(Main.SHELL_USER_PROFILE_PROPERTY)));
  }
  public SettingsManager(String name, Configuration<Main.Settings, Serializable> parent) {
    super(name, parent);
  }
}
