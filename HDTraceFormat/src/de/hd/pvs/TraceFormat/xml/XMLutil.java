
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
	 * Parse the long value of the Element's subtag or use the default value.
	 *  
	 * @param element
	 * @param tag
	 * @param defaults
	 * @return
	 * @throws Exception
	 */
	static public long getLongValue(XMLTag element, String tag, long defaults)
	throws Exception {
		String text;
		try {
			text = element.getFirstNestedXMLTagWithName(tag).getContainedText();
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
	static public long getLongValueAttribute(XMLTag element, String attributeName, long defaults)
	throws Exception {
		String text;
		try {
			text = element.getAttribute(attributeName);
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
	static public int getIntValueAttribute(XMLTag element, String attributeName, int defaults)
	throws Exception {
		String text = "null";
		try {
			text = element.getAttribute(attributeName);
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
	static public long getLongValue(XMLTag element, String tag, String defaults)
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
	static public double getDoubleValue(XMLTag element, String tag,
			double defaults) throws Exception {
		String text;
		try {
			text = element.getFirstNestedXMLTagWithName(tag).getContainedText();
			if (text == null || text.length() == 0) {
				return defaults;
			}
		} catch (Exception e) {
			return defaults;
		}
		return Numbers.getDoubleValue(text);
	}
}
