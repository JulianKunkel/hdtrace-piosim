
 /** Version Control Information $Id$
  * @lastmodified    $Date$
  * @modifiedby      $LastChangedBy$
  * @version         $Revision$ 
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

package drawable;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;

public class ArrowDrawer
{
	private static       int     Head_Length      = 15;
	private static       int     Head_Half_Width  = 5;

	//  For Viewer 
	public static void setHeadLength( int new_length )
	{
		Head_Length = new_length;
	}

	//  For Viewer 
	public static void setHeadWidth( int new_width )
	{
		Head_Half_Width = new_width / 2;
		if ( Head_Half_Width < 1 )
			Head_Half_Width = 1;
	}

	/*
        Draw an Arrow between 2 vertices
        (start_time, start_ypos) and (final_time, final_ypos)
        Asssume caller guarantees : start_time <= final_time
	 */
	private static int  drawForward( 
			Graphics2D g, Color color, Stroke stroke,
			int x1, int y1,
			int x2, int y2)
	{
		boolean  isSlopeNonComputable = false;
		double   slope = 0.0;
		if ( x1 != x2 )
			slope = (double) ( y2 - y1 ) / ( x2 - x1 );
		else {
			isSlopeNonComputable = true;
			if ( y1 != y2 )
				if ( y2 > y1 )
					slope = Double.POSITIVE_INFINITY;
				else
					slope = Double.NEGATIVE_INFINITY;
			else
				// x1==x2, y1==y2, same point
				slope = Double.NaN;
		}

		int iHead, iTail, jHead, jTail;

		/* The main line */
		// jHead = slope * ( iHead - x1 ) + y1
		iHead = x1;
		jHead = y1;

		// jTail = slope * ( iTail - x2 ) + y2
		iTail = x2;
		jTail = y2;
		int iLeft, jLeft, iRight, jRight;

		iLeft = 0; jLeft = 0; iRight = 0; jRight = 0;
		/* The left line */
		double cosA, sinA;
		double xBase, yBase, xOff, yOff;
		if ( isSlopeNonComputable ) {
			if ( slope == Double.NaN ) {
				cosA =  1.0d;
				sinA =  0.0d;
			}
			else {
				if ( slope == Double.POSITIVE_INFINITY ) {
					cosA =  0.0d;
					sinA =  1.0d;
				}
				else {
					cosA =  0.0d;
					sinA = -1.0d;
				}
			}
		}else {
			cosA = 1.0d / Math.sqrt( 1.0d + slope * slope );
			sinA = slope * cosA;
		}

		xBase  = iTail - Head_Length * cosA;
		yBase  = jTail - Head_Length * sinA;
		xOff   = Head_Half_Width * sinA;
		yOff   = Head_Half_Width * cosA;
		iLeft  = (int) Math.round( xBase + xOff );
		jLeft  = (int) Math.round( yBase - yOff );
		iRight = (int) Math.round( xBase - xOff );
		jRight = (int) Math.round( yBase + yOff );

		Stroke orig_stroke = null;
		if ( stroke != null ) {
			orig_stroke = g.getStroke();
			g.setStroke( stroke );
		}

		g.setColor( color );
		// Draw the main line with possible characteristic from stroke
		g.drawLine( iHead, jHead, iTail, jTail );

		if ( stroke != null )
			g.setStroke( orig_stroke );

		// Draw the arrow head without stroke's effect
		g.drawLine( iTail,  jTail,   iLeft,  jLeft );
		g.drawLine( iLeft,  jLeft,   iRight, jRight );
		g.drawLine( iRight, jRight,  iTail,  jTail );

		return 1;
	}

	/*
        Draw an Arrow between 2 vertices
        (start_time, start_ypos) and (final_time, final_ypos)
        Asssume caller guarantees : final_time <= start_time
	 */
	private static int  drawBackward( Graphics2D g, Color color, Stroke stroke,                                    
			int x1, int y1,
			int x2, int y2)
	{
		boolean  isSlopeNonComputable = false;
		double   slope = 0.0;
		if ( x1 != x2 )
			slope = (double) ( y1 - y2 ) / ( x1 - x2 );
		else {
			isSlopeNonComputable = true;
			if ( y1 != y2 )
				if ( y1 > y2 )
					slope = Double.POSITIVE_INFINITY;
				else
					slope = Double.NEGATIVE_INFINITY;
			else
				// x1==x2, y1==y2, same point
				slope = Double.NaN;
		}

		int iHead, iTail, jHead, jTail;

		/* The main line */
		// jHead = slope * ( iHead - x1 ) + y1
		iHead = x1;
		jHead = y1;
		iTail = x2;
		jTail = y2;
		int iLeft, jLeft, iRight, jRight;

		iLeft = 0; jLeft = 0; iRight = 0; jRight = 0;
		/* The left line */
		double cosA, sinA;
		double xBase, yBase, xOff, yOff;
		if ( isSlopeNonComputable ) {
			if ( slope == Double.NaN ) {
				cosA = -1.0d;
				sinA =  0.0d;
			}
			else {
				if ( slope == Double.POSITIVE_INFINITY ) {
					cosA =  0.0d;
					sinA = -1.0d;
				}
				else {
					cosA =  0.0d;
					sinA =  1.0d;
				}
			}
		}
		else {
			cosA = - 1.0d / Math.sqrt( 1.0d + slope * slope );
			sinA = slope * cosA;
		}
		xBase  = iTail - Head_Length * cosA;
		yBase  = jTail - Head_Length * sinA;
		xOff   = Head_Half_Width * sinA;
		yOff   = Head_Half_Width * cosA;
		iLeft  = (int) Math.round( xBase + xOff );
		jLeft  = (int) Math.round( yBase - yOff );
		iRight = (int) Math.round( xBase - xOff );
		jRight = (int) Math.round( yBase + yOff );

		Stroke orig_stroke = null;
		if ( stroke != null ) {
			orig_stroke = g.getStroke();
			g.setStroke( stroke );
		}
		g.setColor( color );

		// Draw the main line
		g.drawLine( iTail, jTail, iHead, jHead );
		// Draw the arrow head
		g.drawLine( iTail,  jTail,   iLeft,  jLeft );
		g.drawLine( iLeft,  jLeft,   iRight, jRight );
		g.drawLine( iRight, jRight,  iTail,  jTail );

		if ( stroke != null )
			g.setStroke( orig_stroke );

		return 1;
	}


	public static int  draw( 
			Graphics2D g, Color color, Stroke stroke,
			int x1, int y1,
			int x2, int y2 )
	{
		if ( x1 <= x2 )
			return drawForward( g, color, stroke, x1, y1, x2, y2);
		else
			return drawBackward( g, color, stroke, x1, y1, x2, y2); 
	}
}
