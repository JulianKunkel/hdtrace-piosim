/*
 *  (C) 2001 by Argonne National Laboratory
 *      See COPYRIGHT in top-level directory.
 */

/*
 *  @author  Anthony Chan
 */

package base.topology;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;

public class Event
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
        g.drawLine( x-1 ,  y + height/2, x-1 , y - height/4);
        g.drawLine( x+1 ,  y + height/2, x+1 , y - height/4);
        
        /* Fill the ellipse first */
        g.fillArc( x - event_base_width/2 , y - height/4, event_base_width, event_base_width, 0, 360 );

        g.setColor( Color.white );
        /* Draw the white ellipse boundray */
        g.drawLine( x ,  y + height/2, x , y - height/4);
        g.drawArc( x - event_base_width/2 , y - height/4, event_base_width, event_base_width, 0, 360 );

        if ( stroke != null )
            g.setStroke( orig_stroke );

        return 1;
    }

		public static void setBaseWidth(int eventBaseWidth) {
			event_base_width = eventBaseWidth;
		}
}
