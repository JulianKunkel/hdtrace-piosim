package de.hd.pvs.TraceFormat.relation;

import javax.xml.stream.XMLStreamException;

import de.hd.pvs.TraceFormat.relation.file.RawRelationXMLReader;
import de.hd.pvs.TraceFormat.relation.file.RelateInternal;
import de.hd.pvs.TraceFormat.relation.file.RelationFileEntry;

/**
 * On demand reader, tries to minimize memory footprint.
 * 
 * @author julian
 *
 */
public class RelationXMLReader {
	final RawRelationXMLReader reader;
	
	
	
	public RelationXMLReader(String file) throws Exception{
		reader = new RawRelationXMLReader(file);
	}

	/**
	 * Read the XML file until a new entry can be manufactured.
	 * 
	 * @return
	 * @throws XMLStreamException
	 */
	public RelationEntry getNextEntry() throws XMLStreamException{

		while(true){
			RelationFileEntry entry = reader.getNextEntry();
			if(entry == null){
				break;
			}

			System.out.println(entry.getType() + " " + entry.getRelationID());
			
			switch(entry.getType()){
			case RELATE_INTERNAL:
			case RELATE_LOCAL:
			case RELATE_PROCESS:
			case RELATE_REMOTE:
				System.out.println(((RelateInternal) entry).getFullTokenID());
			}
			
		}
		
		return null;
	}
	
	public RelationHeader getHeader(){
		return reader.getHeader();
	}
}
