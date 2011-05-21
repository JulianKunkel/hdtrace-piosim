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

package de.viewer.common;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;

/**
 * Sorts the node children alphabetically, use it together with SortedJTreeModel
 * 
 * @author Julian M. Kunkel
 */
public class SortedJTreeNode extends DefaultMutableTreeNode{
	private static final long serialVersionUID = 1L;

	public SortedJTreeNode(Object obj) {
		super(obj);
	}
	
	public SortedJTreeNode() {
		
	}
	
	@Override
	public void insert(MutableTreeNode newChild, int childIndex) {
		// find correct position with bin search:
		int min = 0;
		int max = getChildCount() - 1;
		
		if(max == -1){
			super.insert(newChild, 0);
			return;
		}

		while(true){
			int cur = (min + max) / 2;
			TreeNode entry = getChildAt(cur);
			
			// length lexicographical order, i.e. shorter string first.
			
			String newChildStr = newChild.toString();
			String entryStr = entry.toString();
			
			if(min == max){ // found entry or stopped.
				// check length first
				if ( entryStr.length() > newChildStr.length()){
					super.insert(newChild, cur);
				}else if(entryStr.length() < newChildStr.length()){
					super.insert(newChild, cur +1);
				}else{
					// same length
					if(entryStr.compareTo(newChildStr) >= 0){
						super.insert(newChild, cur);
					}else{
						super.insert(newChild, cur +1);	
					}
				}
				
				return;
			} 
			
			// not found => continue bin search:								
			if ( entryStr.length() > newChildStr.length()){
				max = cur;
			}else if(entryStr.length() < newChildStr.length()){
				min = cur + 1;
			}else{
				if ( entryStr.compareTo(newChildStr) >= 0 ){
					max = cur;
				}else{
					min = cur + 1;
				}
			}
			
			
		}
	}
}
