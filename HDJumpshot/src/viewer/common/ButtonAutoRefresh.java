
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
 *  @author Julian M. Kunkel
 */

package viewer.common;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;

import viewer.common.IconManager.IconType;
import viewer.first.MainManager;

/**
 * This button is for triggering auto refresh in any form, it shows its state.
 * @author julian
 *
 */
public class ButtonAutoRefresh extends JButton implements ActionListener
{
	private static final long serialVersionUID = -2980493349149271927L;

	final private IAutoRefreshable     autoRefreshable;

	public ButtonAutoRefresh(IAutoRefreshable autoRefreshable)
	{
		super(MainManager.getIconManager().getActiveToolbarIcon(IconType.AutoRefresh));
		this.setToolTipText( "Automatically redraw" );
		this.setMnemonic( KeyEvent.VK_A );

		this.autoRefreshable = autoRefreshable;
		this.addActionListener(this);
		
		setBorder();
	}

	private void setBorder(){
		if(autoRefreshable.isAutoRefresh()){
			setBorder(BorderFactory.createLoweredBevelBorder());
			setBackground(Color.GREEN);
		}else{
			setBorder(BorderFactory.createRaisedBevelBorder());
			setBackground(Color.LIGHT_GRAY);
		}
	}

	public void actionPerformed( ActionEvent event )
	{
		autoRefreshable.setAutoRefresh(! autoRefreshable.isAutoRefresh());    		    		    		
	
		setBorder();
	}
	
	public boolean isAutoRefresh(){
		return autoRefreshable.isAutoRefresh();
	}
}
