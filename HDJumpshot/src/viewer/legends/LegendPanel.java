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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.Border;

import viewer.common.TopWindow;


public class LegendPanel extends JPanel
implements ActionListener
{
	private LegendTable  legend_table;

	private JButton      all_btn;
	private JButton      clear_btn;
	private JButton      close_btn;

	public LegendPanel( final TraceFormatBufferedFileReader  reader )
	{
		super();
		super.setLayout( new BoxLayout( this, BoxLayout.Y_AXIS ) );

		Border  lowered_border, empty_border, etched_border;
		lowered_border = BorderFactory.createLoweredBevelBorder();
		empty_border   = BorderFactory.createEmptyBorder( 4, 4, 4 ,4 );
		etched_border  = BorderFactory.createEtchedBorder();

		legend_table  = new LegendTable(reader);
		JScrollPane scroller = new JScrollPane( legend_table,
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS );
		scroller.setBorder( BorderFactory.createCompoundBorder(
				lowered_border, BorderFactory.createCompoundBorder(
						empty_border, etched_border ) ) );
		super.add( scroller );

		Border titled_border;

		JPanel  select_panel = new JPanel();
		select_panel.setLayout( new BoxLayout( select_panel,
				BoxLayout.X_AXIS ) );
		select_panel.add( Box.createHorizontalGlue() );

		all_btn = new JButton( "Select" );
		all_btn.setToolTipText( "Select all Legends" );
		// all_btn.setAlignmentX( Component.CENTER_ALIGNMENT );
		all_btn.addActionListener( this );
		select_panel.add( all_btn );

		select_panel.add( Box.createHorizontalGlue() );

		clear_btn = new JButton( "Deselect" );
		clear_btn.setToolTipText( "Deselect all Legends" );
		// clear_btn.setAlignmentX( Component.CENTER_ALIGNMENT );
		clear_btn.addActionListener( this );
		select_panel.add( clear_btn );

		select_panel.add( Box.createHorizontalGlue() );
		titled_border = BorderFactory.createTitledBorder(
				etched_border, " All " );
		select_panel.setBorder( titled_border );
		super.add( select_panel );

		JPanel  end_panel = new JPanel();
		end_panel.setLayout( new BoxLayout( end_panel, BoxLayout.X_AXIS ) );
		end_panel.add( Box.createHorizontalGlue() );

		close_btn = new JButton( "close" );
		close_btn.setToolTipText( "Hide this panel" );
		// close_btn.setAlignmentX( Component.CENTER_ALIGNMENT );
		close_btn.addActionListener( this );
		end_panel.add( close_btn );

		end_panel.add( Box.createHorizontalGlue() );
		super.add( end_panel );
	}

	public void actionPerformed( ActionEvent evt )
	{
		Object evt_src = evt.getSource();

		if ( evt_src == close_btn )
			TopWindow.Legend.setVisible( false );
		else if ( evt_src == all_btn )
			legend_table.selectAll();
		else if ( evt_src == clear_btn )
			legend_table.clearSelection();
	}
}
