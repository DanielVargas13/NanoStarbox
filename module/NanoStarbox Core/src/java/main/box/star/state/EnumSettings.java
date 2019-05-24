package box.star.state;

import java.io.Serializable;

public class EnumSettings extends Configuration<Enum, Serializable> implements Serializable {

  private static final long serialVersionUID = -1068114640999220388L;

  EnumSettings(Manager<Enum, Serializable> manager) {
    super(manager);
  }
}
