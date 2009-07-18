
/** Version Control Information $Id$
 * @lastmodified    $Date$
 * @modifiedby      $LastChangedBy$
 * @version         $Revision$ 
 */


//Copyright (C) 2008, 2009 Julian M. Kunkel

//This file is part of PIOsimHD.

//PIOsimHD is free software: you can redistribute it and/or modify
//it under the terms of the GNU General Public License as published by
//the Free Software Foundation, either version 3 of the License, or
//(at your option) any later version.

//PIOsimHD is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU General Public License for more details.

//You should have received a copy of the GNU General Public License
//along with PIOsimHD.  If not, see <http://www.gnu.org/licenses/>.

package de.hd.pvs.TraceFormat.project;

import java.io.File;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import de.hd.pvs.TraceFormat.ReservedTopologyNames;
import de.hd.pvs.TraceFormat.SimpleConsoleLogger;
import de.hd.pvs.TraceFormat.project.datatypes.ContiguousDatatype;
import de.hd.pvs.TraceFormat.project.datatypes.Datatype;
import de.hd.pvs.TraceFormat.project.datatypes.DatatypeEnum;
import de.hd.pvs.TraceFormat.project.datatypes.NamedDatatype;
import de.hd.pvs.TraceFormat.project.datatypes.StructDatatype;
import de.hd.pvs.TraceFormat.project.datatypes.VectorDatatype;
import de.hd.pvs.TraceFormat.topology.TopologyNode;
import de.hd.pvs.TraceFormat.topology.TopologyTypes;
import de.hd.pvs.TraceFormat.xml.XMLReaderToRAM;
import de.hd.pvs.TraceFormat.xml.XMLTag;

/**
 * This class is used to create a primitive project description from an XML file.
 *  
 * @author Julian M. Kunkel
 */

public class ProjectDescriptionXMLReader {

	protected XMLTag rootTag;

	public void readProjectDescription(ProjectDescription descriptionInOut, String projectFilename) throws IOException{
		final File XMLFile = new File(projectFilename);
		if (! XMLFile.canRead()) {
			throw new IllegalArgumentException("Project file not readable: " + XMLFile.getAbsolutePath());
		}
		descriptionInOut.setProjectFilename(XMLFile.getAbsolutePath());

		final XMLReaderToRAM reader = new XMLReaderToRAM();
		rootTag = reader.readXML(projectFilename);

		// read standard descriptions:
		descriptionInOut.setApplicationName( rootTag.getAttribute("name"));

		final XMLTag desc = rootTag.getAndRemoveFirstNestedXMLTagWithName("Description");
		if(desc != null){
			descriptionInOut.setDescription( desc.getContainedText());
		}

		final XMLTag xmlTopology = rootTag.getAndRemoveFirstNestedXMLTagWithName("Topology");
		TopologyTypes labels = new TopologyTypes();

		if(xmlTopology == null){
			throw new IllegalArgumentException("Topology Tag not found! Invalid XML!");
		}

		parseTopologyLabel(xmlTopology, labels);

		descriptionInOut.setTopologyTypes(labels);

		descriptionInOut.setTopologyRoot( 
				parseTopology(0, xmlTopology,
						new TopologyNode(descriptionInOut.getFilesPrefix(), null, ReservedTopologyNames.File.toString()), 
						descriptionInOut) );

		// parse the descriptions of the external statistics:
		XMLTag element = rootTag.getAndRemoveFirstNestedXMLTagWithName("ExternalStatistics");
		if(element != null && element.getNestedXMLTags() != null){
			final ArrayList<XMLTag> children = element.getNestedXMLTags(); 
			for(XMLTag stat: children){
				descriptionInOut.addStatisticsGroup(stat.getName());
			}
		}

		LinkedList<XMLTag>  elements;
		/* read communicator list */
		elements =  rootTag.getNestedXMLTagsWithName("CommunicatorList");
		if (elements.size() == 1) {
			// MPI trace file.
			elements = elements.get(0).getNestedXMLTagsWithName("Communicator");
			for (int i = 0; i < elements.size(); i++) {
				final String name = elements.get(i).getAttribute("name");

				MPICommunicator c = createCommunicator(name);
				readCommunicator(c, elements.get(i), descriptionInOut);
				descriptionInOut.addCommunicator(c);
			}

		}

		/* read datatypes */
		elements =  rootTag.getNestedXMLTagsWithName("Datatypes");
		if (elements.size() == 1) {
			// Datatype mapping is stored per rank:
			for(XMLTag rankXML: elements.get(0).getNestedXMLTagsWithName("Rank")){
				final int rank = Integer.parseInt(rankXML.getAttribute("name"));

				if(rankXML.getNestedXMLTags() == null)
					continue;

				// DatatypeID to datatype mapping
				HashMap<Long, Datatype> datatypeMapping = new HashMap<Long, Datatype>();

				for(XMLTag datatype: rankXML.getNestedXMLTags()){
					final Datatype newType = readDatatype(datatype, datatypeMapping);
					SimpleConsoleLogger.Debug(rank + ": found datatype " + newType);
					datatypeMapping.put(newType.getTid(), newType);
				}
				
				if(descriptionInOut.getDatatypeMap(rank) != null){
					throw new IllegalArgumentException("Error: type map already set for rank " + rank);
				}
				descriptionInOut.setDatatypeMap(rank, datatypeMapping);
			}		
		}
	}

