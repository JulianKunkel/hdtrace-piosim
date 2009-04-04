
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

package viewer.first;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.border.EtchedBorder;


public class FirstMenuBar extends JMenuBar
{
	private static final long serialVersionUID = -7909027107571610938L;
	
	private        boolean         isApplet;
	private        FirstPanel      first_panel;

	private        JMenuItem       file_select_item;
	private        JMenuItem       file_close_item;
	private        JMenuItem       file_exit_item;
	private        JMenuItem       show_legend_item;
	private        JMenuItem       show_timeline_item;
	private        JMenuItem       edit_prefer_item;
	private        JMenuItem       help_manual_item;
	private        JMenuItem       help_about_item;

	public FirstMenuBar( boolean isTopApplet, FirstPanel in_panel )
	{
		super();
		super.setBorder( new EtchedBorder() );

		isApplet       = isTopApplet;
		first_panel    = in_panel;

		JMenu      menu, submenu;
		menu = new JMenu( "File" );
		file_select_item = new JMenuItem( "Open Project File" );
		file_select_item.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent evt ) {
				first_panel.getLogFileSelectButton().doClick();
			}
		} );
		menu.add( file_select_item );

		final JMenuItem file_add_project_item = new JMenuItem( "Add Project File*" );
		file_add_project_item.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent evt ) {
				first_panel.getFileaddButton().doClick();
			}
		} );
		menu.add( file_add_project_item );
		
		file_close_item  = new JMenuItem( "Close" );
		file_close_item.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent evt ) {
				first_panel.getLogFileCloseButton().doClick();
			}
		} );
		menu.add( file_close_item );
		menu.addSeparator();
		file_exit_item   = new JMenuItem( "Exit" );
		file_exit_item.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent evt ) {
				if ( isApplet )
					TopWindow.Legend.disposeAll();
				else
					TopWindow.First.disposeAll();
			}
		} );
		menu.add( file_exit_item );
		super.add( menu );

		menu = new JMenu( "Edit" );
		edit_prefer_item = new JMenuItem( "Preferences ..." );
		edit_prefer_item.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent evt ) {
				first_panel.getEditPreferenceButton().doClick();
			}
		} );
		menu.add( edit_prefer_item );

		super.add( menu );

		menu = new JMenu( "View" );
		submenu = new JMenu( "Reload" );
		show_legend_item = new JMenuItem( "Legend window" );
		show_legend_item.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent evt ) {
				first_panel.getShowLegendButton().doClick();
			}
		} );
		submenu.add( show_legend_item );
		show_timeline_item = new JMenuItem( "Timeline window" );
		show_timeline_item.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent evt ) {
				first_panel.getShowTimelineButton().doClick();
			}
		} );
		submenu.add( show_timeline_item );
		menu.add( submenu );
		super.add( menu );

		menu = new JMenu( "Help" );
		help_manual_item = new JMenuItem( "Manual" );
		help_manual_item.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent evt ) {
				first_panel.getHelpManualButton().doClick();
			}
		} );
		menu.add( help_manual_item );

		help_about_item = new JMenuItem( "About" );
		help_about_item.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent evt ) {
				first_panel.getHelpAboutButton().doClick();
			}
		} );
		menu.add( help_about_item );
		super.add( menu );
		// super.setAlignmentX( Component.LEFT_ALIGNMENT );
	}
}
