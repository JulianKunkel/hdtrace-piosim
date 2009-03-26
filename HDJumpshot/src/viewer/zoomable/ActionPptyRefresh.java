/*
 *  (C) 2001 by Argonne National Laboratory
 *      See COPYRIGHT in top-level directory.
 */

/*
 *  @author  Anthony Chan
 */

package viewer.zoomable;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import topology.TopologyManager;
import viewer.common.Parameters;
import viewer.common.PreferenceFrame;
import viewer.common.TopWindow;
import viewer.timelines.CanvasTimeline;

public class ActionPptyRefresh implements ActionListener
{
    private TopologyManager        y_tree;

    private CanvasTimeline      timelines;
    private PreferenceFrame  	   pptys_frame;

    public ActionPptyRefresh( TopologyManager in_y_tree, CanvasTimeline timelines)
    {
        y_tree             = in_y_tree;
        this.timelines = timelines;
    }

    public void actionPerformed( ActionEvent event )
    {
        pptys_frame = (PreferenceFrame) TopWindow.Preference.getWindow();
        if ( pptys_frame != null )
            pptys_frame.updateAllParametersFromFields();
                
        Parameters.initStaticClasses();
                
        y_tree.setRootVisible( Parameters.Y_AXIS_ROOT_VISIBLE );
        
        timelines.forceRedraw();
    }
}
