
 /** Version Control Information $Id: ActionTimelineRestore.java 149 2009-03-27 13:55:56Z kunkel $
  * @lastmodified    $Date: 2009-03-27 14:55:56 +0100 (Fr, 27. Mär 2009) $
  * @modifiedby      $LastChangedBy: kunkel $
  * @version         $Revision: 149 $ 
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
 *  (C) 2007 by Julian Kunkel
 *      See COPYRIGHT in top-level directory.
 */

/*
 *  @author  Julian Kunkel
 */

package de.viewer.zoomable;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import de.topology.TopologyManager;


public class ActionTimelineRestore implements ActionListener
{
	final TopologyManager topologyManager;
	
    public ActionTimelineRestore( TopologyManager        in_tree )
    {
        topologyManager         = in_tree;

    }

    public void actionPerformed( ActionEvent event )
    {
    	topologyManager.restoreTopology();   
    }
}
