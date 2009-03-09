package de.hd.pvs.traceConverter.Input.Trace;

import java.util.HashMap;
import java.util.LinkedList;

/**
 * DOM like nested XML Tags
 * 
 * @author julian
 *
 */
public class XMLTag {
	private LinkedList<XMLTag> nestedXMLTags;

	private final HashMap<String, String> attributes;	
	private final String					  name;
	private String                  containedText = null;
	
	private XMLTag 	parentTag;
	
	public XMLTag(final String name, final HashMap<String, String> attributes,
			XMLTag parentXMLTag) {
		this.attributes = attributes;
		this.name = name;
		
		if(	parentXMLTag != null){
			parentXMLTag.addXMLChildTag(this);
		}
		parentTag = parentXMLTag;
	}
	
	private void addXMLChildTag(XMLTag tag){
		if (nestedXMLTags == null){
			nestedXMLTags = new LinkedList<XMLTag>();
		}
		
		nestedXMLTags.add(tag);
	}
	
	public void setXMLParentTag(XMLTag parent){
		parent.addXMLChildTag(this);
		this.parentTag = parent;
	}
	
	public LinkedList<XMLTag> getNestedXMLTags() {
		return nestedXMLTags;
	}
	
	public void setContainedText(String containedText) {
		this.containedText = containedText;
	}
	
	public HashMap<String, String> getAttributes() {
		return attributes;
	}
	
	public String getAttribute(String attribute){
		return attributes.get(attribute);
	}

	public void addAttribute(String attribute, String value){
		attributes.put(attribute, value);
	}
	
	public String getContainedText() {
		return containedText;
	}
	
	public XMLTag getParentTag() {
		return parentTag;
	}
	
	public boolean isChild(){
		return parentTag != null;
	}
	
	public String getName() {
		return name;
	}
	
	@Override
	public String toString() {
		StringBuffer buff = new StringBuffer();
		for(String key: attributes.keySet()){
			buff.append(" " + key + "=\"" + attributes.get(key) + "\"");
		}		
		
		buff.append(">");
		
		if(nestedXMLTags != null){
			// print nestedXMLTags
			for(XMLTag child: nestedXMLTags){
				buff.append(child);
			}
		}
		
		if(containedText != null)
			buff.append(containedText);
		
		return "<" + name + buff.toString() + "</" + name + ">\n";
	}
	
}
