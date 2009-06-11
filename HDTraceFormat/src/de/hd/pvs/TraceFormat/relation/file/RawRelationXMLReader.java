package de.hd.pvs.TraceFormat.relation.file;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Stack;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.sun.xml.internal.stream.XMLInputFactoryImpl;

import de.hd.pvs.TraceFormat.relation.RelationHeader;
import de.hd.pvs.TraceFormat.util.Epoch;
import de.hd.pvs.TraceFormat.xml.XMLTag;
import de.hd.pvs.TraceFormat.xml.XMLTagCreator;


public class RawRelationXMLReader {
	private final XMLStreamReader reader;
	final RelationHeader header;

	private RelationHeader readRelationHeader() throws XMLStreamException{	
		reader.next();		

		final HashMap<String, String> attributes = new HashMap<String, String>();
		// read attributes
		for( int i = 0; i < reader.getAttributeCount(); i++ ){			
			attributes.put(reader.getAttributeLocalName(i), reader.getAttributeValue(i));
		}

		return new RelationHeader(attributes.get("localToken"), 
				attributes.get("hostID"), 
				Integer.parseInt(attributes.get("topologyNumber")),
				Epoch.parseTime(attributes.get("timeAdjustment")));
	}

	public RawRelationXMLReader(String filename) throws IOException, XMLStreamException{
		reader = XMLInputFactoryImpl.newInstance().createXMLStreamReader(new BufferedInputStream(new FileInputStream(filename)));
		header = readRelationHeader();
	}

	public RelationFileEntry getNextEntry() throws XMLStreamException{

		/**
		 * Top level nested data:
		 */
		final Stack<XMLTagCreator> stackedData = new Stack<XMLTagCreator>();  

		XMLTag nestedElementTag = null;

		while(reader.hasNext()){
			final int nextType = reader.next();

			switch(nextType){
			case(XMLStreamConstants.CHARACTERS):
				final String str = reader.getText().trim();
			if(str.length() > 0){
				if(stackedData.size() == 0){
					throw new IllegalArgumentException("Invalid text \"" + str + "\" at: " + getPosition());
				}

				stackedData.peek().setContainedText(str);
			}
			break;
			case(XMLStreamConstants.START_ELEMENT):{
				final String name =  reader.getName().getLocalPart();
				
				// check if there are nested XMLTrace stats
				XMLTagCreator currentData = new XMLTagCreator();
				currentData.setName(name);

				// generate attributes
				for( int i = 0; i < reader.getAttributeCount(); i++ ){			
					currentData.addAttribute(reader.getAttributeLocalName(i), reader.getAttributeValue(i));
				}
				
				stackedData.push(currentData);
			}
			break;
			case(XMLStreamConstants.END_ELEMENT):{
				final String name =  reader.getName().getLocalPart();
				
				if(name.equals("relation")){
					return null;
				}
					
				final XMLTagCreator currentData = stackedData.pop();

				if(nestedElementTag != null){
					// attach nested element to child:						
					currentData.addNestedXMLTag(nestedElementTag);					
					nestedElementTag = null;
				}
				
				final XMLTag newData = currentData.createXMLTag();
				assert(newData.getName().equals(name));					

				if(stackedData.size() == 0){					
					final String token = newData.getAttribute("t");
					final String timeStr = newData.getAttribute("time");
										
					try{
					// now decide what element to create:
					if(name.equals("rel")){
						if(newData.getAttribute("l") != null){
							String [] split = newData.getAttribute("l").split(":");
							return new RelateLocal(token, split[2], split[1], split[0]);
						}else if(newData.getAttribute("i") != null){
							String [] split = newData.getAttribute("i").split(":");
							return new RelateInternal(token, split[0]);
						}else if(newData.getAttribute("p") != null){
							String [] split = newData.getAttribute("p").split(":");
							return new RelateProcess(token, split[1], split[0]);
						}else if(newData.getAttribute("r") != null){
							String [] split = newData.getAttribute("r").split(":");
							return new RelateRemote(token, split[3], split[2], split[1], split[0]);
						}else{
							return new RelationInitial(token);
						}
					}else if (name.equals("s")){						
						Epoch time = Epoch.parseTime(timeStr);						
						return new RelationStartState(newData, header.getTimeAdjustment().add(time));
					}else if (name.equals("e")){						
						Epoch time = Epoch.parseTime(timeStr);		
						return new RelationEndState(newData, header.getTimeAdjustment().add(time));
					}else if (name.equals("un")){
						return new RelationTerminate(token);
					}else{
						throw new IllegalArgumentException("Found invalid tag with name: " + name);
					}		
					}catch(Exception e){
						throw new IllegalArgumentException("Found invalid tag: " + newData, e);
					}
				}

				stackedData.peek().addNestedXMLTag(newData);					
			}
			break;
			default:

			}

		}
		return null;
	}

	/**
	 * Return the file header
	 * @return
	 */
	public RelationHeader getHeader() {
		return header;
	}	
	
	private String getPosition(){
		return " line: " + reader.getLocation().getLineNumber() + " column: " + reader.getLocation().getColumnNumber(); 
	}
	
	public void close() throws XMLStreamException{
		reader.close();
	}
}
