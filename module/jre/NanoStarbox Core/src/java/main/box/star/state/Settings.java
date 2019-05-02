package box.star.state;

import java.io.Serializable;

public class Settings extends Configuration<Enum, Serializable> implements Serializable {

  private static final long serialVersionUID = -1068114640999220388L;

  public Settings(String name){super(name);}

}
