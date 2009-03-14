
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

package de.hd.pvs.traceConverter.Input.Trace;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.LinkedList;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


/**
 * Reads the XML file on demand with a Sax XML Parser.
 * 
 * @author Julian M. Kunkel
 */
public class SaxTraceFileReader{

	/**
	 * Maximum number of items which should be read ahead from the Input file
	 */
	private static final int CACHED_ELEMENTS_SIZE = 10;	

	private SaxThread saxThread = new SaxThread();

	private DefaultHandler traceHandler = new TraceHandler();

	/**
	 * is the trace file read completely
	 */
	boolean finished = false;

	/**
	 * Thread controlling the SAX Parser, Producer & Consumer Design Pattern
	 *
	 */
	class SaxThread extends Thread {

		/**
		 * The data read already from the trace file.
		 */
		private LinkedList<XMLTraceEntry> readData = new LinkedList<XMLTraceEntry>();	

		private SAXParser        saxParser;
		/**
		 * The file stream used for input
		 */
		private BufferedInputStream stream;

		// This method is called when the thread runs
		public void run(){
			try{
				// Parse XML file:
				saxParser.parse(stream, getParent().traceHandler);				
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
		synchronized public void push (XMLTraceEntry elem) throws InterruptedException {
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
		synchronized public XMLTraceEntry poll () throws InterruptedException {			
			while (readData.size() == 0 && finished == false)
				wait();

			//System.out.println("Poll now");

			notify();
			return(readData.pollLast());
		} 
	}


	final class TraceHandler extends DefaultHandler {

		/**
		 * State/Statistics which are currently build
		 */
		private XMLTag currentData;

		/**
		 * Top level nested data:
		 */
		private XMLTag nestedData;


		/**
		 * Current depths of the tag nesting.
		 */
		private int nesting_depth = 0; 
		
		/**
		 * set on true when the Program tag is set
		 */
		boolean startProcessing = false;

		/**
		 * Is called by the SaxThread, converts read XML data into a valid XML TraceEntry. 
		 */
		private void addElements(XMLTag data, XMLTag nestedData){
			XMLTraceEntry newTraceEntry;
			try{
				newTraceEntry = XMLTraceEntryFactory.manufactureXMLTraceObject(data, null, nestedData);
			}catch(IllegalArgumentException e){			
				throw new IllegalArgumentException("Invalid XML object: " + data, e);
			}

			try{
				saxThread.push(newTraceEntry);
			}catch(InterruptedException e){
				e.printStackTrace();
			}
		}

		/**
		 * Called upon start of the document.
		 */
		public void startDocument() throws SAXException
		{
		}

		/**
		 * Is called for all tag open elements.
		 */
		public void startElement(String namespaceURI, String localName,
				String qName, Attributes atts) throws SAXException{
			nesting_depth++;

			if (qName.equals("Program") && nesting_depth == 1){
				startProcessing = true;
				return;
			}

			if(! startProcessing)
				return;

			HashMap<String, String> attributes = new HashMap<String, String>();

			// generate attributes
			for( int i = 0; i < atts.getLength(); i++ )
				attributes.put(atts.getQName(i), atts.getValue(i));

			// check if there are nested XMLTrace stats
			XMLTag parent = currentData;
			currentData = new XMLTag(qName, attributes, parent);
		}

		/**
		 * Is called for all tag close elements.
		 */
		public void endElement(String namespaceURI, String localName, String qName) throws SAXException
		{
			nesting_depth--;

			if (qName.equals("Program") && nesting_depth == 0){
				startProcessing = false;
				return;
			}

			if(! startProcessing)
				return;

			if(qName.equals("Nested")){
				nestedData = currentData;				
			}else if(! currentData.isChild()){
				addElements(currentData, nestedData);
				nestedData = null;
			}
			currentData = currentData.getParentTag();
		}

		/**
		 * Gets called for all characters outside XML-Tags
		 */
		public void characters(char ch[], int start, int length)
		{
			String s = new String(ch,start,length).trim();
			if (s.length() > 0) {
				if(currentData != null)
					currentData.setContainedText(s);
				else
					System.out.println("C:" + s);
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


	private SaxTraceFileReader getParent(){
		return this;
	}


	/**
	 * Constructor, start processing of the file.
	 * @param filename
	 */
	public SaxTraceFileReader(String filename) throws Exception {
		saxThread.init(new BufferedInputStream(new FileInputStream(filename)));       
		saxThread.start();
		// give the SaxThread some time to produce new data.
		Thread.yield();
	}

	/**
	 * Get new input data if available, may block until new data is available
	 * 
	 * @return
	 */
	public XMLTraceEntry getNextInputData(){		
		try{
			return saxThread.poll();
		}catch(InterruptedException e){
			e.printStackTrace();
		}
		return null;
	}

	public boolean isFinished() {
		return finished;
	}
}
