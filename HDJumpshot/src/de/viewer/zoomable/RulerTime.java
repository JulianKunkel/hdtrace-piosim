
/** Version Control Information $Id: RulerTime.java 406 2009-06-16 14:18:45Z kunkel $
 * @lastmodified    $Date: 2009-06-16 16:18:45 +0200 (Di, 16. Jun 2009) $
 * @modifiedby      $LastChangedBy: kunkel $
 * @version         $Revision: 406 $ 
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

package de.viewer.zoomable;


import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import de.drawable.TimeBoundingBox;
import de.hd.pvs.TraceFormat.ITracableObject;
import de.viewer.common.AbstractTimelineFrame.ZeroCallback;
import de.viewer.common.Const;
import de.viewer.common.Debug;
import de.viewer.common.Routines;

public class RulerTime extends ScrollableObject
{
	private static final long serialVersionUID = -3385219672039152225L;

	private static final   int   TICKMARK_HEIGHT   = 10;
	private static final   int   I_FONT_BASELINE   = TICKMARK_HEIGHT + Const.FONT_SIZE + 5;
	private static final   int   VIEW_HEIGHT       = I_FONT_BASELINE + 5;

	private DecimalFormat  fmt;

	private ZeroCallback zeroCallback;
	
	@Override
	public boolean applyFilter(String text) {
		return false;
	}
	
	public RulerTime( ScrollbarTimeModel   model, ViewportTime viewport, ZeroCallback zeroCallback )
	{
		super( model, viewport );
		
		this.zeroCallback = zeroCallback;
		fmt         = (DecimalFormat) NumberFormat.getInstance();
		fmt.applyPattern( Const.RULER_TIME_FORMAT );
		
		setUseBackgroundThread(false);
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
	public int getRealImageHeight()
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

			double t_delta = 0;
			i_X_0 = super.time2pixel( t_init );
			
			tInitMark  = Routines.getTimeRulerFirstMark( t_init, tIncrement );
			tFinalMark = timebounds.getLatestTime() + tIncrement;
			

			if(zeroCallback.isStartWithZero()){
				t_delta = - t_init;
				tInitMark = t_init;
			}
			
			for ( time = tInitMark; time < tFinalMark; time += tIncrement ) {
				i_X = super.time2pixel( time ) - i_X_0;
				offGraphics.drawLine( i_X, 1, i_X, TICKMARK_HEIGHT );
				text = fmt.format( time + t_delta);
				offGraphics.drawString( text, i_X - 3, I_FONT_BASELINE );
				if ( Debug.isActive() )
					Debug.print( time + ":" + i_X + ", " ); 
			}
			
			// draw rightmost timestamp
			
			if ( Debug.isActive() )
				Debug.println( "|" );

			offGraphics.dispose();
		}
	}
	
	@Override
	public ITracableObject getObjectAt(Point view_click) {
		return null;
	}
}
