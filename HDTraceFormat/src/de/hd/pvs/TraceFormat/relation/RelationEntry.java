package de.hd.pvs.TraceFormat.relation;

import java.util.ArrayList;

import de.hd.pvs.TraceFormat.trace.StateTraceEntry;

/**
 * An entry as present in the relation file.
 * @author julian
 *
 */
public class RelationEntry {
	final ArrayList<String> parents;
	final ArrayList<String> children;
	final ArrayList<StateTraceEntry> states;

	public RelationEntry(ArrayList<String> parents, 
			ArrayList<String> children, 
			ArrayList<StateTraceEntry> states) {
		this.parents = parents;
		this.children = children;
		this.states = states;
	}
}
