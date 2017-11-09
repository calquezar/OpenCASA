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
public class Spermatozoon implements Serializable {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  /**   */
  public String id = "*";
  /**   */
  public boolean flag = false;
  /**   */
  public boolean inTrack = false;
  /**   */
  public int trackNr;
  /**   */
  public float x;
  /**   */
  public float y;
  /**   */
  public int z;
  // Boundary data
  /**   */
  public float bx;
  /**   */
  public float by;
  /**   */
  public float width;
  /**   */
  public float height;
  // Selection variables
  /**   */
  public boolean selected = false;
  // Morphometrics
  /**   */
  public float total_area = -1;
  /**   */
  public float total_perimeter = -1;
  /**   */
  public float total_feret = -1;
  /**   */
  public float total_minFeret = -1;
  /**
   * @param source - Spermatozoon to be copied
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
