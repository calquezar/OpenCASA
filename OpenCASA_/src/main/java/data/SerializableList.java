package data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Carlos Alquezar
 * This class extends ArrayList to make it serializable
 */
public class SerializableList extends ArrayList implements Serializable {

  /**
   * 
   */
  public SerializableList() {}

  /**
   * @param c
   */
  public SerializableList(Collection c) {
    super(c);
  }

  /**
   * @param initialCapacity
   */
  public SerializableList(int initialCapacity) {
    super(initialCapacity);
  }

}
