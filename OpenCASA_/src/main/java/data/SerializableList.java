/*
 *   OpenCASA software v1.0 for video and image analysis
 *   Copyright (C) 2018  Carlos Alquézar
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
import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Carlos Alquezar This class extends ArrayList to make it serializable
 */
public class SerializableList extends ArrayList implements Serializable {

  /**
   * 
   */
  public SerializableList() {
  }

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
