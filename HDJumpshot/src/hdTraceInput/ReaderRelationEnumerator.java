package hdTraceInput;

import java.util.ArrayList;
import java.util.Enumeration;

import de.hd.pvs.TraceFormat.relation.RelationEntry;

public class ReaderRelationEnumerator implements Enumeration<RelationEntry> {	
	private int pos = 0;
	final private ArrayList<RelationEntry> entries;
	
	public ReaderRelationEnumerator(
			ArrayList<RelationEntry> entries
		) {
		this.entries = entries;
	}

	@Override
	public boolean hasMoreElements() {
		return pos < entries.size();
	}
	
	@Override
	public RelationEntry nextElement() {
		return entries.get(pos++);
	}
}