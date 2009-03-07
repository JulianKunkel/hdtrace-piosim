package de.hd.pvs.traceConverter;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Reads the XML file on demand with a Sax XML Parser.
 * 
 * @author julian
 */
public class SaxTraceFileTokenizer extends DefaultHandler{

	/**
     * Depths of the tag nesting.
     */
    private int nesting_depth = 0;

    
	/**
	 * Constructor, start the processing of the file.
	 * @param filename
	 */
	public SaxTraceFileTokenizer(String filename) {
        try {
            // Create a SaxParser
            SAXParserFactory saxFactory   = SAXParserFactory.newInstance();
            SAXParser        saxParser = saxFactory.newSAXParser();

            // Parse XML file:
            saxParser.parse(filename, this);
        }
        catch (Exception e) {
            System.out.println(e);
        }
	}

    /**
     * Intent on the console:
     */
    public void indent()
    {
        for (int i=0;i<nesting_depth;i++)
            System.out.print("\t");
    }

    /**
     * Called upon start of the document.
     */
    public void startDocument() throws SAXException
    {
    }

    /**
     * Wird bei jedem öffnenden Tag aufgerufen, definiert im Interface ContentHandler.
     * Bei leeren Tags wie zum Beispiel &lt;img /&gt; wird startElement und
     * endElement direkt hintereinander aufgerufen. Mit J2SE 1.4.2 scheint nur
     * qName gefüllt zu sein.
     *
     * @param namespaceURI URI des Namespaces für dieses Element, kann auch ein leerer String sein.
     * @param localName Lokaler Name des Elements, kann auch ein leerer String sein.
     * @param qName Qualifizierter Name (mit Namespace-Prefix) des Elements.
     * @param atts Liste der Attribute.
     */
    public void startElement(String namespaceURI, String localName,
            String qName, Attributes atts) throws SAXException
    {
        indent();

        System.out.print("<" + qName);

        // Test-Code um zu sehen, was in namespaceURI und localName steht
        // System.out.print(" " + namespaceURI);
        // System.out.print(" " + localName);

        // Attribute ausgeben
        for( int i = 0; i < atts.getLength(); i++ )
            System.out.print(" " + atts.getQName(i) + "=\"" + atts.getValue(i) + "\"");

        System.out.println(">");

        nesting_depth++;
    }

    /**
     * Wird bei jedem schließenden Tag aufgerufen, definiert im Interface ContentHandler.
     *
     * @param namespaceURI URI des Namespaces für dieses Element, kann auch ein leerer String sein.
     * @param localName Lokaler Name des Elements.
     * @param qName Qualifizierter Name des Elements.
     */
    public void endElement(String namespaceURI, String localName, String qName)
    {
        nesting_depth--;

        indent();

        System.out.println("</" + qName + ">");
    }

    /**
     * Wird immer aufgerufen, wenn Zeichen im Dokument auftauchen.
     *
     * @param ch Character Array
     * @param start Startindex der Zeichen in ch
     * @param length Länge der Zeichenkette
     */
    public void characters(char ch[], int start, int length)
    {
        String s = new String(ch,start,length).trim();
        if (s.length() > 0) {
            indent();
            System.out.println(s);
        }
    }

    /**
     * Called if spaces are in the document " ", "\t", "\n", "\r"
     * irrelevant, therefore ignored. 
     */
    public void ignorableWhitespace(char[] cha, int start, int length)
    {
    }
}
