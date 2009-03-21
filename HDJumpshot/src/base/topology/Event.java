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
    public static int  draw( Graphics2D g, Color color, Stroke stroke,
			int x, int y, // center position
			int width, int height)
    {
        Stroke orig_stroke = null;
        if ( stroke != null ) {
            orig_stroke = g.getStroke();
            g.setStroke( stroke );
        }
        
        g.setColor( color );
        g.drawLine( x ,  y + height/4, x , y - height/4);
        /* Fill the ellipse first */
        g.fillArc( x - width/2 , y - height/4, width, height/8, 0, 360 );

        g.setColor( Color.white );
        /* Draw the white ellipse boundray */
        g.drawArc( x - width/2 , y - height/4, width, height/8, 0, 360 );

        if ( stroke != null )
            g.setStroke( orig_stroke );

        return 1;
    }
}
