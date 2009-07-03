
 /** Version Control Information $Id: ActionZoomRedo.java 205 2009-04-11 15:33:40Z kunkel $
  * @lastmodified    $Date: 2009-04-11 17:33:40 +0200 (Sa, 11. Apr 2009) $
  * @modifiedby      $LastChangedBy: kunkel $
  * @version         $Revision: 205 $ 
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

import de.drawable.TimeBoundingBox;
import de.viewer.common.Const;
import de.viewer.common.Debug;
import de.viewer.common.Dialogs;
import de.viewer.common.ModelTime;



public class ActionZoomRedo implements ActionListener
{
    private ToolBarStatus      toolbar;
    private ModelTime          model;

    public ActionZoomRedo( ToolBarStatus in_toolbar, ModelTime in_model )
    {
        toolbar    = in_toolbar;
        model      = in_model;
    }

    public void actionPerformed( ActionEvent event )
    {
        Window  window;
        String  msg;

        if ( model.isZoomRedoStackEmpty() ) {
            window  = SwingUtilities.windowForComponent( (JToolBar) toolbar );
            msg     = "Zoom Redo Stack is empty";
            Dialogs.warn( window, msg );
        }
        else
            model.zoomRedo();

        if ( Debug.isActive() )
            Debug.println( "Action for Zoom Redo button." );
    }
}
