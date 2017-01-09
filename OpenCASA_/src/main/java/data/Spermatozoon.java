package spermAnalysis;


public class Spermatozoon {
	
	/** @brief */
	boolean flag=false;
	public boolean inTrack=false;
	public int	trackNr;
	public float	x;
	public float	y;
	public int	z;
	
	/**
	 * @param source - Spermatozoon to be copied
	 */
	public void copy(Spermatozoon source) {
		this.x=source.x;
		this.y=source.y;
		this.z=source.z;
		this.trackNr=source.trackNr;
		this.inTrack=source.inTrack;
		this.flag=source.flag;
	}
	
	/**
	 * @param s - Spermatozoon used as reference to calculate the distance
	 * @return euclidean distance to the Spermatozoon s
	 */
	public float distance (Spermatozoon s) {
		return (float) Math.sqrt(Math.pow(this.x-s.x, 2) + Math.pow(this.y-s.y, 2));
	}

}
