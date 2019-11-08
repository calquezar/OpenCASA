/*
 *   OpenCASA software v2.0 for video and image analysis
 *   Copyright (C) 2019  Jorge Yagüe
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
 * @author Jorge Yagüe
 *
 */
public class Square implements Serializable {

	@Override
	public String toString() {
		return "Square [x=" + x + ", y=" + y + ", width=" + width + ", height=" + height + "]";
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/** Up left corner */
	public float x;
	/**   */
	public float y;
	/**   */
	public float width;
	/**   */
	public float height;
	/**   */
	public float area;
	/** ID */
	public String id = "*";

	/**
	 * @param source - Square to be copied
	 */
	public void copy(Square source) {
		this.id = source.id;
		this.x = source.x;
		this.y = source.y;
		this.area = source.area;
		this.width = source.width;
		this.height = source.height;
	}

}
