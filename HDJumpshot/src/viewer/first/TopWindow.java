
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

import java.awt.Dimension;
import java.awt.Rectangle;

import javax.swing.JFrame;

import viewer.common.Parameters;
import viewer.common.Routines;
import viewer.common.TopControl;

public abstract class TopWindow
{
	public  static TopControl    Control      = null;
	private static Dimension     Screen_Size  = null;

	private JFrame  root;

	public TopWindow()
	{ root = null; }

	public void setWindow( JFrame in_root )
	{ root = in_root; }

	public JFrame getWindow()
	{ return root; }

	public void setVisible( boolean isVisible )
	{
		if ( root != null )
			root.setVisible( isVisible );
	}

	public void dispose()
	{
		if ( root != null ) {
			root.dispose();
			root = null;
		}
	}

	public abstract void disposeAll(); 

	public static void layoutIdealLocations()
	{
		if ( ! Parameters.AUTO_WINDOWS_LOCATION )
			return;

		if ( Screen_Size == null )
			Screen_Size = Routines.getScreenSize();

		JFrame control_frame    = First.getWindow();
		if ( Control == null && control_frame != null )
			Control = (TopControl) control_frame;

		Rectangle bounds = new Rectangle();
		JFrame legend_frame   = Legend.getWindow();
		if ( legend_frame != null ) {
			legend_frame.setLocation( bounds.getLocation() );
			bounds = legend_frame.getBounds();
		}
		JFrame first_frame    = First.getWindow();
		if ( first_frame != null ) {
			bounds.x += bounds.width;
			first_frame.setLocation( bounds.getLocation() );
			bounds = first_frame.getBounds();
		}
		JFrame timeline_frame = Timeline.getWindow();
		if ( timeline_frame != null ) {
			if ( first_frame != null )
				bounds.y += bounds.height;
			else
				bounds.x += bounds.width;
			timeline_frame.setLocation( bounds.getLocation() );
			bounds = timeline_frame.getBounds();
		}
		JFrame prefer_frame   = Preference.getWindow();
		if ( prefer_frame != null ) {
			bounds.x += bounds.width;
			if ( bounds.x > Screen_Size.width-20 )
				bounds.x = Screen_Size.width - prefer_frame.getWidth();
			prefer_frame.setLocation( bounds.getLocation() );
			if ( bounds.y + prefer_frame.getHeight() > Screen_Size.height )
				prefer_frame.setSize( prefer_frame.getWidth(),
						Screen_Size.height - bounds.y );
		}
	}



	public static TopWindow  Preference  = new TopWindow()
	{
		public void disposeAll()
		{
			// to invoke Control.setEditPreferenceButtonEnabled( true );
			setVisible( false );
			dispose();
		}
	};


	public static TopWindow  Timeline    = new TopWindow()
	{
		public void disposeAll()
		{
			// to invoke Control.setViewTimelineButtonEnabled( true );
			setVisible( false );
			dispose();
		}
	};


	public static TopWindow  Legend      = new TopWindow()
	{
		public void disposeAll()
		{
			// to invoke Control.setViewLegendButtonEnabled( true );
			setVisible( false );
			Timeline.disposeAll();
			dispose();
		}
	};


	public static TopWindow  First       = new TopWindow()
	{
		public void disposeAll()
		{
			Legend.disposeAll();
			Preference.disposeAll();
			dispose();
			System.exit( 0 );
		}
	};
}
