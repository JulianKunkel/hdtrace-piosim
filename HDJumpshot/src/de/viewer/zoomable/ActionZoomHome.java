
 /** Version Control Information $Id: ActionZoomHome.java 204 2009-04-11 13:36:48Z kunkel $
  * @lastmodified    $Date: 2009-04-11 15:36:48 +0200 (Sa, 11. Apr 2009) $
  * @modifiedby      $LastChangedBy: kunkel $
  * @version         $Revision: 204 $ 
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

import java.awt.*;
import java.awt.event.*;
import java.net.*;
import javax.swing.*;

import de.viewer.common.Const;
import de.viewer.common.Debug;
import de.viewer.common.Dialogs;
import de.viewer.common.ModelTime;


public class ActionZoomHome implements ActionListener
{
    private ToolBarStatus      toolbar;
    private ModelTime          model;

    public ActionZoomHome( ToolBarStatus in_toolbar, ModelTime in_model )
    {
        toolbar    = in_toolbar;
        model      = in_model;
    }

    public void actionPerformed( ActionEvent event )
    {
        model.zoomHome();

        if ( Debug.isActive() )
            Debug.println( "Action for Zoom Home button" );
    }
}
