
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


import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import viewer.common.Const;
import viewer.common.Debug;
import viewer.common.Routines;
import de.hd.pvs.TraceFormat.TraceObject;
import drawable.TimeBoundingBox;

public class RulerTime extends ScrollableObject
{
	private static final long serialVersionUID = -3385219672039152225L;

	private static final   int   TICKMARK_HEIGHT   = 10;
	private static final   int   I_FONT_BASELINE   = TICKMARK_HEIGHT + Const.FONT_SIZE + 5;
	private static final   int   VIEW_HEIGHT       = I_FONT_BASELINE + 5;

	private DecimalFormat  fmt;

	public RulerTime( ScrollbarTimeModel   model )
	{
		super( model );
		fmt         = (DecimalFormat) NumberFormat.getInstance();
		fmt.applyPattern( Const.RULER_TIME_FORMAT );
	}

	public Dimension getMinimumSize()
	{
		//  the width below is arbitary
		if ( Debug.isActive() )
			Debug.println( "RulerTime: min_size = "	+ "(0," + VIEW_HEIGHT + ")" );
		return new Dimension( 30, VIEW_HEIGHT );
	}

	public Dimension getMaximumSize()
	{
		if ( Debug.isActive() )
			Debug.println( "RulerTime: max_size = "	+ "(" + Short.MAX_VALUE + "," + VIEW_HEIGHT + ")" );
		return new Dimension( Short.MAX_VALUE, VIEW_HEIGHT );
	}

	@Override
	//  Function defined the height of the JComponent.
	public int getJComponentHeight()
	{
		return VIEW_HEIGHT;
	}

	@Override
	protected void drawOneImageInBackground(       
			Image offImage,
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
			offGraphics.setFont( Const.FONT );
			offGraphics.setColor( Color.black );


			final double   tRange = timebounds.getDuration();
			double         tIncrement;
			tIncrement = tRange / ( NumViewsPerImage * 10.0 );
			tIncrement = Routines.getTimeRulerIncrement( tIncrement );
			
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
	public TraceObject getObjectAt(Point view_click) {
		return null;
	}
}
