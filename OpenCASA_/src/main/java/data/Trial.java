package data;

import java.io.Serializable;

import ij.ImagePlus;

/**
 * @author Carlos Alquezar
 *
 */
public class Trial implements Serializable {
  /**   */
  public String ID = "";
  /**   */
  public String type = "";
  /** source's filename  */
  public String source = "";
  /**   */
  public SList tracks = null;
  /**   */
  public int fieldWidth = 0;
  /**   */
  public int fieldHeight = 0;
  /**   */
  public Trial() {
  }
  /**
   * @param ID
   * @param type
   * @param source
   * @param t
   */
  public Trial(String ID, String type, String source, SList t) {
    this.ID = ID;
    this.type = type;
    this.source = source;
    this.tracks = t;
  }
  /**
   * @param ID
   * @param type
   * @param source
   * @param t
   * @param imp
   * @param motileSperm
   */
  public Trial(String ID, String type, String source, SList t,int width, int height) {
    this.ID = ID;
    this.type = type;
    this.source = source;
    this.tracks = t;
    this.fieldWidth = width;
    this.fieldHeight = height;
  }

}
