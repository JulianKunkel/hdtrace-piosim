package topology.mappings;

import hdTraceInput.TraceFormatBufferedFileReader;

import java.util.ArrayList;
import java.util.LinkedList;

import topology.TopologyInnerNode;
import topology.TopologyTraceTreeNode;
import topology.TopologyTreeNode;
import viewer.common.SortedJTreeNode;
import de.hd.pvs.TraceFormat.TraceFormatFileOpener;
import de.hd.pvs.TraceFormat.topology.TopologyNode;

/**
 * Shows ranks first, then threads, levels above are removed.
 * 
 * @author julian
 */
public class RankTopologyMapping extends DefaultTopologyTreeMapping{
	
	static final String RANKTOPO="rank";

	@Override
	public SortedJTreeNode createTopology(TraceFormatBufferedFileReader reader) {
		final SortedJTreeNode treeRoot = new SortedJTreeNode("HDTrace");		

		outer: for(int f = 0 ; f < reader.getNumberOfFilesLoaded() ; f++){
			final TraceFormatFileOpener file = reader.getLoadedFile(f);
			final TopologyNode topoRoot = file.getTopology();
			final ArrayList<String> list = file.getTopologyLabels().getLabels();
			
			for(int i=0 ; i < list.size() ; i++){
				if(list.get(i).compareToIgnoreCase(RANKTOPO) == 0){
					
					final TopologyTreeNode fileNode = new TopologyInnerNode(file.getTopology(), file);
					addTopologyTreeNode(fileNode, treeRoot);    	

					if(file.getTopology().getTraceSource() != null){
						TopologyTreeNode childNode = new TopologyTraceTreeNode("Trace", file.getTopology(), file);
						addTopologyTreeNode(childNode, fileNode);			
					}
					
					loadRankTopology(i, fileNode, file);
					continue outer;
				}
			}
			
			recursivlyAddTopology(1, treeRoot, topoRoot, file);
		}

		return treeRoot;	
	}
	
	private void loadRankTopology(int rankPos, SortedJTreeNode parent, TraceFormatFileOpener file)
	{
		// bfs:
		final LinkedList<TopologyNode> rankTopos = file.getTopology().getChildrenOfDepth(rankPos);
		for(TopologyNode topo: rankTopos){
			recursivlyAddTopology(1, parent, topo, file);
		}
	}

	@Override
	public boolean isAvailable(TraceFormatBufferedFileReader reader) {
		// check labels:
		for(int f = 0 ; f < reader.getNumberOfFilesLoaded(); f++){
			final TraceFormatFileOpener file=reader.getLoadedFile(f);
			for(String label: file.getTopologyLabels().getLabels()){
				if(label.compareToIgnoreCase(RANKTOPO) == 0){
					return true;
				}
			}
		}
		return false;
	}
}
