
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

package viewer.zoomable;

import java.text.NumberFormat;
import java.text.DecimalFormat;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

import de.hd.pvs.TraceFormat.TraceObject;
import drawable.DrawObjects;
import drawable.TimeBoundingBox;

import viewer.common.Const;
import viewer.common.Debug;
import viewer.common.Routines;
import viewer.dialog.InfoDialog;

public class RulerTime extends ScrollableObject
{
    private static final   Font  FONT              = Const.FONT;
    private static final   int   FONT_SIZE         = FONT.getSize();
    private static final   int   TICKMARK_HEIGHT   = 10;
    private static final   int   I_FONT_BASELINE   = TICKMARK_HEIGHT
                                                   + FONT_SIZE + 5;
    private static final   int   VIEW_HEIGHT       = I_FONT_BASELINE + 5;

    private double         tRange;
    private double         tIncrement;
    private DecimalFormat  fmt;
    
    public RulerTime( ModelTime model )
    {
        super( model );
        tRange      = 0.0d;
        tIncrement  = 0.0d;
        fmt         = (DecimalFormat) NumberFormat.getInstance();
        fmt.applyPattern( Const.RULER_TIME_FORMAT );
    }

    public Dimension getMinimumSize()
    {
        //  the width below is arbitary
        if ( Debug.isActive() )
            Debug.println( "RulerTime: min_size = "
                         + "(0," + VIEW_HEIGHT + ")" );
        return new Dimension( 0, VIEW_HEIGHT );
    }

    public Dimension getMaximumSize()
    {
        if ( Debug.isActive() )
            Debug.println( "RulerTime: max_size = "
                         + "(" + Short.MAX_VALUE + "," + VIEW_HEIGHT + ")" );
        return new Dimension( Short.MAX_VALUE, VIEW_HEIGHT );
    }

    //  Function defined the height of the JComponent.
    public int getJComponentHeight()
    {
        return VIEW_HEIGHT;
    }

    protected void initializeAllOffImages( final TimeBoundingBox imgs_times )
    {}

    protected void finalizeAllOffImages( final TimeBoundingBox imgs_times )
    {}

    protected void drawOneOffImage(       Image            offImage,
                                   final TimeBoundingBox  timebounds )
    {
        if ( Debug.isActive() )
            Debug.println( "RulerTime: drawOneOffImage()'s offImage = "
                         + offImage );
        if ( offImage != null ) {
            // int offImage_width = visible_size.width * NumViewsPerImage;
            int offImage_width   = offImage.getWidth( this );
            int offImage_height  = offImage.getHeight( this ); 
            // int offImage_height  = VIEW_HEIGHT; 
            Graphics offGraphics = offImage.getGraphics();

            // offGraphics.getClipBounds() returns null
            // offGraphics.setClip( 0, 0, getWidth()/NumImages, getHeight() );
            // Do the ruler labels in a small font that's black.
            offGraphics.setColor( Color.white );
            offGraphics.fillRect( 0, 0, offImage_width, offImage_height );
            offGraphics.setFont( FONT );
            offGraphics.setColor( Color.black );

            if ( timebounds.getDuration() != tRange ) {
                tRange     = timebounds.getDuration();
                tIncrement = tRange / ( NumViewsPerImage * 10.0 );
                tIncrement = Routines.getTimeRulerIncrement( tIncrement );
            }

            double time, tInitMark, tFinalMark;
            int    i_X, i_X_0;
            String text = null;

            if ( Debug.isActive() )
                Debug.print( "RulerTime.drawOffImage at : " );
            double t_init = timebounds.getEarliestTime();
            i_X_0 = super.time2pixel( t_init );
            tInitMark  = Routines.getTimeRulerFirstMark( t_init, tIncrement );
            tFinalMark = timebounds.getLatestTime() + tIncrement;
            for ( time = tInitMark; time < tFinalMark; time += tIncrement ) {
                i_X = super.time2pixel( time ) - i_X_0;
                // offGraphics.drawLine( i_X, offImage_height-1,
                //                       i_X, 
                //                       offImage_height-TICKMARK_HEIGHT-1 );
                offGraphics.drawLine( i_X, 1, i_X, TICKMARK_HEIGHT );
                text = fmt.format( time );
                offGraphics.drawString( text, i_X - 3, I_FONT_BASELINE );
                if ( Debug.isActive() )
                    Debug.print( time + ":" + i_X + ", " ); 
            }
            if ( Debug.isActive() )
                Debug.println( "|" );

            offGraphics.dispose();
        }
    }

    @Override
    public TraceObject getDrawableAt(Point view_click, TimeBoundingBox vport_times) {
    return null;
    }
    
    
    /*   Empty content to fulfill ScrollableObject's requirement  */
    public InfoDialog getPropertyAt( final Point            view_click,
                                     final TimeBoundingBox  vport_times )
    {
        return super.getTimePropertyAt( view_click );
    }

}
