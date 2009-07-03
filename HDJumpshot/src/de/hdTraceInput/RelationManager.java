package de.hdTraceInput;

import java.util.HashMap;

import de.hd.pvs.TraceFormat.relation.RelationEntry;
import de.hd.pvs.TraceFormat.relation.RelationHeader;
import de.topology.TopologyRelationExpandedTreeNode;
import de.topology.TopologyRelationTreeNode;

/**
 * Manages known relations across files.
 * This allows to link multiple files together.
 * 
 * @author julian
 *
 */
public class RelationManager {
	
	private final class MappedRelation{
		final TopologyRelationExpandedTreeNode treeNode;
		final RelationEntry entry;
		
		public MappedRelation(TopologyRelationExpandedTreeNode treeNode, RelationEntry entry) {
			this.treeNode = treeNode;
			this.entry = entry;
		}
	}
	
	/**
	 * Contains one relation file and the split timelines.
	 * @author julian
	 */
	private static class ManagedTopologyFile{
		final TopologyRelationTreeNode topologyTreeNode;
		
		/**
		 * Map the tokenID to the timeline => relationEntry
		 */
		final HashMap<Long, MappedRelation> entryMap = new HashMap<Long, MappedRelation>();
		
		
		public ManagedTopologyFile(TopologyRelationTreeNode topoNode) {	
			this.topologyTreeNode = topoNode;
		}
	}
	
	public static class RelationSearchResult{
		final TopologyRelationTreeNode topologyRelationTreeNode;
		final TopologyRelationExpandedTreeNode topologyRelationExpandedTreeNode;
		
		final RelationEntry entry;
		
		public RelationSearchResult(TopologyRelationTreeNode topologyRelationTreeNode,  
				TopologyRelationExpandedTreeNode topologyRelationExpandedTreeNode, 
				RelationEntry entry) {
			this.topologyRelationExpandedTreeNode = topologyRelationExpandedTreeNode;
			this.topologyRelationTreeNode = topologyRelationTreeNode;
			this.entry = entry;
		}
		
		public RelationEntry getEntry() {
			return entry;
		}
		
		public TopologyRelationExpandedTreeNode getTopologyRelationExpandedTreeNode() {
			return topologyRelationExpandedTreeNode;
		}
		
		public TopologyRelationTreeNode getTopologyRelationTreeNode() {
			return topologyRelationTreeNode;
		}
	}
	
	/**
	 * map hostID to a local map to map to the topology and finally to the relation.
	 * (hostID, processID, topoID (int)), tokenID (long)
	 */
	final HashMap<String, ManagedTopologyFile> map = new HashMap<String, ManagedTopologyFile>();
	
	/**
	 * Use this function together with <code>addTopologyTreeNode</code>
	 * @param reader
	 */
	public void addFile(BufferedRelationReader reader, TopologyRelationTreeNode topoNode){
		final RelationHeader header = reader.getHeader();
		
		if(map.containsKey(header.getUniqueID())){
			throw new IllegalArgumentException("File with the same \"unique\" id already added: " + header.getUniqueID());
		}
		
		map.put(header.getUniqueID(), new ManagedTopologyFile(topoNode));
	}
	
	/**
	 * Add a topologyTreeNode to the appropriate mapping.
	 * @param filename
	 */
	public void addTopologyTreeNode(TopologyRelationExpandedTreeNode topoNode){					
		final ManagedTopologyFile managedFile = map.get(topoNode.getRelationSource().getHeader().getUniqueID());
		if(managedFile == null){
			throw new IllegalArgumentException("File not registered, yet => Call addfile first!");
		}
		
		// now add all entries to the map.
		for( RelationEntry entry : topoNode.getEntries()){			
			managedFile.entryMap.put(entry.getTokenID(), new MappedRelation(topoNode, entry));
		}		
	}
		
	/**
	 * Remove a file from the manager.
	 * @param header
	 */
	public void removeFile(RelationHeader header){
		if( map.remove(header.getUniqueID()) == null){
			throw new IllegalArgumentException("File with unique id " + header.getUniqueID() + " not loaded");
		}
	}
	
	/**
	 * Find the parent relation entry for a given entry.
	 * @return parent || null if not found (i.e. file not loaded, yet)
	 */
	public RelationSearchResult getParentRelationEntry(RelationEntry entry){
		assert(entry != null);
		
		final String [] ids = entry.getRelatedIDPerLevel();
		if(ids == null){
			// has no parent.
			throw new IllegalArgumentException("The given entry has no parent!");
		}

		final RelationHeader header = entry.getHeader(); 
		// create the unique id to identify the relation file
		String uniqueID;			
		
		// depending on the given id's 		
		switch(ids.length){
		case 4: // host
			uniqueID = ids[0] + ":" + ids[1] + ":" + ids[2];
			break;
		case 3: // local
			uniqueID = header.getHostID() + ":" + ids[0] + ":" + ids[1] ;
			break;
		case 2: // process
			uniqueID = header.getHostID() + ":" + header.getLocalToken() + ":" +  ids[0];
			break;
		case 1: // internal
			uniqueID = header.getUniqueID();
			break;
		case 0:
			throw new IllegalArgumentException("The given entry has no parent!");
		default:
			throw new IllegalArgumentException("Invalid state!");
		}
		
		//System.out.println("SEARCH " + uniqueID +" " + ids.length);
		
		final ManagedTopologyFile topoRelation = map.get(uniqueID);
		if(topoRelation == null){
			// file not loaded yet.
			return null;
		}

		final MappedRelation result =  topoRelation.entryMap.get(Long.parseLong(ids[ids.length-1]));
		if(result == null){
			return null;
		}
		
		return new RelationSearchResult(topoRelation.topologyTreeNode, result.treeNode, result.entry);		
	}
}