	/**
	 * Factory method, create the appropriate datatype from XML 
	 * @param xml XMLRepresentation of datatype.
	 * @return
	 */
	private Datatype readDatatype(XMLTag xml, HashMap<Long, Datatype> datatypeMapping){
		final DatatypeEnum type = DatatypeEnum.valueOf(xml.getName());

		Datatype datatype = null;

		switch(type){		
		case CONTIGUOUS:{						
			long oldType = Long.parseLong(xml.getAttribute("oldType"));
			final Datatype old = datatypeMapping.get(oldType);
			int count = Integer.parseInt(xml.getAttribute("count"));
			
			datatype = new ContiguousDatatype(old, count);			
			
			break;
		}case NAMED:{
			String name = xml.getAttribute("name").replace("MPI_", "");
			
			datatype = NamedDatatype.valueOf(name);
			break;
		}case STRUCT:{
			StructDatatype struct = new StructDatatype(); 
			for(XMLTag child: xml.getNestedXMLTags()){
				long id = Long.parseLong(child.getAttribute("id"));
				int displacement = Integer.parseInt(child.getAttribute("displacement"));
				int blockLen = Integer.parseInt(child.getAttribute("blocklen"));
				
				final Datatype old = datatypeMapping.get(id);
				assert(old != null);
				struct.appendType(old, displacement, blockLen);
			}			
			datatype = struct;
			break;
		}case VECTOR:{
			int count = Integer.parseInt(xml.getAttribute("count"));
			int blocklength = Integer.parseInt(xml.getAttribute("blocklength"));
			int stride = Integer.parseInt(xml.getAttribute("stride"));
			long oldType = Long.parseLong(xml.getAttribute("oldType"));
			final Datatype old = datatypeMapping.get(oldType);
			
			datatype = new VectorDatatype(old, count, blocklength, stride);
			break;
		}default:
			throw new IllegalArgumentException("Datatype " + type + " not implemented, yet");
		}		


		final long tid = Long.parseLong(xml.getAttribute("id"));
		datatype.setTid(tid);
		return datatype;
	}

	protected MPICommunicator createCommunicator(String name){
		return new MPICommunicator(name);
	}
	
	private void readCommunicator(MPICommunicator comm, XMLTag xml, ProjectDescription desc){
		final LinkedList<XMLTag>  elements = xml.getNestedXMLTagsWithName("Rank");

		Iterator<XMLTag> it = elements.iterator();

		for(int i=0; i < elements.size(); i++){
			XMLTag tag = it.next();

			final String rank = tag.getAttribute("global");
			if (rank == null){
				throw new InvalidParameterException("Invalid XML, no global rank specified !");
			}
			final String localRank = tag.getAttribute("local");
			if (localRank == null){
				throw new InvalidParameterException("Invalid XML, no local rank specified !");
			}
			final String cid = tag.getAttribute("cid");
			if (rank == null){
				throw new InvalidParameterException("Invalid XML, no communicator ID specified !");
			}						

			try{
				final int cidi = Integer.parseInt(cid);
				final int ranki = Integer.parseInt(rank);
				final int locali = Integer.parseInt(localRank);
				comm.addRank(ranki, locali, cidi);				
			}catch(NumberFormatException e){
				throw new InvalidParameterException("Invalid XML, communicator attribute is not an integer");
			}	
		}
	}


	/**
	 * Recursivly read topology labels
	 * @param topologyTag
	 * @param labels
	 */
	private void parseTopologyLabel(XMLTag topologyTag, TopologyTypes labels){
		final XMLTag curLabel = topologyTag.getFirstNestedXMLTagWithName("Level");
		if(curLabel == null){
			return;
		}
		final String name = curLabel.getAttribute("type");		
		labels.addTypeForNextLevel(name);

		parseTopologyLabel(curLabel, labels);
	}


	private TopologyNode parseTopology(int depth, XMLTag xmlTopology, TopologyNode topoNode, ProjectDescription desc){		
		final LinkedList<XMLTag> children =  xmlTopology.getNestedXMLTagsWithName("Node");		
		
		for(XMLTag tag: children){
			final String childName = tag.getAttribute("name");

			String label = xmlTopology.getAttribute("type");
			if( label == null ){
				label = desc.getTopologyType(depth);
			}

			TopologyNode childNode = new TopologyNode(childName, topoNode, label);
			
			parseTopology(depth + 1, tag, childNode, desc);			
		}

		return topoNode;
	}



	public ArrayList<XMLTag> getUnparsedChildTags() {
		return rootTag.getNestedXMLTags();
	}
}
