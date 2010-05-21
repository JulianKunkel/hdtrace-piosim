
 /** Version Control Information $Id$
  * @lastmodified    $Date$
  * @modifiedby      $LastChangedBy$
  * @version         $Revision$ 
  */


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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Read a complete XML with DOM and create XMLTags, this is useful for unification of
 * the read process. 
 * 
 * @author Julian M. Kunkel
 */
public class XMLReaderToRAM {

	/**
	 * Protected DOM Builder
	 */
	protected DocumentBuilder DOMbuilder;
	
	public XMLReaderToRAM(){
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try{
			DOMbuilder = factory.newDocumentBuilder();
		}catch(ParserConfigurationException e){
			throw new IllegalStateException(e);
		}
	}

	/**
	 * Read the XML file and return the XML root tag.
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public XMLTag readXML(String file) throws IOException{
		//Contains the DOM, read during readProjectDescription
		Document DOMdocument ;
		try{
			DOMdocument = DOMbuilder.parse(file);
		}catch(SAXException e){
			throw new IOException(e);
		}
		
		Element applicationNode = DOMdocument.getDocumentElement();
		
		return readXMLTag(applicationNode);
	}
	
	/**
	 * Read the XML file and return the XML root tag.
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public XMLTag convertXMLToXMLTag(String string) throws Exception{
		//Contains the DOM, read during readProjectDescription		
		Document document = DOMbuilder.parse(  new ByteArrayInputStream( string.getBytes() ));
		
		return readXMLTag(document.getDocumentElement());
	}
	
	private XMLTag readXMLTag(Element tag){
		HashMap<String, String> attributes = new HashMap<String, String>();
		
		NamedNodeMap map = tag.getAttributes();
		for(int i=0; i < map.getLength(); i++){
			attributes.put(map.item(i).getNodeName(), map.item(i).getTextContent());			
		}
				
		String containedText = null;
		
		final ArrayList<XMLTag> nestedXMLTags = new ArrayList<XMLTag>(0);		
		
		// scan for child nodes:
		final NodeList list = tag.getChildNodes();
		for(int i=0; i < list.getLength(); i++){
			final Node item = list.item(i);
			if(item.getNodeType() == Node.ELEMENT_NODE){
				XMLTag child = readXMLTag((Element) item);
				nestedXMLTags.add(child);
			}else if(item.getNodeType() == Node.TEXT_NODE){
				final String txt = item.getTextContent().trim(); 
				if(txt.length() > 0){
					containedText = txt;
				}
			}
		}
		
		final XMLTag xmlTag = new XMLTag(tag.getNodeName(), attributes , containedText, nestedXMLTags);
		return xmlTag;
	}
}
