
/** Version Control Information $Id: DrawObjects.java 406 2009-06-16 14:18:45Z kunkel $
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

package de.drawable;

import java.awt.Color;
import java.awt.Graphics2D;

import de.hd.pvs.TraceFormat.ITracableObject;
import de.hd.pvs.TraceFormat.TracableObjectType;
import de.hd.pvs.TraceFormat.trace.IEventTraceEntry;
import de.hd.pvs.TraceFormat.trace.IStateTraceEntry;
import de.hd.pvs.TraceFormat.util.Epoch;


public class DrawObjects{

	final static double nestingMultiplier = 0.7;
	
	public static double getHeightMultiplier(int nestingDepth){
		return Math.pow(nestingMultiplier, nestingDepth);
	}
	
	public static int getNestingDepth(double heightMultiplier){
		return (int) (Math.log(Math.abs(0.5 - heightMultiplier)) / Math.log(nestingMultiplier) - 0.9) - 1;
	}


	public static  int  drawState( Graphics2D g,
			String name,
			CoordPixelXform  coord_xform,
			IStateTraceEntry state,                            
			Color color,
			int nestingDepth,
			int timeline,
			Epoch globalMinTime)
	{	
		int iStart   = coord_xform.convertTimeToPixel( state.getEarliestTime().subtract(globalMinTime).getDouble() );
		int iFinal   = coord_xform.convertTimeToPixel( state.getLatestTime().subtract(globalMinTime).getDouble() );
		
		int height = (int) ( coord_xform.getTimelineHeight() *  getHeightMultiplier(nestingDepth));

		int jStart   = coord_xform.convertTimelineToPixel( timeline ) + (coord_xform.getTimelineHeight() - height) / 2;
		int jFinal   = jStart + height;
		
		return StateDrawer.drawForward( g, color, null , iStart, jStart, iFinal, jFinal, name );
	}
	
	public static void  drawBox(			
			Graphics2D g,
			CoordPixelXform  coord_xform,
			double startTime,
			double endTime,
			Color backGroundcolor,
			int timeline)
	{
		int x1   = coord_xform.convertTimeToPixel( startTime );
		int x2   = coord_xform.convertTimeToPixel( endTime );

		int height = coord_xform.getTimelineHeight();

		int y1   = coord_xform.convertTimelineToPixel( timeline );

		g.setColor( backGroundcolor );
		g.fillRect( x1, y1, x2-x1+1, height);
	}
	
	public static void  drawScrambeledBox(			
			Graphics2D g,
			CoordPixelXform  coord_xform,
			double startTime,
			double endTime,
			int timeline)
	{
		int x1   = coord_xform.convertTimeToPixel( startTime );
		int x2   = coord_xform.convertTimeToPixel( endTime );

		int height = coord_xform.getTimelineHeight();

		int y1   = coord_xform.convertTimelineToPixel( timeline );

		g.setColor( Color.GRAY );
		g.fillRect( x1, y1, x2-x1+1, height);
		
		g.setColor(Color.BLACK);
		g.drawLine(x1, y1, x2, y1 + height);
		g.drawLine(x1, y1 + height, x2, y1);
	}

	public static void  drawStatisticBackground(			
			Graphics2D g,
			CoordPixelXform  coord_xform,
			double startTime,
			double endTime,
			Color backGroundcolor,
			Color color,
			int timeline)
	{
		int x1   = coord_xform.convertTimeToPixel( startTime );
		int x2   = coord_xform.convertTimeToPixel( endTime );

		int height = coord_xform.getTimelineHeight();

		int y1   = coord_xform.convertTimelineToPixel( timeline );

		g.setColor( backGroundcolor );
		g.fillRect( x1, y1, x2-x1+1, height);

		g.setColor( color );
	}
	
	//  assume this Primitive overlaps with coord_xform.TimeBoundingBox
	public static int  drawArrow(			
			Graphics2D g, 
			CoordPixelXform  coord_xform,
			Epoch startTime, Epoch endTime,
			int startTimeLine, int endTimeline,
			Color color )
	{
		int iStart   = coord_xform.convertTimeToPixel( startTime.getDouble() );
		int iFinal   = coord_xform.convertTimeToPixel( endTime.getDouble() );

		int jStart   = coord_xform.convertTimelineToPixel( startTimeLine ) + coord_xform.getTimelineHeight() / 2;
		int jFinal   = coord_xform.convertTimelineToPixel( endTimeline )  + coord_xform.getTimelineHeight() / 2;

		return ArrowDrawer.draw( g, color, null, iStart, jStart, iFinal, jFinal );
	}

	public static int  drawEvent( Graphics2D g, CoordPixelXform coord_xform,
			IEventTraceEntry event,
			int timeline,
			Color color,
			Epoch globalMinTime)
	{

		int iStart   = coord_xform.convertTimeToPixel( event.getEarliestTime().subtract(globalMinTime).getDouble() );

		int jStart   = coord_xform.convertTimelineToPixel( timeline );

		return EventDrawer.draw( g, color, iStart, jStart, (int) coord_xform.getTimelineHeight() );
	}


	/**
	 * Determine the time distance between a time and a trace entry.
	 * 
	 * @param time
	 * @param entry
	 * @return
	 */
	static public double getTimeDistance(Epoch time, ITracableObject entry){
		double distance = Math.abs( entry.getEarliestTime().subtract( time).getDouble() );

		if(entry.getType() == TracableObjectType.STATE){
			final IStateTraceEntry state = (IStateTraceEntry) entry;
			if ( time.compareTo(state.getLatestTime()) <= 0 && time.compareTo(state.getEarliestTime()) >= 0 ){
				// perfect match.
				return 0;
			}

			double distanceRight = Math.abs( state.getLatestTime().subtract(time).getDouble() );

			if(distanceRight < distance){
				return distanceRight;
			}
		}
		return distance;		
	}
}
