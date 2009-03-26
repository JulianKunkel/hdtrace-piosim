
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
