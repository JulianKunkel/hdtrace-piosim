
 /** Version Control Information $Id: ActionSearchForward.java 187 2009-04-05 12:36:44Z kunkel $
  * @lastmodified    $Date: 2009-04-05 14:36:44 +0200 (So, 05. Apr 2009) $
  * @modifiedby      $LastChangedBy: kunkel $
  * @version         $Revision: 187 $ 
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

package de.viewer.zoomable;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import de.viewer.common.Debug;


public class ActionSearchForward implements ActionListener
{
    private ViewportTimeYaxis  canvas_vport;

    public ActionSearchForward( ViewportTimeYaxis  in_vport )
    {
        canvas_vport  = in_vport;
    }

    public void actionPerformed( ActionEvent event )
    {
        canvas_vport.searchForward();

        if ( Debug.isActive() )
            Debug.println( "Action for Search Forward button. " );
    }
}
