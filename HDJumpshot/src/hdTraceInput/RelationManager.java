package hdTraceInput;

import java.util.ArrayList;
import java.util.HashMap;

import topology.TopologyTreeNode;
import de.hd.pvs.TraceFormat.relation.RelationEntry;
import de.hd.pvs.TraceFormat.relation.RelationHeader;

/**
 * Manages known relations across files.
 * This allows to link multiple files together.
 * 
 * @author julian
 *
 */
public class RelationManager {
	
	private static class ManagedTopologyRelation{
		final RelationHeader header;
		final ArrayList<RelationEntry> entriesTimeSorted;
		/**
		 * Map the tokenID to the relationEntry
		 */
		final HashMap<Long, RelationEntry> entryMap;
		
		final TopologyTreeNode topologyTreeNode;
		
		public ManagedTopologyRelation(ArrayList<RelationEntry> entriesTimeSorted, 
				HashMap<Long, RelationEntry> entryMap, TopologyTreeNode topoNode) {
			assert(entriesTimeSorted.size() > 0);
			header = entriesTimeSorted.get(0).getHeader();
			this.entriesTimeSorted = entriesTimeSorted;
			this.entryMap = entryMap;			
			this.topologyTreeNode = topoNode;
		}		
	}
	
	public static class RelationSearchResult{
		final TopologyTreeNode topologyTreeNode;
		final RelationEntry entry;
		
		public RelationSearchResult(TopologyTreeNode topoNode, RelationEntry entry) {
			this.topologyTreeNode = topoNode;
			this.entry = entry;
		}
		
		public RelationEntry getEntry() {
			return entry;
		}
		
		public TopologyTreeNode getTopologyTreeNode() {
			return topologyTreeNode;
		}
	}
	
	/**
	 * map hostID to a local map to map to the topology and finally to the relation.
	 * (hostID, processID, topoID (int)), tokenID (long)
	 */
	final HashMap<String, ManagedTopologyRelation> map = new HashMap<String, ManagedTopologyRelation>();
		
	/**
	 * Add a file to the appropriate mapping.
	 * @param filename
	 */
	public void addFile(BufferedRelationReader reader, TopologyTreeNode topoNode){		
		final RelationHeader header = reader.getHeader();
		
		if(map.containsKey(header.getUniqueID())){
			throw new IllegalArgumentException("File with the same \"unique\" id already added: " + header.getUniqueID());
		}
			
		final HashMap<Long, RelationEntry> entryMap = new HashMap<Long, RelationEntry>();
		
		// now add all entries to the map.
		for( RelationEntry entry : reader.getEntries()){			
			entryMap.put(entry.getTokenID(), entry);
		}
		
		final ManagedTopologyRelation topoRelation = new ManagedTopologyRelation(reader.getEntries(), entryMap, topoNode);		
		map.put(header.getUniqueID(), topoRelation);
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
		
		final ManagedTopologyRelation topoRelation = map.get(uniqueID);
		if(topoRelation == null){
			// file not loaded yet.
			return null;
		}

		return new RelationSearchResult(topoRelation.topologyTreeNode, topoRelation.entryMap.get(Long.parseLong(ids[ids.length-1])));		
	}
}
