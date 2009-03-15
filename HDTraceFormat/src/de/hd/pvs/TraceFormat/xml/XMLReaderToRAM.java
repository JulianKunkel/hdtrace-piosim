package de.hd.pvs.TraceFormat.xml;

import java.io.ByteArrayInputStream;
import java.io.IOException;
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
 * @author julian
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
			throw new IllegalArgumentException(e);
		}
		
		Element applicationNode = DOMdocument.getDocumentElement();
		
		return readXMLTag(applicationNode, null);
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
		
		return readXMLTag(document.getDocumentElement(), null);
	}
	
	private XMLTag readXMLTag(Element tag, XMLTag parent){
		HashMap<String, String> attributes = new HashMap<String, String>();
		
		NamedNodeMap map = tag.getAttributes();
		for(int i=0; i < map.getLength(); i++){
			attributes.put(map.item(i).getNodeName(), map.item(i).getTextContent());			
		}
		
		XMLTag xmlTag = new XMLTag(tag.getNodeName(), attributes , parent);
				
		// scan for child nodes:
		final NodeList list = tag.getChildNodes();
		for(int i=0; i < list.getLength(); i++){
			final Node item = list.item(i);
			if(item.getNodeType() == Node.ELEMENT_NODE){
				XMLTag child = readXMLTag((Element) item, xmlTag);
			}else if(item.getNodeType() == Node.TEXT_NODE){
				final String txt = item.getTextContent().trim(); 
				if(txt.length() > 0){
					xmlTag.setContainedText(txt);
				}
			}
		}
		
		return xmlTag;
	}
}
