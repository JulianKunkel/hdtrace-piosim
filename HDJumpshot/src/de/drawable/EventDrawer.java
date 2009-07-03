
/** Version Control Information $Id: EventDrawer.java 188 2009-04-05 14:00:14Z kunkel $
 * @lastmodified    $Date: 2009-04-05 16:00:14 +0200 (So, 05. Apr 2009) $
 * @modifiedby      $LastChangedBy: kunkel $
 * @version         $Revision: 188 $ 
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
import java.awt.Stroke;

public class EventDrawer
{
	static int event_base_width = 30;

	public static int  draw( Graphics2D g, Color color, int x, int y, // center position
			int height)
	{
		Stroke stroke = null; // TOOD maybe neat?
		Stroke orig_stroke = null;

		if ( stroke != null ) {
			orig_stroke = g.getStroke();
			g.setStroke( stroke );
		}

		g.setColor( color );
		g.drawLine( x-1 ,  y + height, x-1 , y + height/4);
		g.drawLine( x+1 ,  y + height, x+1 , y + height/4);

		/* Fill the ellipse first */
		g.fillArc( x - event_base_width/2 , y + height/4, event_base_width, event_base_width, 0, 360 );

		g.setColor( Color.white );
		/* Draw the white ellipse boundray */
		g.drawLine( x ,  y + height/2, x , y + height/4);
		g.drawArc( x - event_base_width/2 , y + height/4, event_base_width, event_base_width, 0, 360 );

		if ( stroke != null )
			g.setStroke( orig_stroke );

		return 1;
	}

	public static void setBaseWidth(int eventBaseWidth) {
		event_base_width = eventBaseWidth;
	}
}
