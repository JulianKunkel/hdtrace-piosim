package de.hd.pvs.traceConverter;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.LinkedList;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import sun.awt.X11.Depth;

import de.hd.pvs.traceConverter.Input.InputData;

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
	 * set on true when the Program tag is set
	 */
	boolean startProcessing = false;

	/**
	 * Maximum number of items which should be read ahead from the Input file
	 */
	private static final int CACHED_ELEMENTS_SIZE = 5;


	/**
	 * Thread controlling the SAX Parser, Producer & Consumer Design Pattern
	 *
	 */
	class SaxThread extends Thread {

		/**
		 * The data read already from the trace file.
		 */
		private LinkedList<InputData> readData = new LinkedList<InputData>();	

		/**
		 * is the trace file read completely
		 */
		boolean finished = false;
		private SAXParser        saxParser;
		/**
		 * The file stream used for input
		 */
		private BufferedInputStream stream;

		// This method is called when the thread runs
		public void run(){
			try{
				// Parse XML file:
				saxParser.parse(stream, getParent());				
				finalizeParsing();											
			}catch(Exception e){
				e.printStackTrace();
			}
		}

		public void init(BufferedInputStream stream) throws Exception{
			// Create a SaxParser
			SAXParserFactory saxFactory   = SAXParserFactory.newInstance();
			saxParser = saxFactory.newSAXParser();
			this.stream = stream;
		}

		/**
		 * Called once when the parsing is finished
		 */
		synchronized private void finalizeParsing(){
			finished = true;
			//System.out.println("Parsing thread finished");
			notifyAll();
		}

		/**
		 * Add read data to the queue
		 * 
		 * @param elem
		 * @throws InterruptedException
		 */
		synchronized public void push (InputData elem) throws InterruptedException {
			while (readData.size() >= CACHED_ELEMENTS_SIZE)
				wait();

			//System.out.println("Push now");

			readData.push(elem);

			notify();
		}

		/**
		 * Poll (read) data from the queue
		 * @return
		 * @throws InterruptedException
		 */
		synchronized public InputData pollFirst () throws InterruptedException {			
			while (readData.size() == 0 && finished == false)
				wait();

			//System.out.println("Poll now");

			notify();
			return(readData.pollFirst());
		} 
	}

	private SaxThread saxThread = new SaxThread();


	private SaxTraceFileTokenizer getParent(){
		return this;
	}

	/**
	 * Get new input data if available, may block until new data is available
	 * 
	 * @return
	 */
	public InputData getNextInputData(){		
		try{
			return saxThread.pollFirst();
		}catch(InterruptedException e){
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Is called by the SaxThread
	 */
	private void addElements(InputData data){
		try{
			saxThread.push(data);
		}catch(InterruptedException e){
			e.printStackTrace();
		}
	}

	/**
	 * Constructor, start processing of the file.
	 * @param filename
	 */
	public SaxTraceFileTokenizer(String filename) throws Exception {
		saxThread.init(new BufferedInputStream(new FileInputStream(filename)));       
		saxThread.start();
		// give the SaxThread some time to produce new data.
		Thread.yield();
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
		nesting_depth++;
		
		if (qName.equals("Program") && nesting_depth == 2){
			startProcessing = true;
			return;
		}

		if(! startProcessing)
			return;

		indent();

		System.out.print("<" + qName);

		// Test-Code um zu sehen, was in namespaceURI und localName steht
		// System.out.print(" " + namespaceURI);
		// System.out.print(" " + localName);

		// Attribute ausgeben
		for( int i = 0; i < atts.getLength(); i++ )
			System.out.print(" " + atts.getQName(i) + "=\"" + atts.getValue(i) + "\"");

		System.out.println(">");
			}

	/**
	 * Wird bei jedem schließenden Tag aufgerufen, definiert im Interface ContentHandler.
	 *
	 * @param namespaceURI URI des Namespaces für dieses Element, kann auch ein leerer String sein.
	 * @param localName Lokaler Name des Elements.
	 * @param qName Qualifizierter Name des Elements.
	 */
	public void endElement(String namespaceURI, String localName, String qName) throws SAXException
	{
		nesting_depth--;
		
		if (qName.equals("Program") && nesting_depth == 1){
			startProcessing = false;
			return;
		}

		if(! startProcessing)
			return;
		
		indent();
		
		System.out.println("</" + qName + ">");

		addElements(new InputData(new SimpleEpoch(0,0)));
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
