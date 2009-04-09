
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

package viewer.timelines;

import hdTraceInput.TraceFormatBufferedFileReader;

import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.KeyEvent;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JToolBar;

import viewer.common.AbstractTimelineFrame;
import viewer.common.IconManager;
import viewer.common.ModelInfoPanel;
import viewer.common.IconManager.IconType;
import viewer.zoomable.ActionSearchBackward;
import viewer.zoomable.ActionSearchForward;
import viewer.zoomable.ActionSearchInit;
import viewer.zoomable.ModelTime;
import viewer.zoomable.ScrollableObject;
import viewer.zoomable.ViewportTimeYaxis;
import de.hd.pvs.TraceFormat.TraceObject;

public class TimelineFrame extends AbstractTimelineFrame<TraceObject>
{
	private static final long serialVersionUID = -496973267971206572L;

	private JButton                 searchBack_btn;
	private JButton                 searchInit_btn;
	private JButton                 searchFore_btn;


	public TimelineFrame( final TraceFormatBufferedFileReader reader, final ModelTime modelTime )
	{		
		super(reader);
		super.init( modelTime );
		setTitle("TimeLine: " + reader.getCombinedProjectFilename());

		getFrame().setPreferredSize(new Dimension(1220, 700)); /* JK-SIZE */
	}

	@Override
	protected void addOwnPanelsOrToolbars(JPanel menuPanel) {
		// no own toolbars.
	}

	@Override
	protected void addToToolbarMenu(JToolBar toolbar, IconManager iconManager, Insets insets) {
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
	}

	@Override
	protected ModelInfoPanel<TraceObject> createModelInfoPanel() {
		return new TimelineTraceObjectInfoPanel(getReader());
	}

	@Override
	protected ScrollableObject createCanvasArea(ViewportTimeYaxis viewport) {
		return new CanvasTimeline(  getModelTime(), getReader(),  getYModel(), getTopologyManager(), viewport);
	}
}
