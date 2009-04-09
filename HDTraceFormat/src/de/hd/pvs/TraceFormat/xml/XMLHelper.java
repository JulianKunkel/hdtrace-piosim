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

/**
 * Contains helper functions for reading/writing XML
 * 
 * @author Julian M. Kunkel
 *
 */
public class XMLHelper {
	
	/**
	 * Escape characters in string which are not valid to write in an attribute:
	 * 
   *   Attribute value:
   *    "<"  =>  "&lt;"
   *    "&"  =>  "&amp;"
   *    '"'  =>  '&quot;' (if using quote" as delimiter)
   *    "'"  =>  "&apos;" (if using apostrophe' as delimiter)
   *    
   *  It is expected that apostrophe is used as a delimiter
	 * @param string
	 * @return
	 */
	static public String escapeAttribute(String value){
		return value.replace("<", "&lt;").replace("'", "&apos;").replace("&", "&amp;");
	}
	
	/**
	 * Escape characters in string which are not valid to write in an Text node:
	 * 
	 *  Text node:
   *    "<"  =>  "&lt;"
   *    "&"  =>  "&amp;"
   */
	static public String escapeTagString(String text){
		return text.replace("<", "&lt;").replace("&", "&amp;");
	}
	
	/**
	 * Remove invalid content from a tag.
	 * @param tag
	 * @return
	 */
	static public String validTag(String tag){
		return tag.replaceAll("[^a-zA-Z0-9]", "");
	}
}
