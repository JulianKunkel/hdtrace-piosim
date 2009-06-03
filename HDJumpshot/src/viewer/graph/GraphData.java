//	Copyright (C) 2009 Julian M. Kunkel
//	
//	This file is part of HDJumpshot.
//	
//	HDJumpshot is free software: you can redistribute it and/or modify
//	it under the terms of the GNU General Public License as published by
//	the Free Software Foundation, either version 3 of the License, or
//	(at your option) any later version.
//	
//	HDJumpshot is distributed in the hope that it will be useful,
//	but WITHOUT ANY WARRANTY; without even the implied warranty of
//	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//	GNU General Public License for more details.
//	
//	You should have received a copy of the GNU General Public License
//	along with HDJumpshot.  If not, see <http://www.gnu.org/licenses/>.

package viewer.graph;
import java.awt.Color;

/**
 * Abstract interface for a 2D graph data.
 * Contains tuples of points (X, Y) 
 * 
 * @author Julian M. Kunkel
 *
 */
abstract public class GraphData{
	final String title;
	final Color color;
	
	public GraphData(String title, Color color) {
		this.title = title;
		this.color = color;
	}
	
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