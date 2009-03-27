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

import java.awt.Graphics2D;

import de.hd.pvs.TraceFormat.TraceObjectType;
import de.hd.pvs.TraceFormat.trace.EventTraceEntry;
import de.hd.pvs.TraceFormat.trace.StateTraceEntry;
import de.hd.pvs.TraceFormat.trace.XMLTraceEntry;
import de.hd.pvs.TraceFormat.util.Epoch;


public class DrawObjects{
	
	static double nestingMultiplier = 0.8;


	public static  int  drawState( Graphics2D g,
			CoordPixelXform  coord_xform,
			StateTraceEntry state,                            
			ColorAlpha color,
			int nestingDepth,
			int timeline,
			Epoch globalMinTime)
	{	
        int iStart   = coord_xform.convertTimeToPixel( state.getEarliestTime().subtract(globalMinTime).getDouble() );
        int iFinal   = coord_xform.convertTimeToPixel( state.getLatestTime().subtract(globalMinTime).getDouble() );

        float height = (float) ( coord_xform.getTimelineHeight() * Math.pow(nestingMultiplier, nestingDepth) );
        
        int jStart   = coord_xform.convertTimelineToPixel( timeline ) - (int) height/2;
        int jFinal   = coord_xform.convertTimelineToPixel( timeline ) + (int) height/2;
		
		return StateDrawer.drawForward( g, color, null , iStart, jStart, iFinal, jFinal, 1f, true );
	}

	public static int  drawStatistic( 
			Graphics2D g,
			CoordPixelXform  coord_xform,
			Epoch timeStamp,
			double lastTimeStamp,
			float normalizedHeight,
			ColorAlpha color,
			int timeline)
	{

        int iStart   = coord_xform.convertTimeToPixel( lastTimeStamp );
        int iFinal   = coord_xform.convertTimeToPixel( timeStamp.getDouble() );

        float height = coord_xform.getTimelineHeight();
        
        int jStart   = coord_xform.convertTimelineToPixel( timeline ) - (int) height/2;
        int jFinal   = coord_xform.convertTimelineToPixel( timeline ) + (int) height/2;
		
		return StateDrawer.drawForward( g, color, null , iStart, jStart, iFinal, jFinal, 
				normalizedHeight, false );
	}

	//  assume this Primitive overlaps with coord_xform.TimeBoundingBox
	public static int  drawArrow(			
			Graphics2D g, 
			CoordPixelXform  coord_xform,
			Epoch startTime, Epoch endTime,
			int startTimeLine, int endTimeline,
			ColorAlpha color )
	{
		int iStart   = coord_xform.convertTimeToPixel( startTime.getDouble() );
        int iFinal   = coord_xform.convertTimeToPixel( endTime.getDouble() );

        int jStart   = coord_xform.convertTimelineToPixel( startTimeLine );
        int jFinal   = coord_xform.convertTimelineToPixel( endTimeline );

		return ArrowDrawer.draw( g, color, null, iStart, jStart, iFinal, jFinal );
	}

	public static int  drawEvent( Graphics2D g, CoordPixelXform coord_xform,
			EventTraceEntry event,
			int timeline,
			ColorAlpha color,
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
	static public double getTimeDistance(Epoch time, XMLTraceEntry entry){
		double distance = Math.abs( entry.getEarliestTime().subtract( time).getDouble() );
		
		if(entry.getType() == TraceObjectType.STATE){
			final StateTraceEntry state = (StateTraceEntry) entry;
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
