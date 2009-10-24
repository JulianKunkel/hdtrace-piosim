
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

package de.hd.pvs.piosim.model;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import de.hd.pvs.TraceFormat.xml.XMLReaderToRAM;
import de.hd.pvs.TraceFormat.xml.XMLTag;
import de.hd.pvs.piosim.model.components.superclasses.IBasicComponent;
import de.hd.pvs.piosim.model.dynamicMapper.DynamicModelClassMapper;
import de.hd.pvs.piosim.model.logging.ConsoleLogger;
import de.hd.pvs.piosim.model.networkTopology.INetworkEdge;
import de.hd.pvs.piosim.model.networkTopology.INetworkNode;
import de.hd.pvs.piosim.model.networkTopology.NetworkTopology;
import de.hd.pvs.piosim.model.program.Application;
import de.hd.pvs.piosim.model.program.ApplicationXMLReader;


/**
 * This class reads XML and creates valid objects or a complete model from it.
 * If XML does not contain information for a specific tag/attribute then the default values
 * as set in the component are used.
 * Uses reflection to set fields of the objects.
 *
 * @author Julian M. Kunkel
 *
 */
public class ModelXMLReader {
	final SerializationHandler serializationHandler = new SerializationHandler();

	/**
	 * Should all commands be read on demand or at beginning (i.e. for further modification?)
	 */
	private boolean readCompleteProgramIntoMemory = false;

	/**
	 * The function contains all routines to read the XML description files and
	 * transforms them into valid Java Classes representing the data.
	 */
	public Model parseProjectXML(String filename) throws Exception {
		return parseProjectXML(filename, null);
	}

	/**
	 * Read a project from the file and the required program (== applications) XML from the same directory.
	 *
	 * @param filename The project XML file
	 * @param fixedFileAppMapping The application to file mapping can be changed by this map
	 * 				 (application-alias => filename).
	 * @return Model from XML
	 * @throws Exception
	 */
	public Model parseProjectXML(String filename, HashMap<String, String> fixedFileAppMapping) throws Exception{

		XMLReaderToRAM file = new XMLReaderToRAM();
		XMLTag rootTag = file.readXML(filename);

		return readProjectXML(rootTag, (new File(filename)).getParent(), fixedFileAppMapping);
	}

	/**
	 * Reads project XML from the projectNode (DOM) and loads the application XML files from the
	 * given applicationDirName
	 *
	 * @param projectNode
	 * @param applicationDirName
	 * @param fixedFileAppMapping The application to file mapping can be changed by this map
	 * 				 (application-alias => filename).
	 * @return
	 * @throws Exception
	 */
	public Model readProjectXML(XMLTag projectNode, String applicationDirName, HashMap<String, String> fixedFileAppMapping) throws Exception {

		// TODO: migrate to model builder?
		ModelBuilder mb = new ModelBuilder();

		Model model = mb.getModel();

		loadApplications(model, projectNode.getFirstNestedXMLTagWithName("ApplicationList"),
				applicationDirName);

		// load fixed file mapping:
		if(fixedFileAppMapping != null) {
			for (String alias: fixedFileAppMapping.keySet()) {
				String filename = fixedFileAppMapping.get(alias);

				loadSingleApp(model, filename, alias);
			}
		}

		readTemplates(model, projectNode.getFirstNestedXMLTagWithName("Templates"));

		serializationHandler.readXML(projectNode.getFirstNestedXMLTagWithName("GlobalSettings"), model.getGlobalSettings());

		ConsoleLogger.getInstance().debug(this, "Creating Components");
		createAllComponents(model, projectNode.getFirstNestedXMLTagWithName("ComponentList"));

		ConsoleLogger.getInstance().debug(this, "Connecting Components");
		try {
			loadTopology(model, projectNode.getFirstNestedXMLTagWithName("ComponentList"));
		} catch (Exception e) {
			System.err.println("Available model: " + model);

			throw e;
		}

		return model;
	}

