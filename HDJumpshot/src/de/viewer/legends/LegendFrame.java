
/** Version Control Information $Id: LegendFrame.java 469 2009-07-01 13:27:24Z kunkel $
 * @lastmodified    $Date: 2009-07-01 15:27:24 +0200 (Mi, 01. Jul 2009) $
 * @modifiedby      $LastChangedBy: kunkel $
 * @version         $Revision: 469 $ 
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

package de.viewer.legends;


import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.border.Border;

import de.hd.pvs.TraceFormat.TraceFormatFileOpener;
import de.hdTraceInput.FileLoadedListener;
import de.hdTraceInput.TraceFormatBufferedFileReader;
import de.viewer.first.TopWindow;


public class LegendFrame extends TopWindow
{
	private static final long serialVersionUID = -935855888445611813L;

	private        LegendTracePanel        trace_panel;
	private        LegendStatisticPanel    statistic_panel;
	private final MyFileLoadedListener fileloadListener = new MyFileLoadedListener();
	private final TraceFormatBufferedFileReader  reader;

	private class LegendTracePanel extends JPanel
	{
		private static final long serialVersionUID = -3333521394333510573L;
		
		private final LegendTable  legend_table;
		private final JScrollPane scroller;

		public LegendTracePanel( final TraceFormatBufferedFileReader  reader )
		{
			super();
			super.setLayout( new BoxLayout( this, BoxLayout.Y_AXIS ) );

			Border  lowered_border, empty_border, etched_border;
			lowered_border = BorderFactory.createLoweredBevelBorder();
			empty_border   = BorderFactory.createEmptyBorder( 4, 4, 4 ,4 );
			etched_border  = BorderFactory.createEtchedBorder();

			legend_table  = new LegendTable(reader.getLegendTraceModel());
			scroller = new JScrollPane( legend_table,	JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,	JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED );
			scroller.setBorder( BorderFactory.createCompoundBorder( lowered_border, BorderFactory.createCompoundBorder(empty_border, etched_border ) ) );
			super.add( scroller );		
		}
		
	}

	private class LegendStatisticPanel extends JPanel{ 
		private static final long serialVersionUID = -3333521394333510573L;

		private final LegendTable  legend_table;
		private final JScrollPane scroller;

		public LegendStatisticPanel( final TraceFormatBufferedFileReader  reader )
		{
			super();
			super.setLayout( new BoxLayout( this, BoxLayout.Y_AXIS ) );

			Border  lowered_border, empty_border, etched_border;
			lowered_border = BorderFactory.createLoweredBevelBorder();
			empty_border   = BorderFactory.createEmptyBorder( 4, 4, 4 ,4 );
			etched_border  = BorderFactory.createEtchedBorder();

			legend_table  = new LegendTable(reader.getLegendStatisticModel());
			scroller = new JScrollPane( legend_table,	JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,	JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED );
			scroller.setBorder( BorderFactory.createCompoundBorder( lowered_border, BorderFactory.createCompoundBorder(empty_border, etched_border ) ) );
			super.add( scroller );
		}
	}
	
	private class MyFileLoadedListener implements FileLoadedListener{
		@Override
		public void additionalFileLoaded(TraceFormatFileOpener file) {	
			reader.getLegendStatisticModel().commitModel();
			reader.getLegendTraceModel().commitModel();
			
			// if there are categories added or removed, update the tables and the scrollpane to reflect changes to visible categories
			statistic_panel.legend_table.revalidate();
			trace_panel.legend_table.revalidate();
		}			
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
		this.reader = reader;
	}
	
	@Override
	protected void destroyWindow() {
		super.destroyWindow();
		reader.removeFileLoadListener(fileloadListener);
	}
	
	@Override
	protected void initWindow() {	
		super.initWindow();
		reader.addFileLoadListener(fileloadListener);
		// init!
		fileloadListener.additionalFileLoaded(null);
	}
}
