
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

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.hd.pvs.TraceFormat.util.Epoch;
import de.hd.pvs.TraceFormat.util.Numbers;

/**
 * XML helper class. Contains functions which simplify the parsing of a XML DOM.
 * 
 * @author Julian M. Kunkel
 */
public class XMLutil {
	
	/**
	 * Return a list of all Tag nodes which match with the given tag. 
	 * @param element
	 * @param tag
	 * @return
	 */
	static public ArrayList<Element> getElementsByTag(Element element, String tag) {
		ArrayList<Element> list = getChildElements(element);
		
		// remove all elements which do not match.
		for (int i =  list.size() -1; i >= 0 ; i--) {
			Element node = list.get(i);
			if ( node.getNodeName().compareToIgnoreCase(tag) != 0) {
				list.remove(i);
			}
		}
		
		return list;
	}
	
	/**
	 * Return a list of child nodes.
	 * 
	 * @param element
	 * @return
	 */
	static public ArrayList<Element> getChildElements(Element element) {
		ArrayList<Element> list = new ArrayList<Element>();
		if (element == null){
			return list; 
		}
		NodeList children = element.getChildNodes();
		
		for (int i = 0; i < children.getLength(); i++) {
			Node node = children.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				list.add((Element) node);
			}
		}
		
		return list;
	}
	
	/**
	 * Return at most one node which matches with the specified tag. 
	 * @param element or null if there is no such element.
	 * @param tag
	 * @return
	 * @throws Exception
	 */
	static public Element getFirstElementByTag(Element element, String tag)
	throws Exception {
		ArrayList<Element> list = XMLutil.getElementsByTag(element, tag);
		if (list.size() == 0)
			return null;
		return list.get(0);
	}
	
	/**
	 * Return the plain text contained in a Tag.
   * Strips whitespace around the text.
	 * @param element
	 * @param tag
	 * @return an empty String if there is no such Element. 
	 * @throws Exception
	 */
	static public String getPlainText(Element element, String tag)
	throws Exception {
		return getPlainText(element, tag, "");
	}
	
	/**
	 * Return the plain text contained in a Tag or the default String if there is no such Tag.
	 * Strips whitespace around the text.
	 *  
	 * @param element
	 * @param tag
	 * @return
	 * @throws Exception
	 */
	static public String getPlainText(Element element, String tag, String defaultString)
	throws Exception {
		Node node = XMLutil.getFirstElementByTag(element, tag);
		if (node == null)
			return defaultString;
		return node.getTextContent().trim();
	}
	
	/**
	 * Parse an Epoch in a Subnode (Tag) as specified. Use the defaultTime if there is no such Tag
	 * in the Node.  
	 * 
	 * @param element The Node in which the nested Tag should occur.
	 * @param tag
	 * @param defaultTime
	 * @return
	 * @throws Exception
	 */
	static public Epoch getTime(Element element, String tag, Epoch defaultTime) throws Exception{
		return Epoch.parseTime(XMLutil.getPlainText(element, tag, defaultTime.toString()));
	}
	
	/**
	 * Return the attribute Text of the attribute with the attributeName.
	 * @param element
	 * @param attributeName
	 * @return
	 * @throws Exception
	 */
	static public String getAttributeText(Element element, String attributeName)
	throws Exception {
		Node node = element.getAttributes().getNamedItem(attributeName);
		if (node == null) 
			return null;
		return node.getNodeValue();
	}
	
	/**
	 * Parse the long value of the Element's subtag or use the default value.
	 *  
	 * @param element
	 * @param tag
	 * @param defaults
	 * @return
	 * @throws Exception
	 */
	static public long getLongValue(Element element, String tag, long defaults)
	throws Exception {
		String text;
		try {
			text = XMLutil.getPlainText(element, tag);
			if (text == null || text.length() == 0) {
				return defaults;
			}
		} catch (Exception e) {
			return defaults;
		}
		return Numbers.getLongValue(text);
	}
	
	/**
	 * Parse the long value of the Element's attribute or use the default value.
	 * 
	 * @param element
	 * @param attributeName
	 * @param defaults
	 * @return
	 * @throws Exception
	 */
	static public long getLongValueAttribute(Element element, String attributeName, long defaults)
	throws Exception {
		String text;
		try {
			text = element.getAttributes().getNamedItem(attributeName).getNodeValue();
			if (text == null || text.length() == 0) {
				return defaults;
			}
		} catch (Exception e) {
			return defaults;
		}
		return Numbers.getLongValue(text);
	}
	
	/**
	 * Parse the int value of the Element's attribute or use the default value.
	 * 
	 * @param element
	 * @param attributeName
	 * @param defaults
	 * @return
	 * @throws Exception
	 */
	static public int getIntValueAttribute(Element element, String attributeName, int defaults)
	throws Exception {
		String text = "null";
		try {
			text = element.getAttributes().getNamedItem(attributeName).getNodeValue();
			if (text == null || text.length() == 0) {
				return defaults;
			}
		} catch (Exception e) {
			return defaults;
		}
		return Integer.parseInt(text);
	}
	
	/**
	 * Parse the long value of the Element's attribute or parse the default String value.
	 * 
	 * @param element
	 * @param tag
	 * @param defaults
	 * @return
	 * @throws Exception
	 */
	static public long getLongValue(Element element, String tag, String defaults)
	throws Exception {
		return getLongValue(element, tag, Numbers.getLongValue(defaults));
	}
	
	/**
	 * Parse the double value of the Element's attribute or use the default value.
	 * @param element
	 * @param tag
	 * @param defaults
	 * @return
	 * @throws Exception
	 */
	static public double getDoubleValue(Element element, String tag,
			double defaults) throws Exception {
		String text;
		try {
			text = XMLutil.getPlainText(element, tag);
			if (text == null || text.length() == 0) {
				return defaults;
			}
		} catch (Exception e) {
			return defaults;
		}
		return Numbers.getDoubleValue(text);
	}
}