	/**
	 * Connect all components based on XML, i.e. NIC's and PORT's. Therefore the Object have to be
	 * created earlier. Therefore, the method must be invoked once all Objects are created.
	 *
	 * @param model
	 * @param componentList
	 * @throws Exception
	 */
	private void loadTopology(Model model, XMLTag componentList) throws Exception {
		/* connect components */
		final XMLTag element = componentList.getFirstNestedXMLTagWithName("TopologyList");

		for(XMLTag topologyXML: element.getNestedXMLTagsWithName("Topology")){

			final NetworkTopology topology = new NetworkTopology();

			model.addTopology(topology);

			serializationHandler.readXML(topologyXML, topology);

			final List<XMLTag> nodeList = topologyXML.getNestedXMLTagsWithName("Node");

			for (XMLTag node : nodeList) {

				final int srcID = Integer.parseInt( node.getAttribute("id") );

				for (XMLTag edgexml : node.getNestedXMLTagsWithName("Edge")) {
					final int edgeID = Integer.parseInt( edgexml.getAttribute("id") );
					final int targetID =  Integer.parseInt( edgexml.getAttribute("to") );

					INetworkNode srcNode;
					INetworkNode targetNode;
					INetworkEdge edge;

					try{
						srcNode = (INetworkNode) model.getCidCMap().get(srcID);
						targetNode = (INetworkNode) model.getCidCMap().get(targetID);
						edge = (INetworkEdge) model.getCidCMap().get(edgeID);
					}catch(ClassCastException ex){
						throw new IllegalArgumentException("Topology link error from \"" +
								srcID + " to " + targetID + " via " + edgeID, ex);
					}

					topology.addEdge(srcNode, edge, targetNode);
				}
			}
		}
	}


	/**
	 * Load all given applications from the directory.
	 *
	 * @param model
	 * @param element The root of the Model XML.
	 * @param dirname
	 * @throws Exception
	 */
	private void loadApplications(Model model, XMLTag element, String dirname)
	throws Exception {
		LinkedList<XMLTag> list = element.getNestedXMLTagsWithName("Application");
		for (XMLTag e : list) {
			final String alias = e.getAttribute("alias");

			final String file = e.getAttribute("file");

			if (alias == null) {
				throw new IllegalArgumentException(
				"No name attribute found in application");
			}
			if (file == null) {
				throw new IllegalArgumentException(
				"No file attribute found in application");
			}


			ConsoleLogger.getInstance().debug(this, "Parsing application: " + alias + " " + dirname + "/" + file );

			loadSingleApp(model, dirname + "/" + file, alias);
		}
	}

	/**
	 * Load a single application XML from an XML file and put it into the model ApplicationNameMap.
	 *
	 * @param model
	 * @param filename The absolute name of the application XML file.
	 * @param alias
	 * @throws Exception
	 */
	private void loadSingleApp(Model model, String filename, String alias) throws Exception{
		ApplicationXMLReader appXMLReader = new ApplicationXMLReader();

		Application newApp = appXMLReader.parseApplication(filename, readCompleteProgramIntoMemory);

		newApp.setAlias(alias);

		model.applicationNameMap.put(alias, newApp);
	}

	/**
	 * Read the Templates from the XML.
	 *
	 * @param model
	 * @param templateRoot The root of the Model XML.
	 * @throws Exception
	 */
	private void readTemplates(Model model, XMLTag templateRoot)
	throws Exception {
		List<XMLTag> elements = templateRoot.getNestedXMLTags();

		for(XMLTag e: elements){
			IBasicComponent component = (IBasicComponent) serializationHandler.createDynamicObjectFromXML(e);
			model.templateManager.addTemplate(component, component.getName());
		}
	}

	/**
	 * Read all model components as specified in the XML file.
	 *
	 * @param model
	 * @param xml
	 * @throws Exception
	 */
	private void createAllComponents(Model model, XMLTag xml) throws Exception {
		for (String type : DynamicModelClassMapper.getAvailableModelTypes()) {
			// the elements are contained in the tag XXList
			XMLTag element = xml.getFirstNestedXMLTagWithName(type + "List");

			if(element == null){
				System.err.println("No list available for component type: " + type);
				continue;
			}

			List<XMLTag>  list = element.getNestedXMLTags();
			for (XMLTag e : list) {
				IBasicComponent newComponent = (IBasicComponent) serializationHandler.createDynamicObjectFromXML(e);
				try{
					model.addComponent(newComponent);

				}catch(IllegalArgumentException error){
					throw new IllegalArgumentException("Searching for " + type + "List. Parsed: " + newComponent, error);
				}
			}
		}
	}

	/**
	 * Should all commands be read on demand or at beginning (i.e. for further modification?)
	 */
	public void setReadCompleteProgramIntoMemory(boolean readCompleteProgramIntoMemory) {
		this.readCompleteProgramIntoMemory = readCompleteProgramIntoMemory;
	}

	public boolean isReadCompleteProgramIntoMemory() {
		return readCompleteProgramIntoMemory;
	}
}
