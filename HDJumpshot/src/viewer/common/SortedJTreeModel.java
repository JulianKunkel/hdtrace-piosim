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

package viewer.common;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;

public class SortedJTreeModel extends DefaultTreeModel {
	private static final long serialVersionUID = -4571118169344183171L;

	public SortedJTreeModel(TreeNode root) {
		super(root);
	}

	/**
	 * Insert a node at the right position.
	 * @param newChild
	 * @param parent
	 */
	public void insertNodeInto(MutableTreeNode newChild, MutableTreeNode parent) {
		// find correct position with bin search:
		int min = 0;
		int max = parent.getChildCount() - 1;

		if(max == -1){
			super.insertNodeInto(newChild, parent, 0);
			return;
		}

		while(true){
			int cur = (min + max) / 2;
			TreeNode entry = parent.getChildAt(cur);


			if(min == max){ // found entry or stopped.
				if ( entry.toString().compareTo(newChild.toString()) >= 0){
					super.insertNodeInto(newChild, parent, cur);
				}else{
					super.insertNodeInto(newChild, parent, cur + 1);
				}

				return;
			} 

			// not found => continue bin search:			
			if ( entry.toString().compareTo(newChild.toString()) >= 0){
				max = cur;
			}else{
				min = cur + 1;
			}
		}		
	}



	@Override
	public void insertNodeInto(MutableTreeNode newChild, MutableTreeNode parent, int index) {
		insertNodeInto(newChild, parent);
	}
}
