
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
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import de.hd.pvs.TraceFormat.xml.XMLReaderToRAM;
import de.hd.pvs.TraceFormat.xml.XMLTag;
import de.hd.pvs.piosim.model.annotations.ChildComponents;
import de.hd.pvs.piosim.model.components.superclasses.BasicComponent;
import de.hd.pvs.piosim.model.components.superclasses.IBasicComponent;
import de.hd.pvs.piosim.model.dynamicMapper.CommandType;
import de.hd.pvs.piosim.model.dynamicMapper.DynamicModelClassMapper;
import de.hd.pvs.piosim.model.dynamicMapper.DynamicTraceEntryToCommandMapper;
import de.hd.pvs.piosim.model.interfaces.IChildObject;
import de.hd.pvs.piosim.model.interfaces.ISerializableObject;
import de.hd.pvs.piosim.model.interfaces.ISerializableTemplateObject;
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
	AttributeAnnotationHandler commonAttributeHandler = new AttributeAnnotationHandler();

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

		readGlobalSettings(model, projectNode.getFirstNestedXMLTagWithName("GlobalSettings"));

		ConsoleLogger.getInstance().debug(this, "Creating Components");
		createAllComponents(model, projectNode.getFirstNestedXMLTagWithName("ComponentList"));

		ConsoleLogger.getInstance().debug(this, "Connecting Components");
		try {
			loadTopology(model, projectNode.getFirstNestedXMLTagWithName("ComponentList"));
		} catch (Exception e) {
			e.printStackTrace();

			System.err.println("Available model: " + model);
		}

		return model;
	}

	/**
	 * This method parses the XML and creates a single component of the type as specified in the XML
	 *
	 * @param model
	 * @param xml The root node containing the element and all sub-elements
	 * @throws Exception
	 */
	public ISerializableObject createComponentFromXML(XMLTag xml, boolean isCloneOfTemplate) throws Exception{
		String implementation = xml.getAttribute("implementation");

		ConsoleLogger.getInstance().debug(this, "will create: " + implementation);

		// use reflection to instantiate the object
		Constructor<ISerializableObject> ct = ((Class<ISerializableObject>) Class.forName(implementation)).getConstructor();

		if (ct == null){
			throw new IllegalArgumentException("Constructor for the implementation " + implementation + " not found");
		}
		ISerializableObject component;
		try{
			component = ct.newInstance();
		}catch(InstantiationException e){
			throw new IllegalArgumentException("Constructor for the implementation " + implementation + " invalid");
		}

		commonAttributeHandler.readSimpleAttributes(xml, component);

		readChildComponents(xml, component, isCloneOfTemplate);


		// next we invoke the readComponentDetailsFromXML method for the object hierarchy
		Class<?> classIterate = component.getClass();
		while(classIterate != Object.class) {
			try{
				Method m = this.getClass().getDeclaredMethod("readComponentDetailsFromXML",
						new Class[]{XMLTag.class, classIterate});
				m.invoke(this, new Object[]{xml, component});
			}catch(NoSuchMethodException e){
			}

			classIterate = classIterate.getSuperclass();
		}

		if (isCloneOfTemplate){
			ISerializableTemplateObject namedTemplate = (ISerializableTemplateObject) component;

			// change template and names...
			namedTemplate.setTemplate(namedTemplate.getName());
			if(namedTemplate.getName() != null)
				namedTemplate.setName(namedTemplate.getName() + "_01");
		}

		return component;
	}



	/**
	 * Read the XML for the GlobalSettings and create a valid <code>GlobalSetting</code> object.
	 *
	 * @param model
	 * @param xml Root node containing the GlobalSetting XML.
	 * @throws Exception
	 */
	private void readGlobalSettings(Model model, XMLTag xml) throws Exception {
		GlobalSettings global = model.globalSettings;
		commonAttributeHandler.readSimpleAttributes(xml, global);

		LinkedList<XMLTag> clientMeth = xml.getNestedXMLTagsWithName("ClientMethod");
		if(clientMeth != null){
			for(XMLTag n: clientMeth){
				String smethod = n.getAttribute("name");
				CommandType method =  DynamicTraceEntryToCommandMapper.getCommandForTraceEntryName(smethod);
				global.setClientFunctionImplementation(method, n.getContainedText());
			}
		}
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

			commonAttributeHandler.readSimpleAttributes(topologyXML, topology);
			readChildComponents(topologyXML, topology, false);

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
			IBasicComponent component = (IBasicComponent) createComponentFromXML(e, false);
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
				IBasicComponent newComponent = (IBasicComponent) createComponentFromXML(e, false);
				try{
					model.addComponent(newComponent);
				}catch(IllegalArgumentException error){
					throw new IllegalArgumentException("Searching for " + type + "List. Parsed: " + newComponent, error);
				}
			}
		}
	}

	////////////////////////////////////////////////////////////////////////////////

	// the following methods are called by reflection during creation of a object and allow
	// to specify special actions.

	/**
	 * Dummy wrapper is called if no specific method can be found
	 *
	 * @param xml
	 * @param dummy
	 */
	private void readComponentDetailsFromXML(XMLTag xml, BasicComponent dummy) throws Exception{
		commonAttributeHandler.readSimpleAttributes(xml, dummy.getIdentifier());
	}

	////////////////////////////////////////////////////////////////////////////////


	/**
	 * Read all child components of a component from the XML based on the <code>ChildComponents</code>
	 * annotation.
	 *
	 * @param xml The node containing the object.
	 * @param comp
	 * @param isCloneOfTemplate Is this object a clone of a template
	 * @throws Exception
	 */
	private void readChildComponents(XMLTag xml, ISerializableObject comp, boolean isCloneOfTemplate) throws Exception{
		// Walk through the object hierarchy
		Class<?> classIterate = comp.getClass();

		while(classIterate != Object.class) {
			Field [] fields = classIterate.getDeclaredFields();
			for (Field field : fields) {
				if( ! field.isAnnotationPresent(ChildComponents.class))
					continue;
				// check if a default class shall be loaded.
				final ChildComponents annotation = field.getAnnotation(ChildComponents.class);

				final XMLTag parentNode = xml.getFirstNestedXMLTagWithName(field.getName().toUpperCase());

				if(parentNode == null) {
					// not set!
					continue;
				}

				field.setAccessible(true);

				List<XMLTag>  elements = parentNode.getNestedXMLTags();
				if(elements != null){
					// create child components
					for(XMLTag e: elements){
						ISerializableObject newComponent = createComponentFromXML(e, isCloneOfTemplate);

						if(IChildObject.class.isAssignableFrom(newComponent.getClass())){
							//now set the child's parent components if needed:
							((IChildObject) newComponent).setParentComponent((IBasicComponent) comp);
						}

						if(Collection.class.isAssignableFrom(field.getType()) ){
							((Collection<ISerializableObject>) field.get(comp)).add(newComponent);
						}else{
							field.set(comp, newComponent);
						}
					}
				}


				field.setAccessible(false);
			}

			classIterate = classIterate.getSuperclass();
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
