
/** Version Control Information $Id: PreferenceFrame.java 469 2009-07-01 13:27:24Z kunkel $
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

package de.viewer.common;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import de.viewer.first.TopWindow;


public class PreferenceFrame extends TopWindow
implements ActionListener
{
	private static final long serialVersionUID = -5140472254843946440L;

	private PreferencePanel  pptys_panel;

	private JButton          update_btn;
	private JButton          save_btn;
	private JButton          close_btn;
	
	public PreferenceFrame()
	{
		setTitle( "Preferences" );

		Container root_panel = getFrame().getContentPane();
		root_panel.setLayout( new BoxLayout( root_panel, BoxLayout.Y_AXIS ) );

		JScrollPane  scroller;
		pptys_panel = new PreferencePanel();
		pptys_panel.updateAllFieldsFromParameters();
		scroller   = new JScrollPane( pptys_panel );
		Dimension screen_size = Routines.getScreenSize();
		scroller.setMinimumSize(new Dimension( 100, 100 ) );
		scroller.setMaximumSize(new Dimension( screen_size.width / 2,
						screen_size.height * 4/5 ) );
		scroller.setPreferredSize(	new Dimension( pptys_panel.getPreferredSize().width * 10/9,
						screen_size.height * 3/5 ) );
		root_panel.add( scroller );

		JPanel mid_panel = new JPanel();
		mid_panel.setLayout( new BoxLayout( mid_panel, BoxLayout.X_AXIS ) );
		mid_panel.add( Box.createHorizontalGlue() );

		update_btn = new JButton( "update" );
		update_btn.setToolTipText(
				"Update all parameters based on the current preference" );
		// update_btn.setAlignmentX( Component.CENTER_ALIGNMENT );
		update_btn.addActionListener( this );
		mid_panel.add( update_btn );

		mid_panel.add( Box.createHorizontalGlue() );

		save_btn = new JButton( "save" );
		save_btn.setToolTipText(
		"Save preference to Jumpshot-4 setup file" );
		// save_btn.setAlignmentX( Component.CENTER_ALIGNMENT );
		save_btn.addActionListener( this );
		mid_panel.add( save_btn );

		mid_panel.add( Box.createHorizontalGlue() );
		root_panel.add( mid_panel );

		JPanel end_panel = new JPanel();
		end_panel.setLayout( new BoxLayout( end_panel, BoxLayout.X_AXIS ) );
		end_panel.add( Box.createHorizontalGlue() );

		close_btn = new JButton( "close" );
		close_btn.setToolTipText( "Close this window" );
		// close_btn.setAlignmentY( Component.CENTER_ALIGNMENT );
		close_btn.addActionListener( this );
		end_panel.add( close_btn );

		end_panel.add( Box.createHorizontalGlue() );
		root_panel.add( end_panel );
	}

	public void updateAllParametersFromFields()
	{
		pptys_panel.updateAllParametersFromFields();
	}

	public void updateAllFieldsFromParameters()
	{
		pptys_panel.updateAllFieldsFromParameters();
	}


	public void actionPerformed( ActionEvent evt )
	{
		if ( evt.getSource() == this.update_btn ) {
			pptys_panel.updateAllParametersFromFields();
		}
		else if ( evt.getSource() == this.save_btn ) {
			pptys_panel.updateAllParametersFromFields();
			Parameters.writeToSetupFile( getFrame() );
		}
		else if ( evt.getSource() == this.close_btn ) {
			PreferenceFrame.this.setVisible( false );
		}
	}
	
	@Override
	protected void destroyWindow() {
		// TODO Auto-generated method stub
		
	}
}
