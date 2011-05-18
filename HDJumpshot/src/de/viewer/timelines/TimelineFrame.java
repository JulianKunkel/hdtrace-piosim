
/** Version Control Information $Id: TimelineFrame.java 303 2009-05-24 14:05:27Z kunkel $
 * @lastmodified    $Date: 2009-05-24 16:05:27 +0200 (So, 24. Mai 2009) $
 * @modifiedby      $LastChangedBy: kunkel $
 * @version         $Revision: 303 $ 
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

package de.viewer.timelines;


import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;

import de.hdTraceInput.TraceFormatBufferedFileReader;
import de.topology.TopologyInputPlugin;
import de.viewer.common.AbstractTimelineFrame;
import de.viewer.common.IconManager;
import de.viewer.common.ModelInfoPanel;
import de.viewer.common.ModelTime;
import de.viewer.common.TimelineToolBar;
import de.viewer.common.IconManager.IconType;
import de.viewer.timelines.topologyPlugins.MPIRankInputPlugin;
import de.viewer.timelines.topologyPlugins.MPIThreadInputPlugin;
import de.viewer.zoomable.ActionSearchBackward;
import de.viewer.zoomable.ActionSearchForward;
import de.viewer.zoomable.ActionSearchInit;
import de.viewer.zoomable.ScrollableObject;


public class TimelineFrame extends AbstractTimelineFrame<TraceObjectInformation>
{
	private static final long serialVersionUID = -496973267971206572L;

	private JButton                 searchBack_btn;
	private JButton                 searchInit_btn;
	private JButton                 searchFore_btn;
	
	/**
	 * Text filter, allows to filter events with a specific attribute or allows to render them as a heat map 
	 */
	private JTextField              txtFilter;
	
	/**
	 * Text filter for the heat maps
	 */
	private JTextField              heatMapFilter;


	public TimelineFrame( final TraceFormatBufferedFileReader reader, final ModelTime modelTime )
	{		
		super(reader, modelTime);
		setTitle("TimeLine: " + reader.getCombinedProjectFilename());

		getFrame().setMinimumSize(new Dimension(700, 500));		
	}

	@Override
	protected void addOwnPanelsOrToolbars(JPanel menuPanel) {
		// no own toolbars.
	}
	
	@Override
	protected void fireNestedStateChanged() {
		getCanvasArea().redrawIfAutoRedraw();
	}
	
	@Override
	protected void addToToolbarMenu(TimelineToolBar toolbar, IconManager iconManager, Insets insets) {
				
		searchBack_btn = new JButton( iconManager.getActiveToolbarIcon(IconType.SearchLeft) );
		searchBack_btn.setMargin( insets );
		searchBack_btn.setToolTipText( "Search Backward in time" );
		searchBack_btn.setMnemonic( KeyEvent.VK_B );
		searchBack_btn.addActionListener(
				new ActionSearchBackward( getTimeCanvasVport() ) );
		toolbar.add( searchBack_btn );

		searchInit_btn = new JButton( iconManager.getActiveToolbarIcon(IconType.Search) );
		searchInit_btn.setMargin( insets );
		searchInit_btn.setToolTipText(
				"Search Initialization from last popup InfoBox's time" );
		searchInit_btn.setMnemonic( KeyEvent.VK_S );
		searchInit_btn.addActionListener(
				new ActionSearchInit( getTimeCanvasVport() ) );
		toolbar.add( searchInit_btn );

		searchFore_btn = new JButton( iconManager.getActiveToolbarIcon(IconType.SearchRight) );
		searchFore_btn.setMargin( insets );
		searchFore_btn.setToolTipText( "Search Forward in time" );
		searchFore_btn.setMnemonic( KeyEvent.VK_F );
		searchFore_btn.addActionListener(
				new ActionSearchForward( getTimeCanvasVport() ) );
		toolbar.add( searchFore_btn );

		toolbar.addSeparator();
		
		txtFilter = new JTextField(30);
		txtFilter.setMargin( insets );
		txtFilter.setToolTipText("Enter the attributes you want to filter with, e.g. use size > 100 & tag == 3 to filter events which match both entries AND: & and OR: | and () are supported.\nA HeatMap can be created based on an attribute by specifying heatmap: size");
		txtFilter.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {				
				// color the text filter if it is invalid!
				if( ((CanvasTimeline) getCanvasArea()).getFilterListener().applyFilter(txtFilter.getText()) ){
					txtFilter.setBackground(Color.WHITE);
				}else{
					txtFilter.setBackground(Color.RED);
				}
			}
		});
		toolbar.add(txtFilter);
		
		

		heatMapFilter = new JTextField(5);
		heatMapFilter.setMargin( insets );
		heatMapFilter.setToolTipText("If you want to apply a heatmap based on attributes, specify them in a mathematical expression in poland prefix e.g. + size tag means size*tag, ^ means maximum of the following two expressioons and _ the minimum.");
		heatMapFilter.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {				
				// color the text filter if it is invalid!
				if( ((CanvasTimeline) getCanvasArea()).getHeatMapListener().applyFilter(heatMapFilter.getText()) ){
					heatMapFilter.setBackground(Color.WHITE);
				}else{
					heatMapFilter.setBackground(Color.RED);
				}
			}
		});
		toolbar.add(heatMapFilter);
	}

	@Override
	protected ModelInfoPanel<TraceObjectInformation> createModelInfoPanel() {
		return new TimelineTraceObjectInfoPanel(getReader());
	}

	@Override
	protected ScrollableObject createCanvasArea() {
		return new CanvasTimeline( getScrollbarTimeModel(), getTimeCanvasVport(), 
				getReader(),  getYModel(), getTopologyManager(), this);
	}
	
	@Override
	protected List<Class<? extends TopologyInputPlugin>> getAvailablePlugins() {
		LinkedList<Class<? extends TopologyInputPlugin>> plugins = new LinkedList<Class<? extends TopologyInputPlugin>>();
		
		plugins.add(MPIRankInputPlugin.class);
		plugins.add(MPIThreadInputPlugin.class);
		
		return plugins;
	}
}
