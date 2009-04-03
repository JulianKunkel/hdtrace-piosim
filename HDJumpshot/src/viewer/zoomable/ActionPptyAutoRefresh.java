
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

package viewer.zoomable;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;

import viewer.timelines.CanvasTimeline;

public class ActionPptyAutoRefresh implements ActionListener
{

    final private CanvasTimeline     timelines;
    final private JButton			 refresh_btn;

    public ActionPptyAutoRefresh(CanvasTimeline timelines, JButton refresh_btn)
    {
        this.timelines = timelines;
        this.refresh_btn = refresh_btn;
        
        setBorder();
    }
    
    private void setBorder(){
    	if(timelines.isAutoRefresh()){
    		refresh_btn.setBorder(BorderFactory.createLoweredBevelBorder());
    		refresh_btn.setBackground(Color.GREEN);
    	}else{
    		refresh_btn.setBorder(BorderFactory.createRaisedBevelBorder());
    		refresh_btn.setBackground(Color.GRAY);
    	}
    }

    public void actionPerformed( ActionEvent event )
    {
    	if(timelines.isAutoRefresh()){
    		timelines.setAutoRefresh(false);    		    		    		
    	}else{    		
    		timelines.setAutoRefresh(true);
    		//timelines.forceRedraw();
    	}
    	
    	setBorder();
    }
}
