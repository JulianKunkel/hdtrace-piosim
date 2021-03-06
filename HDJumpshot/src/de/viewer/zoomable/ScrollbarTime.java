
/** Version Control Information $Id: ScrollbarTime.java 257 2009-05-01 09:12:52Z kunkel $
 * @lastmodified    $Date: 2009-05-01 11:12:52 +0200 (Fr, 01. Mai 2009) $
 * @modifiedby      $LastChangedBy: kunkel $
 * @version         $Revision: 257 $ 
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

import java.awt.Dimension;

import javax.swing.JScrollBar;

import de.viewer.common.Const;
import de.viewer.common.TimeEvent;
import de.viewer.common.TimeListener;


/**
 * X-Axis Scrollbar for the time.
 *
 */
public class ScrollbarTime extends JScrollBar implements TimeListener
{
	private static final long serialVersionUID = 1L;
	final private ScrollbarTimeModel   model;
	final private Dimension            min_size;

	public ScrollbarTime( ScrollbarTimeModel   model )
	{
		super( JScrollBar.HORIZONTAL );
		this.model = model;		

		setModel( model );
		this.addAdjustmentListener( model );

		super.setUnitIncrement( Const.TIME_SCROLL_UNIT_INIT );

		min_size = super.getMinimumSize();
		if ( min_size.height <= 0 )
			min_size.height = 20;
	}

	@Override
	public void timeChanged(TimeEvent evt) {
		// update scroll position because model time changed.
		model.setDisableAdjustmentListener(true);
		model.updateScrollPosition();
		setScrollBarIncrements();
		model.setDisableAdjustmentListener(false);
	}

	private void setScrollBarIncrements() throws IllegalStateException
	{
		int sb_block_incre, sb_unit_incre;
		sb_block_incre = model.getExtent();
		if ( sb_block_incre <= 0 ) {
			throw new IllegalStateException(
					"You have reached the Zoom limit! "
					+ "Time ScrollBar has 0 BLOCK Increment. "
					+ "Zoom out or risk crashing the viewer." );
		}
		this.setBlockIncrement( sb_block_incre );
		
		sb_unit_incre  =  model.getScrollbarIncrement();
		if ( sb_unit_incre <= 0 ) {
			throw new IllegalStateException( "You have reached the Zoom limit! "
					+ "Time ScrollBar has 0 UNIT Increment. "
					+ "Zoom out or risk crashing the viewer." );
		}

		this.setUnitIncrement( sb_unit_incre );
	}

	@Override
	public Dimension getMinimumSize()
	{
		return min_size;
	}

}
