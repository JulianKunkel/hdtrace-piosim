/*
 *  (C) 2001 by Argonne National Laboratory
 *      See COPYRIGHT in top-level directory.
 */

/*
 *  @author  Anthony Chan
 */

package base.drawable;

import java.awt.Graphics2D;

import base.topology.Arrow;
import base.topology.Event;
import base.topology.State;
import de.hd.pvs.TraceFormat.statistics.StatisticEntry;
import de.hd.pvs.TraceFormat.trace.EventTraceEntry;
import de.hd.pvs.TraceFormat.trace.StateTraceEntry;
import de.hd.pvs.TraceFormat.util.Epoch;


public class DrawObjects{

	public static  int  drawState( Graphics2D g,
			CoordPixelXform  coord_xform,
			StateTraceEntry state,                            
			ColorAlpha color,
			int nestingDepth,
			int timeline)
	{		
        int iStart   = coord_xform.convertTimeToPixel( state.getTimeStamp().getDouble() );
        int iFinal   = coord_xform.convertTimeToPixel( state.getEndTime().getDouble() );

        float height = coord_xform.getTimelineHeight(timeline) / nestingDepth;
        
        int jStart   = coord_xform.convertTimelineToPixel( timeline ) - (int) height/2;
        int jFinal   = coord_xform.convertTimelineToPixel( timeline ) + (int) height/2;
		
		return State.drawForward( g, color, null , iStart, jStart, iFinal, jFinal, 1f, true );
	}

	public static int  drawStatistic( 
			Graphics2D g,
			CoordPixelXform  coord_xform,
			StatisticEntry currentStatistic,
			StatisticEntry lastStatistic,
			ColorAlpha color,
			int timeline)
	{

        int iStart   = coord_xform.convertTimeToPixel( lastStatistic.getTimeStamp().getDouble() );
        int iFinal   = coord_xform.convertTimeToPixel( currentStatistic.getTimeStamp().getDouble() );

        float height = coord_xform.getTimelineHeight(timeline);
        
        int jStart   = coord_xform.convertTimelineToPixel( timeline ) - (int) height/2;
        int jFinal   = coord_xform.convertTimelineToPixel( timeline ) + (int) height/2;
		
        // TODO replace height with real value...
		return State.drawForward( g, color, null , iStart, jStart, iFinal, jFinal, 0.5f, false );
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

		return Arrow.draw( g, color, null, iStart, jStart, iFinal, jFinal );
	}

	public static int  drawEvent( Graphics2D g, CoordPixelXform coord_xform,
			EventTraceEntry event,
			int timeline,
			ColorAlpha color )
	{

		int iStart   = coord_xform.convertTimeToPixel( event.getTimeStamp().getDouble() );
        
        int jStart   = coord_xform.convertTimelineToPixel( timeline );
        
		return Event.draw( g, color, null, iStart, jStart, 20, (int) coord_xform.getTimelineHeight(timeline) );
	}
}
