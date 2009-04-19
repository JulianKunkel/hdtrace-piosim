package viewer.common;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;

/**
 * Sorts the node children alphabetically
 * 
 * @author julian
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

			
			if(min == max){ // found entry or stopped.
				if ( entry.toString().compareTo(newChild.toString()) >= 0){
					super.insert(newChild, cur);
				}else{
					super.insert(newChild, cur +1);
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
}
