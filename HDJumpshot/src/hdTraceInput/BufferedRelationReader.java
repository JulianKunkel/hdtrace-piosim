package hdTraceInput;

import java.util.ArrayList;

import de.hd.pvs.TraceFormat.TraceObject;
import de.hd.pvs.TraceFormat.relation.RelationEntry;
import de.hd.pvs.TraceFormat.relation.RelationHeader;
import de.hd.pvs.TraceFormat.relation.RelationXMLReader;
import de.hd.pvs.TraceFormat.trace.RelationSource;
import de.hd.pvs.TraceFormat.util.Epoch;

public class BufferedRelationReader implements IBufferedReader, RelationSource {

	final ArrayList<RelationEntry> entries = new ArrayList<RelationEntry>(20);
	final RelationHeader header;
	
	public BufferedRelationReader(String filename) throws Exception {
		final RelationXMLReader reader = new RelationXMLReader(filename);
		header = reader.getHeader();
		
		// now add all entries
		while(true){
			final RelationEntry entry = reader.getNextEntry();
			if(entry == null){
				break;
			}
			
			entries.add(entry);
		}		
	}
	
	@Override
	public Epoch getMaxTime() {
		return entries.get(entries.size() -1 ).getLatestTime();
	}

	@Override
	public Epoch getMinTime() {
		return entries.get(0).getEarliestTime();
	}
	
	public RelationHeader getHeader() {
		return header;
	}

	@Override
	public TraceObject getTraceEntryClosestToTime(Epoch time) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public ArrayList<RelationEntry> getEntries() {
		return entries;
	}
}
