package de.hd.pvs.TraceFormat.xml;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * This class allows to create an XML tag by arbitrary data. Kind of writable representant for the XML tag.
 * @author julian
 *
 */
public class XMLTagCreator {
	private final ArrayList<XMLTag> nestedXMLTags = new ArrayList<XMLTag>(0);

	private final HashMap<String, String> attributes = new HashMap<String, String>(0);	
	private String				  name;
	private String                containedText;
	
	public void addAttribute(String key, String val){
		attributes.put(key, val);
	}
	
	public String getAttribute(String key){
		return attributes.get(key);
	}	
	
	public void addNestedXMLTag(XMLTag tag){
		nestedXMLTags.add(tag);
	}
	
	public ArrayList<XMLTag> getNestedXMLTags() {
		return nestedXMLTags;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void setContainedText(String containedText) {
		this.containedText = containedText;
	}
	
	
	public String getName() {
		return name;
	}
	
	public String getContainedText() {
		return containedText;
	}
	
	/**
	 * Create a readable XML tag with the contained data
	 * @return
	 */
	public XMLTag createXMLTag(){
		return new XMLTag(name, attributes, containedText, nestedXMLTags);
	}
}
