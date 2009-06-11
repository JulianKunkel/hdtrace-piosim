package de.hd.pvs.TraceFormat.relation;

import java.util.HashMap;

/**
 * Manages known relations across files.
 * This allows to link multiple files together.
 * 
 * @author julian
 *
 */
public class RelationManager {
	
	/**
	 * map hostID to a local map to map to the topology and finally to the relation.
	 */
	final HashMap<String, RelationHeader> map = new HashMap<String, RelationHeader>();
	
	/**
	 * Add a file to the appropriate mapping.
	 * @param filename
	 */
	public void addFile(String filename) throws Exception{
		RelationXMLReader reader = new RelationXMLReader(filename);
		System.out.println(reader.getHeader().getUniqueID()); 
		
		while(true){
			RelationEntry entry = reader.getNextEntry();
			if(entry == null){
				break;
			}
			System.out.println();
		}
	}
	
	public static void main(String[] args) throws Exception {
		RelationManager m = new RelationManager();
		m.addFile("/tmp/test_host0_process0.rel");
		m.addFile("/tmp/test_host0_process1.rel");
		m.addFile("/tmp/test_host1_process0.rel");
	}
}
