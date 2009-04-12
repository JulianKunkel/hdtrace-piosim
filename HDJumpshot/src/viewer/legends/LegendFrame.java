
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

package viewer.legends;

import hdTraceInput.TraceFormatBufferedFileReader;

import java.awt.Dimension;

import javax.swing.JSplitPane;

import viewer.first.TopWindow;


public class LegendFrame extends TopWindow
{
	private static final long serialVersionUID = -935855888445611813L;

	private        LegendTracePanel        trace_panel;
	private        LegendStatisticPanel    statistic_panel;

	@Override
	protected void windowGetsInvisible() {

	}

	@Override
	protected void windowGetsVisible() {

	}

	public LegendFrame( final TraceFormatBufferedFileReader  reader )
	{
		setTitle( "Legend: " + reader.getCombinedProjectFilename() );

		trace_panel = new LegendTracePanel( reader );
		statistic_panel = new LegendStatisticPanel(reader);

		final JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT);        
		split.setOneTouchExpandable(true);

		final Dimension minimumSize = new Dimension(250, 75);
		trace_panel.setMinimumSize(minimumSize);
		statistic_panel.setMinimumSize(minimumSize);

		getFrame().setMinimumSize(new Dimension(250, 500));
		
		split.add(trace_panel);
		split.add(statistic_panel);     

		getFrame().setContentPane( split );                
	}
}
