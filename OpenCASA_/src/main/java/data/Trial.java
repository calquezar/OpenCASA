/*
 *   OpenCASA software v0.8 for video and image analysis
 *   Copyright (C) 2017  Carlos Alquézar
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/    

package data;

import java.io.Serializable;

/**
 * @author Carlos Alquezar
 *
 */
public class Trial implements Serializable {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  /**   */
  public String ID = "";
  /**   */
  public String type = "";
  /** source's filename  */
  public String source = "";
  /**   */
  public SerializableList tracks = null;
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
  public Trial(String ID, String type, String source, SerializableList t) {
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
  public Trial(String ID, String type, String source, SerializableList t,int width, int height) {
    this.ID = ID;
    this.type = type;
    this.source = source;
    this.tracks = t;
    this.fieldWidth = width;
    this.fieldHeight = height;
  }

}
