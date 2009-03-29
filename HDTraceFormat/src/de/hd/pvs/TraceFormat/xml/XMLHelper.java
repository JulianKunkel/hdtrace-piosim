package de.hd.pvs.TraceFormat.xml;

/**
 * Contains helper functions for reading/writing XML
 * 
 * @author julian
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
