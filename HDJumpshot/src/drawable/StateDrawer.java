/*
 *  (C) 2001 by Argonne National Laboratory
 *      See COPYRIGHT in top-level directory.
 */

/*
 *  @author  Anthony Chan
 */

package drawable;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;

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
			int x2, int y2,
			float percentFilled, boolean drawBorder)
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

		if(percentFilled != 1f){
			//state with filled bar !
			Color newColor;
			if(color.getBlue() + color.getRed() + color.getGreen() > 120){
				//make it darker:
				newColor = new Color(color.getRed()/2, color.getGreen()/2, color.getBlue()/2,
						color.getAlpha());
			}else{ //make it brighter, take extra care for real black !
				newColor = new Color((color.getRed()+5)*2, (color.getGreen()+5)*2, (color.getBlue()+5)*2,
						color.getAlpha());
			}
			g.setColor( newColor );
			//System.out.println(g.getColor() + " NEW:" + newColor +  " OLD:" + color);

			g.fillRect( x1, y1, x2-x1+1, (int) (height * (1.0f - percentFilled) ));
		}

		if(drawBorder){ //normal state with border
			BorderStyle.paintStateBorder( g, color,
					x1, y1, true,
					x2, y2, true );
		}
		return 1;
	}

	/*
        Check if a point in pixel coordinate is in a Rectangle
        specified between left-upper vertex (start_time, start_ypos)
        and right-lower vertex (final_time, final_ypos)
        Assume caller guarantees the order of timestamps and ypos, such that
        start_time <= final_time  and  start_ypos <= final_ypos
	 */
	public static boolean isPixelIn( Point pt,
			int x1, int y1,
			int x2, int y2)
	{
		int      pt_x, pt_y;

		pt_y     = pt.y;

		if ( pt_y < y1  )
			return false;

		if ( pt_y > y2 )
			return false;

		pt_x     = pt.x;

		if ( pt_x < x1 )
			return false;

		if ( pt_x > x2 )
			return false;

		return true;
	}
}
