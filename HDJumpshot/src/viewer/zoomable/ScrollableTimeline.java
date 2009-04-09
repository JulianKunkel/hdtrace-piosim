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

package viewer.zoomable;

import java.awt.Dimension;
import java.awt.Point;

import javax.swing.BoundedRangeModel;

import topology.TopologyManager;
import viewer.common.Debug;
import viewer.dialog.InfoDialog;
import de.hd.pvs.TraceFormat.util.Epoch;

abstract public class ScrollableTimeline extends ScrollableObject{
	final private TopologyManager    topologyManager;
	final private BoundedRangeModel  y_model;
	
	public ScrollableTimeline(ModelTime modelTime, 
			BoundedRangeModel   yaxis_model,
			TopologyManager topologyManager, ViewportTimeYaxis viewport) {
		super(modelTime, viewport);
		

		this.topologyManager       = topologyManager;
		this.y_model         = yaxis_model;
	}
	
	public abstract Object getTraceObjectAt(int timeline, Epoch realModelTime, int y);
	
	public abstract InfoDialog getPropertyAt(int timeline, Epoch realModelTime, int y);
	
	@Override
	final public Object getObjectAt(Point local_click) {		
		final CoordPixelImage coord_xform;  // Local Coordinate Transform
		coord_xform = new CoordPixelImage( this, getRowHeight(), super.getTimeBoundsOfImages() );
		
		final int timeline       = coord_xform.convertPixelToTimeline( local_click.y);
		
		if( timeline <= 0 || timeline > topologyManager.getTimelineNumber() ){
			return null;
		}

		final Epoch realTime =  getModelTime().getTimeGlobalMinimum().add(
				coord_xform.convertPixelToTime( local_click.x ));

		return getTraceObjectAt(timeline, realTime, local_click.y - coord_xform.convertTimelineToPixel(timeline));
	}
	
	@Override
	final public InfoDialog getPropertyAt(Point view_click) {
		final CoordPixelImage coord_xform;  // Local Coordinate Transform
		coord_xform = new CoordPixelImage( this, getRowHeight(), super.getTimeBoundsOfImages() );
		
		final int timeline       = coord_xform.convertPixelToTimeline( view_click.y);
		
		if( timeline <= 0 || timeline > topologyManager.getTimelineNumber() ){
			return null;
		}
		
		final Epoch clickedTime =  getModelTime().getTimeGlobalMinimum().add(
				coord_xform.convertPixelToTime( view_click.x ));

		return getPropertyAt(timeline, clickedTime, view_click.y - coord_xform.convertTimelineToPixel(timeline) );
	}
	
	public Dimension getMinimumSize()
	{
		int  min_view_height = 0;
		//  the width below is arbitary
		if ( Debug.isActive() )
			Debug.println( "CanvasTimeline: min_size = "
					+ "(0," + min_view_height + ")" );
		return new Dimension( 0, min_view_height );
	}

	public Dimension getMaximumSize()
	{
		if ( Debug.isActive() )
			Debug.println( "CanvasTimeline: max_size = "
					+ "(" + Short.MAX_VALUE
					+ "," + Short.MAX_VALUE + ")" );
		return new Dimension( Short.MAX_VALUE, Short.MAX_VALUE );
	}

	final public int getJComponentHeight()
	{
		int rows_size = topologyManager.getRowCount() * topologyManager.getRowHeight();
		int view_size = y_model.getMaximum() - y_model.getMinimum() + 1;

		if ( view_size > rows_size )
			return view_size;
		else
			return rows_size;		
	}
	
	protected TopologyManager getTopologyManager() {
		return topologyManager;
	}
	
	protected int getRowCount(){
		return topologyManager.getRowCount();
	}
	
	protected int getRowHeight(){
		return topologyManager.getRowHeight();
	}
	
}
