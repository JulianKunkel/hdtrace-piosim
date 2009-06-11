//	Copyright (C) 2009 Julian M. Kunkel
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

/**
 * This class allows to create an XML tag by arbitrary data. Kind of writable representant for the XML tag.
 * @author Julian M. Kunkel
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
	
	public String removeAttribute(String key){
		return attributes.remove(key);
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
