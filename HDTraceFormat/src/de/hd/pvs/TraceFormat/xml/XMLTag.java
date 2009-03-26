
//	Copyright (C) 2008, 2009 Julian M. Kunkel
//	
//	This file is part of PIOsimHD.
//	
//	PIOsimHD is free software: you can redistribute it and/or modify
//	it under the terms of the GNU General Public License as published by
//	the Free Software Foundation, either version 3 of the License, or
//	(at your option) any later version.
//	
//	PIOsimHD is distributed in the hope that it will be useful,
//	but WITHOUT ANY WARRANTY; without even the implied warranty of
//	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//	GNU General Public License for more details.
//	
//	You should have received a copy of the GNU General Public License
//	along with PIOsimHD.  If not, see <http://www.gnu.org/licenses/>.

package de.hd.pvs.TraceFormat.xml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * DOM like nested XML Tags
 * 
 * @author Julian M. Kunkel
 *
 */
public class XMLTag {
	private ArrayList<XMLTag> nestedXMLTags;

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
			nestedXMLTags = new ArrayList<XMLTag>();
		}
		
		nestedXMLTags.add(tag);
	}
	
	public void setXMLParentTag(XMLTag parent){
		parent.addXMLChildTag(this);
		this.parentTag = parent;
	}
	
	public ArrayList<XMLTag> getNestedXMLTags() {
		return nestedXMLTags;
	}
	
	public LinkedList<XMLTag> getNestedXMLTagsWithName(String name){
		LinkedList<XMLTag> list = new LinkedList<XMLTag>();
		
		if (nestedXMLTags != null ){
			for(XMLTag tag: nestedXMLTags){
				if( tag.getName().equalsIgnoreCase(name) ){
					list.add(tag);
				}
			}
			return list;
		}
		return list;		
	}
	
	public LinkedList<XMLTag> getAndRemoveNestedXMLTagsWithName(String name){
		LinkedList<XMLTag> list = new LinkedList<XMLTag>();
		
		if (nestedXMLTags != null ){
			final Iterator<XMLTag> it = nestedXMLTags.iterator();

			while(it.hasNext()){
				final XMLTag tag = it.next(); 
				if( tag.getName().equalsIgnoreCase(name) ){
					it.remove();
					list.add(tag);
				}
			}
			return list;
		}
		return list;		
	}
	
	
	public XMLTag getFirstNestedXMLTagWithName(String name){
		if (nestedXMLTags != null ){
			for(XMLTag tag: nestedXMLTags){
				if( tag.getName().equalsIgnoreCase(name) ){
					return tag;
				}
			}
		}
		return null;
	}
	
	/**
	 * Removes the first child node with the given name, if it exists 
	 * @param name
	 * @return childNode which matches the name
	 */
	public XMLTag getAndRemoveFirstNestedXMLTagWithName(String name){
		if (nestedXMLTags != null ){
			final Iterator<XMLTag> it = nestedXMLTags.iterator();

			while(it.hasNext()){
				final XMLTag tag = it.next(); 
				if( tag.getName().equalsIgnoreCase(name) ){
					it.remove();
					return tag;
				}
			}
		}
		return null;
	}
	
	public void setNestedXMLTags(ArrayList<XMLTag> nestedXMLTags) {
		this.nestedXMLTags = nestedXMLTags;
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
	final public String toString() {
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
		
		return "<" + name + buff.toString() + "</" + name + ">";
	}
	
}
