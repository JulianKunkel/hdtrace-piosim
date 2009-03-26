/*
 *  (C) 2001 by Argonne National Laboratory
 *      See COPYRIGHT in top-level directory.
 */

/*
 *  @author  Anthony Chan
 */

package viewer.zoomable;

import java.awt.*;
import java.awt.event.*;
import java.net.*;
import javax.swing.*;

import viewer.common.Const;
import viewer.common.Debug;
import viewer.common.Dialogs;

public class ActionZoomOut implements ActionListener
{
    private ToolBarStatus      toolbar;
    private ModelTime          model;

    public ActionZoomOut( ToolBarStatus in_toolbar, ModelTime in_model )
    {
        toolbar    = in_toolbar;
        model      = in_model;
    }

    public void actionPerformed( ActionEvent event )
    {
        Window  window;
        String  msg;

        double zoomlevel = model.getZoomFaktor();
        if ( zoomlevel < Const.MIN_ZOOM_FAKTOR ) {
            window = SwingUtilities.windowForComponent( (JToolBar) toolbar );
            msg    = "The Current ZoomLevel(" + zoomlevel + ") is below "
                   + "the Minimum ZoomLevel(" + Const.MIN_ZOOM_FAKTOR + ")!";
            Dialogs.warn( window, msg );
        }
        else
            model.zoomOut();

        if ( Debug.isActive() )
            Debug.println( "Action for Zoom Out button. ZoomLevel = "
                         + zoomlevel );
    }
}
