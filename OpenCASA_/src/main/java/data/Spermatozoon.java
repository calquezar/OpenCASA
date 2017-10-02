package data;

import java.io.Serializable;

/**
 * @author Carlos Alquezar
 *
 */
public class Spermatozoon implements Serializable {

  /** @brief */
  /**
   * 
   */
  public String id = "*";
  /**
   * 
   */
  public boolean flag = false;
  /**
   * 
   */
  public boolean inTrack = false;
  /**
   * 
   */
  public int trackNr;
  /**
   * 
   */
  public float x;
  /**
   * 
   */
  public float y;
  /**
   * 
   */
  public int z;
  // Boundary data
  /**
   * 
   */
  public float bx;
  /**
   * 
   */
  public float by;
  /**
   * 
   */
  public float width;
  /**
   * 
   */
  public float height;
  // Selection variables
  /**
   * 
   */
  public boolean selected = false;
  // Morphometrics
  /**
   * 
   */
  public float total_area = -1;
  /**
   * 
   */
  public float total_perimeter = -1;
  /**
   * 
   */
  public float total_feret = -1;
  /**
   * 
   */
  public float total_minFeret = -1;

  /**
   * @param source
   *          - Spermatozoon to be copied
   */
  public void copy(Spermatozoon source) {
    this.id = source.id;
    this.x = source.x;
    this.y = source.y;
    this.z = source.z;
    this.trackNr = source.trackNr;
    this.inTrack = source.inTrack;
    this.flag = source.flag;
    this.bx = source.bx;
    this.by = source.by;
    this.width = source.width;
    this.height = source.height;
    this.selected = source.selected;
    this.total_area = source.total_area;
    this.total_perimeter = source.total_perimeter;
    this.total_feret = source.total_feret;
    this.total_minFeret = source.total_minFeret;
  }

  /**
   * @param s
   *          - Spermatozoon used as reference to calculate the distance
   * @return euclidean distance to the Spermatozoon s
   */
  public float distance(Spermatozoon s) {
    return (float) Math.sqrt(Math.pow(this.x - s.x, 2) + Math.pow(this.y - s.y, 2));
  }

}
