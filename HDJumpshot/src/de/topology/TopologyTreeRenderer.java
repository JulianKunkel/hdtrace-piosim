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

package de.topology;

import java.awt.Component;

import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

import de.viewer.common.IconManager;
import de.viewer.common.IconManager.IconType;


/**
 * Set icons depending on topology type.
 * @author Julian M. Kunkel
 */
public class TopologyTreeRenderer extends DefaultTreeCellRenderer {
	private static final long serialVersionUID = 4780540256182237691L;

	final Icon topologyTraceIcon;
	final Icon topologyStatisticIcon;
	
	public TopologyTreeRenderer(IconManager manager) {
		topologyTraceIcon = manager.getOriginalImage(IconType.TopologyTrace);
		topologyStatisticIcon = manager.getOriginalImage(IconType.TopologyStatistic);
  }

  public Component getTreeCellRendererComponent(
                      JTree tree,
                      Object value,
                      boolean sel,
                      boolean expanded,
                      boolean leaf,
                      int row,
                      boolean hasFocus) 
  {
      super.getTreeCellRendererComponent(
                      tree, value, sel,
                      expanded, leaf, row,
                      hasFocus);
      
      if(TopologyTreeNode.class.isInstance(value)){
      	final TopologyTreeNode node = (TopologyTreeNode) value;
      	
      	switch(node.getType()){
      	case INNER_NODE:
      		break;
      	case STATISTIC:
          setIcon(topologyStatisticIcon);
          
      		break;
      	case TRACE:
          setIcon(topologyTraceIcon);
          
      		break;
      	}
      }
      
      return this;
  }
}
