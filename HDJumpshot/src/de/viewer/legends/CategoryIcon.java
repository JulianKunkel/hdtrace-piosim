
 /** Version Control Information $Id: CategoryIcon.java 220 2009-04-18 19:12:42Z kunkel $
  * @lastmodified    $Date: 2009-04-18 21:12:42 +0200 (Sa, 18. Apr 2009) $
  * @modifiedby      $LastChangedBy: kunkel $
  * @version         $Revision: 220 $ 
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

package de.viewer.legends;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.Icon;

import de.drawable.Category;
import de.drawable.VisualizedObjectType;
import de.viewer.common.Parameters;

public class CategoryIcon implements Icon
{
	private static final int    ICON_WIDTH           = LegendConst.ICON_WIDTH;
	private static final int    ICON_HEIGHT          = LegendConst.ICON_HEIGHT;
	private static final int    ICON_HALF_WIDTH      = LegendConst.ICON_WIDTH / 2;
	private static final int    ICON_QUARTER_WIDTH   = LegendConst.ICON_WIDTH / 4;
	private static final int    ICON_HALF_HEIGHT     = LegendConst.ICON_HEIGHT / 2;
	private static final int    ICON_QUARTER_HEIGHT  = LegendConst.ICON_HEIGHT / 4;
	private static final int    XOFF                 = 3;
	private static final int    YOFF                 = 2;

	final private      Category type;

	public CategoryIcon( Category type )
	{		
		this.type = type;
	}

	public Category getCategory() {
		return type;
	}
	
	public int getIconWidth()
	{
		return ICON_WIDTH;
	}

	public int getIconHeight()
	{
		return ICON_HEIGHT;
	}

	public void paintIcon( Component cmpo, Graphics g, int x, int y )
	{	
		Color old_color = g.getColor();
		
		if ( type.getTopologyType() == VisualizedObjectType.EVENT)
			this.paintEventIcon( g, x, y );
		else if ( type.getTopologyType() == VisualizedObjectType.STATE)
			this.paintStateIcon( g, x, y );
		else if ( type.getTopologyType() == VisualizedObjectType.ARROW)
			this.paintArrowIcon( g, x, y );
		else if ( type.getTopologyType() == VisualizedObjectType.STATISTIC)
			this.paintStatisticIcon( g, x, y );		
		else
			this.paintBlankIcon( g, x, y );
		
		g.setColor( old_color );
	}

	private void paintStateIcon( Graphics g, int x, int y )
	{
		int x1, y1, x2, y2;

		// Paint the background
		g.setColor( (Color) Parameters.BACKGROUND_COLOR.toValue() );
		g.fillRect( x, y, ICON_WIDTH, ICON_HEIGHT );

		// Paint middle timeline
		x1 = x ;                       y1 = y + ICON_HALF_HEIGHT;
		x2 = x + ICON_WIDTH - 1;       y2 = y1;
		g.setColor( Color.red );
		g.drawLine( x1, y1, x2, y2 );

		x1 = x + XOFF;                   y1 = y + YOFF;
		x2 = x1 + ICON_WIDTH-1-2*XOFF;   y2 = y1 + ICON_HEIGHT-1-2*YOFF;

		// Paint the state's color
		g.setColor( type.getColor() );
		g.fillRect( x1, y1, ICON_WIDTH-2*XOFF, ICON_HEIGHT-2*YOFF );

		Parameters.STATE_BORDER.paintStateBorder( (Graphics2D) g,
				type.getColor(),
				x1, y1, true,
				x2, y2, true );
	}

	private void paintStatisticIcon( Graphics g, int x, int y )
	{
		int x1, y1, x2, y2;

		// Paint the background
		g.setColor( (Color) Parameters.BACKGROUND_COLOR.toValue() );
		g.fillRect( x, y, ICON_WIDTH, ICON_HEIGHT );

		// Paint the statistic's color
		g.setColor( type.getColor() );
		
		x1 = x + XOFF;                   y1 = y + YOFF;
		x2 = x1 + ICON_WIDTH-1-2*XOFF;   y2 = y1 + ICON_HEIGHT-1-2*YOFF;
		
		for (int i=2; i < 10; i++){
			g.drawLine(x1 + 3* i , y1 + i, x1 + 3 * i, ICON_WIDTH/2);
		}

		g.fillRect( x1, y1 + ICON_HEIGHT/2, ICON_WIDTH-2*XOFF, ICON_HEIGHT/2 -2 *XOFF );

		Parameters.STATE_BORDER.paintStateBorder( (Graphics2D) g,
				type.getColor(),
				x1, y1, true,
				x2, y2, true );
	}

	
	private void paintArrowIcon( Graphics g, int x, int y )
	{
		int x1, y1, x2, y2, x3, y3;

		// g.setColor( Color.black );
		g.setColor( (Color) Parameters.BACKGROUND_COLOR.toValue() );
		g.fillRect( x, y, ICON_WIDTH, ICON_HEIGHT );

		g.setColor(type.getColor());

		/* Draw the arrow body */
		x1 = x ;                       y1 = y + ICON_HALF_HEIGHT;
		x2 = x + ICON_WIDTH - 1;       y2 = y1;
		g.drawLine( x1, y1, x2, y2 );

		/* Draw the arrow head */
		x1 = x2;                       y1 = y2;
		x2 = x1 - ICON_QUARTER_WIDTH ; y2 = y1 + ICON_QUARTER_HEIGHT; 
		x3 = x2;                       y3 = y1 - ICON_QUARTER_HEIGHT; 
		g.drawLine( x1, y1, x2, y2 );
		g.drawLine( x1, y1, x3, y3 );
		g.drawLine( x2, y2, x3, y3 );
	}

	private void paintEventIcon( Graphics g, int x, int y )
	{
		int x1, y1, x2, y2;

		// g.setColor( Color.black );
		g.setColor( (Color) Parameters.BACKGROUND_COLOR.toValue() );
		g.fillRect( x, y, ICON_WIDTH, ICON_HEIGHT );

		// Paint middle timeline
		x1 = x ;                       y1 = y + ICON_HALF_HEIGHT;
		x2 = x + ICON_WIDTH - 1;       y2 = y1;
		g.setColor( Color.red );
		g.drawLine( x1, y1, x2, y2 );

		x1 = x + XOFF;                   y1 = y + YOFF;

		// Fill the ellipse
		g.setColor( type.getColor() );
		g.fillArc( x1, y1, ICON_WIDTH-2*XOFF, ICON_QUARTER_HEIGHT, 0, 360 );

		// Draw the ellipse
		g.setColor( Color.white );
		g.drawArc( x1, y1, ICON_WIDTH-2*XOFF, ICON_QUARTER_HEIGHT, 0, 360 );

		// Draw the line marks the event
		x1 = x + ICON_HALF_WIDTH;        y1 = y + YOFF + ICON_QUARTER_HEIGHT;
		x2 = x1;                         y2 = y + ICON_HEIGHT-1 - 2;
		g.drawLine( x1, y1, x2, y2 );
	}

	private void paintBlankIcon( Graphics g, int x, int y )
	{
		// g.setColor( Color.black );
		g.setColor( (Color) Parameters.BACKGROUND_COLOR.toValue() );
		g.fillRect( x, y, ICON_WIDTH, ICON_HEIGHT );
	}

}
