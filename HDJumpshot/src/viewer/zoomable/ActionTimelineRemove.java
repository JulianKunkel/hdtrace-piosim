
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



package viewer.zoomable;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import topology.TopologyManager;

public class ActionTimelineRemove implements ActionListener
{
    private TopologyManager         tree;

    public ActionTimelineRemove( TopologyManager in_tree )
    {
        tree = in_tree;
    }

    public void actionPerformed( ActionEvent event )
    {
    	tree.removeMarkedTimelines();
    }
}
