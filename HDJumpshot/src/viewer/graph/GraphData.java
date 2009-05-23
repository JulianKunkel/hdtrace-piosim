package viewer.graph;
import java.awt.Color;

/**
 * Abstract interface for a 2D graph data.
 * Contains tuples of points (X, Y) 
 * 
 * @author julian
 *
 */
abstract class GraphData{
	final String title;
	final Color color;
	
	public GraphData(String title, Color color) {
		this.title = title;
		this.color = color;
	}
	
	/**
	 * Return the number of elements
	 * @return
	 */
	abstract int getCount();
	
	/**
	 * Return an enumeration of the X-axis values
	 * @return
	 */
	abstract public ElementEnumeration getXValues();
	
	/**
	 * Return an enumeration of the Y-axis values
	 * @return
	 */
	abstract public ElementEnumeration getYValues();
	
	abstract public double getMaxX();
	abstract public double getMaxY();
	
	abstract public double getMinX();
	abstract public double getMinY();
}