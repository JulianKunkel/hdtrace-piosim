/*
 *  (C) 2007 by Julian Kunkel
 *      See COPYRIGHT in top-level directory.
 */

/*
 *  @author  Julian Kunkel
 */

package viewer.zoomable;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import viewer.topology.TopologyManager;

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
