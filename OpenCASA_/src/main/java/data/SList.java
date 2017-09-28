package data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Carlos Alquezar
 *
 */
public class SList extends ArrayList implements Serializable{

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
