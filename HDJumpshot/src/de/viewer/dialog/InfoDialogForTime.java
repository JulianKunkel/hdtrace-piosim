
/** Version Control Information $Id: InfoDialogForTime.java 261 2009-05-02 11:39:11Z kunkel $
 * @lastmodified    $Date: 2009-05-02 13:39:11 +0200 (Sa, 02. Mai 2009) $
 * @modifiedby      $LastChangedBy: kunkel $
 * @version         $Revision: 261 $ 
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

package de.viewer.dialog;

import java.awt.Component;
import java.awt.Container;
import java.awt.Frame;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import javax.swing.BoxLayout;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import de.hd.pvs.TraceFormat.util.Epoch;
import de.viewer.common.Const;
import de.viewer.common.Routines;

public class InfoDialogForTime extends InfoDialog
{
	private static final String         FORMAT = Const.INFOBOX_TIME_FORMAT;
	private static       DecimalFormat  fmt = null;

	public InfoDialogForTime( final Frame            frame,
			final Epoch           time,
			final Epoch realModelTimeStart)

	{
		super( frame, "Time Info Box", time, realModelTimeStart );
		this.init();
	}

	private void init()
	{
		/* Define DecialFormat for the displayed time */
		if ( fmt == null ) {
			fmt = (DecimalFormat) NumberFormat.getInstance();
			fmt.applyPattern( FORMAT );
		}

		Container root_panel = this.getContentPane();
		root_panel.setLayout( new BoxLayout( root_panel, BoxLayout.Y_AXIS ) );

		StringBuffer textbuf = new StringBuffer();
		int          num_cols = 0, num_rows = 1;

		StringBuffer linebuf = new StringBuffer();
		linebuf.append( "time = " + fmt.format(getClickedTime().getDouble()) );
		num_cols = linebuf.length();
		textbuf.append( linebuf.toString() );

		JTextArea text_area = new JTextArea( textbuf.toString() );
		int adj_num_cols    = Routines.getAdjNumOfTextColumns( text_area, num_cols );
		text_area.setColumns( adj_num_cols );
		text_area.setRows( num_rows );
		text_area.setEditable( false );
		text_area.setLineWrap( true );
		JScrollPane scroller = new JScrollPane( text_area );
		scroller.setAlignmentX( Component.CENTER_ALIGNMENT );
		root_panel.add( scroller );

		root_panel.add( super.getCloseButtonPanel() );
	}
}
