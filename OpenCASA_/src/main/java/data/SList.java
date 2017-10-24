package data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Carlos Alquezar
 * This class extends ArrayList to make it Serializable
 */
public class SList extends ArrayList implements Serializable {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  /**
   * 
   */
  public SList() {
  }

  /**
   * @param c
   */
  public SList(Collection c) {
    super(c);
  }

  /**
   * @param initialCapacity
   */
  public SList(int initialCapacity) {
    super(initialCapacity);
  }

}
