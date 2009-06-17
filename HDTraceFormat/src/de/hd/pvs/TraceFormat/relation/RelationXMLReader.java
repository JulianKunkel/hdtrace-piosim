package de.hd.pvs.TraceFormat.relation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

import javax.xml.stream.XMLStreamException;

import de.hd.pvs.TraceFormat.relation.file.RawRelationXMLReader;
import de.hd.pvs.TraceFormat.relation.file.RelationCreate;
import de.hd.pvs.TraceFormat.relation.file.RelationEndState;
import de.hd.pvs.TraceFormat.relation.file.RelationFileEntry;
import de.hd.pvs.TraceFormat.relation.file.RelationStartState;
import de.hd.pvs.TraceFormat.relation.file.RelationFileEntry.Type;
import de.hd.pvs.TraceFormat.trace.IStateTraceEntry;
import de.hd.pvs.TraceFormat.trace.ITraceEntry;
import de.hd.pvs.TraceFormat.trace.RelationSource;
import de.hd.pvs.TraceFormat.trace.StateTraceEntry;
import de.hd.pvs.TraceFormat.util.Epoch;
import de.hd.pvs.TraceFormat.xml.XMLTag;

/**
 * On demand reader, creates RelationEntries tries to minimize memory footprint.
 * The reader creates entries by end time order.
 * 
 * @author julian
 *
 */
public class RelationXMLReader implements RelationSource{
	final RawRelationXMLReader reader;
	
	/**
	 * Contains already read but yet unprocessed entries
	 */
	final HashMap<Long, ArrayList<RelationFileEntry>> readEntries = new HashMap<Long, ArrayList<RelationFileEntry>>();
	
	public RelationXMLReader(String file) throws Exception{
		reader = new RawRelationXMLReader(file);
	}

	/**
	 * Read the XML file until a new entry can be manufactured.
	 * The returned entry is the next finished Relation Entry.
	 * 
	 * @return
	 * @throws XMLStreamException
	 */
	public RelationEntry getNextEntry() throws XMLStreamException{

		final Epoch timeAdjustment = reader.getHeader().getTimeAdjustment();
		
		while(true){
			RelationFileEntry entry = reader.getNextEntry();
			if(entry == null){
				break;
			}

			final Long tokenID = entry.getRelationTokenID();
			
			//System.out.println(entry.getType() + " " + tokenID);
			
			if(entry.getType() == Type.TERMINATE){
				// now we got one => manufacture entry.
				final ArrayList<RelationFileEntry> containedEntries = readEntries.remove(tokenID);
				final ArrayList<IStateTraceEntry>  traceEntries = new ArrayList<IStateTraceEntry>();
				
				final Stack<RelationStartState> startedStateStack = new Stack<RelationStartState>();
				// currently nested entries, they will be attached to the parent.
				final Stack<ArrayList<ITraceEntry>> nestedEntriesStack = new Stack<ArrayList<ITraceEntry>>();
				
				for(RelationFileEntry child: containedEntries){
					switch(child.getType()){
					case END_STATE:
						// merge it with the last stacked entry:
						final RelationEndState end = (RelationEndState) child;
						final RelationStartState start = startedStateStack.pop();
						final ArrayList<ITraceEntry> nestedEntries  = nestedEntriesStack.pop();
						
						// merge attributes from start & end
						final HashMap<String, String> attributes = new HashMap<String, String>(start.getData().getAttributes());
				
						// remove name tag:
						attributes.remove("name");
						attributes.putAll(end.getData().getAttributes());
						
						attributes.put("time", start.getTime().toString());
						attributes.put("end", end.getTime().toString());
						
						// add start & end data:
						final ArrayList<XMLTag> nestedData = new ArrayList<XMLTag>(start.getData().getNestedXMLTags());
						nestedData.addAll(end.getData().getNestedXMLTags());
						final StateTraceEntry newState = new StateTraceEntry(start.getName(), attributes, 
								timeAdjustment.add(start.getTime()), timeAdjustment.add(child.getTime()), nestedEntries, nestedData);
						
						// add it to the traceEntries if it is a toplevel state and not nested.
						if(startedStateStack.size() > 0){
							nestedEntriesStack.peek().add(newState);
						}else{
							traceEntries.add(newState);
						}
						break;
					case START_STATE:
						startedStateStack.add((RelationStartState) child);
						nestedEntriesStack.add(new ArrayList<ITraceEntry>(0));
						break;
					}					
				}
				
				if(startedStateStack.size() != 0){
					throw new XMLStreamException("Stack was not empty, invalid XML!");
				}
				
				final RelationFileEntry creationEntry =  containedEntries.get(0);
				final RelationFileEntry terminateEntry =  entry;
				assert(creationEntry.getType() == Type.CREATE);
				
				return new RelationEntry(((RelationCreate) creationEntry).getParentToken(), 
						tokenID, reader.getHeader(), traceEntries, 
						timeAdjustment.add(creationEntry.getTime()), timeAdjustment.add(terminateEntry.getTime()));
			}
			
			// remember entry: 			
			ArrayList<RelationFileEntry> list = readEntries.get(tokenID);
			if(list == null){
				list = new ArrayList<RelationFileEntry>();
				readEntries.put(tokenID, list);
			}
			list.add(entry);								
		}
		
		return null;
	}
	
	public RelationHeader getHeader(){
		return reader.getHeader();
	}
}
