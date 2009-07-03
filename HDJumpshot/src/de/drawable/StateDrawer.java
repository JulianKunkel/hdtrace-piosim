
 /** Version Control Information $Id: StateDrawer.java 177 2009-04-02 16:39:18Z kunkel $
  * @lastmodified    $Date: 2009-04-02 18:39:18 +0200 (Do, 02. Apr 2009) $
  * @modifiedby      $LastChangedBy: kunkel $
  * @version         $Revision: 177 $ 
  */

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


/*
 *  (C) 2001 by Argonne National Laboratory
 *      See COPYRIGHT in top-level directory.
 */

/*
 *  @author Anthony Chan (Jumpshot 4), Julian M. Kunkel
 */

package de.drawable;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Insets;

public class StateDrawer
{
	private static StateBorder BorderStyle  = StateBorder.WHITE_RAISED_BORDER;

	public static void setBorderStyle( final StateBorder state_border )
	{
		BorderStyle = state_border;
	}

	/*
        Draw a Rectangle between left-upper vertex (start_time, start_ypos)
        and right-lower vertex (final_time, final_ypos)
        Assume caller guarantees the order of timestamps and ypos, such that
        start_time <= final_time  and  start_ypos <= final_ypos.
	 */
	public static int  drawForward( Graphics2D g, Color color, Insets insets,
			int x1, int y1,
			int x2, int y2)
	{
		if ( insets != null ) {
			x1 += insets.left;
			x2 -= insets.right;
			y1 += insets.top;
			y2 -= insets.bottom;
		}

		// Fill the color of the rectangle
		g.setColor( color );
		int height = y2-y1+1;
		g.fillRect( x1, y1, x2-x1+1, height );

		BorderStyle.paintStateBorder( g, color,	x1, y1, true, x2, y2, true );
		return 1;
	}
}
