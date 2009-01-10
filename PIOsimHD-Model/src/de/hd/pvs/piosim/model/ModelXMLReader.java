
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import de.hd.pvs.piosim.model.annotations.ChildComponents;
import de.hd.pvs.piosim.model.components.superclasses.BasicComponent;
import de.hd.pvs.piosim.model.components.superclasses.OneConnectionComponent;
import de.hd.pvs.piosim.model.dynamicMapper.DynamicCommandClassMapper;
import de.hd.pvs.piosim.model.dynamicMapper.DynamicModelClassMapper;
import de.hd.pvs.piosim.model.dynamicMapper.DynamicCommandClassMapper.CommandType;
import de.hd.pvs.piosim.model.logging.ConsoleLogger;
import de.hd.pvs.piosim.model.program.Application;
import de.hd.pvs.piosim.model.program.ApplicationXMLReader;
import de.hd.pvs.piosim.model.util.XMLutil;


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
		File file = new File(filename);
		
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document document = builder.parse(file);
		Element projectNode = document.getDocumentElement();
		projectNode.normalize();		
		
		
		return readProjectXML(projectNode, file.getParent(), fixedFileAppMapping);		
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
	public Model readProjectXML(Element projectNode, String applicationDirName, HashMap<String, String> fixedFileAppMapping) throws Exception {
		Model model = new Model();
		
		loadApplications(model, XMLutil.getFirstElementByTag(
				projectNode, "ApplicationList"), applicationDirName);
		
		// load fixed file mapping:
		if(fixedFileAppMapping != null) {
			for (String alias: fixedFileAppMapping.keySet()) {
				String filename = fixedFileAppMapping.get(alias);
				
				loadSingleApp(model, filename, alias);
			}
		}
		
		readTemplates(model, XMLutil.getFirstElementByTag(projectNode,	"Templates"));
		
		readGlobalSettings(model, XMLutil.getFirstElementByTag(projectNode, "GlobalSettings"));
		
		ConsoleLogger.getInstance().debug(this, "Creating Components");
		createAllComponents(model, XMLutil.getFirstElementByTag(projectNode, "ComponentList"));
		
		ConsoleLogger.getInstance().debug(this, "Connecting Components");
		try {
			connectComponents(model, XMLutil.getFirstElementByTag(projectNode,
			"ComponentList"));
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
	 * @param isCloneOfTemplate If this object is a clone of a template then the name of the template must be set.
	 * @return
	 * @throws Exception
	 */
	public BasicComponent createComponentFromXML(Element xml, boolean isCloneOfTemplate) throws Exception{
		String implementation = XMLutil.getAttributeText(xml, "implementation");
		
		ConsoleLogger.getInstance().debug(this, "will create: " + implementation);
		
		// use reflection to instantiate the object 
		Constructor<BasicComponent> ct = ((Class<BasicComponent>) Class.forName(implementation)).getConstructor();
		
		if (ct == null){
			throw new IllegalArgumentException("Constructor for the implementation " + implementation + " not found");
		}
		BasicComponent component;
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
						new Class[]{Element.class, classIterate});				
				m.invoke(this, new Object[]{xml, component});
			}catch(NoSuchMethodException e){
			}
			
			classIterate = classIterate.getSuperclass();
		}		
		
		if (isCloneOfTemplate){
			// change template and names...
			component.setTemplate(component.getName());
			if(component.getName() != null)
				component.setName(component.getName() + "_1");
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
	private void readGlobalSettings(Model model, Element xml) throws Exception {
		GlobalSettings global = model.globalSettings;
		commonAttributeHandler.readSimpleAttributes(xml, global);
		
		ArrayList<Element> clientMeth = XMLutil.getElementsByTag(xml, "ClientMethod");
		if(clientMeth != null){
			for(Element n: clientMeth){
				String smethod = XMLutil.getAttributeText(n, "name");
				CommandType method =  DynamicCommandClassMapper.getCommandImplementationGroup(smethod);
				global.setClientFunctionImplementation(method, n.getTextContent());
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
	private void connectComponents(Model model, Element componentList) throws Exception {
		/* connect components */
		ArrayList<Element> list;
		for (String type : DynamicModelClassMapper.getAvailableModelTypes()) {
			Element element = XMLutil.getFirstElementByTag(componentList,
					type + "List");
			
			list = XMLutil.getChildElements(element);
			
			for (Element sub : list) {
				
				ArrayList<Element> subList = XMLutil.getChildElements(sub);
					
				for (Element e : subList) {
					ArrayList<Element> connectedTo = XMLutil.getChildElements(e);
					
					for (Element el : connectedTo) {					
						Element pConnection = XMLutil.getFirstElementByTag(el, "connection");
						
						if(pConnection == null)
							continue;
						
						int componentID = Integer.parseInt( el.getAttribute("id") );
						
						Element econnection = XMLutil.getFirstElementByTag(pConnection, "CONNECTION");						

						assert(econnection != null);
												
						if (!econnection.hasAttribute("to")) {
							System.out.println("No connected-to attribute found in component with id: "	+ componentID  + " parent " + sub.getAttribute("id"));
							continue;
						}
						
						
						if (connectedTo.size() != 0) {
							int connectedToComponentID = Integer.parseInt( econnection.getAttribute("to") );
							
							OneConnectionComponent sourceComponent = null;	
							
							OneConnectionComponent targetComponent = null;
							
							try{
								sourceComponent = (OneConnectionComponent) model.getCidCMap().get(componentID);
							}catch(ClassCastException ex){
								throw new IllegalArgumentException("Linked object must be a OneConnectionComponent, link between \"" +
										componentID + " ->  " +	connectedToComponentID);
							}
							
							try{
								targetComponent = (OneConnectionComponent) model.getCidCMap().get(connectedToComponentID);
							}catch(ClassCastException ex){
								throw new IllegalArgumentException("Linked object must be a OneConnectionComponent, link between \"" +
										componentID + " ->  " +	connectedToComponentID);
							}
							
							if (sourceComponent == null || targetComponent == null) {
								throw new IllegalArgumentException(
										"Invalid connection between components \""
										+ componentID + "\" to \""
										+ connectedToComponentID + "\"");
							}
							
							ConsoleLogger.getInstance().debug(this, "Connecting components: " + componentID + " to " + connectedToComponentID );
							
							sourceComponent.setConnectedComponent(targetComponent);
						}
					}
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
	private void loadApplications(Model model, Element element, String dirname)
	throws Exception {
		ArrayList<Element> list = XMLutil.getElementsByTag(element, "Application");
		for (Element e : list) {
			
			if (!e.hasAttribute("alias")) {
				throw new IllegalArgumentException(
				"No name attribute found in application");
			}
			if (!e.hasAttribute("file")) {
				throw new IllegalArgumentException(
				"No file attribute found in application");
			}
			
			String alias = e.getAttribute("alias");
			
			String file = e.getAttribute("file");
			
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
		
		Application newApp = appXMLReader.parseApplication(filename);
		
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
	private void readTemplates(Model model, Element templateRoot)
	throws Exception {
		ArrayList<Element> elements = XMLutil.getChildElements(templateRoot);
		
		for(Element e: elements){				
			BasicComponent component = createComponentFromXML(e, false);
			model.templateManager.addTemplate(component);				
		}
	}
	
	/**
	 * Read all model components as specified in the XML file.
	 * 
	 * @param model
	 * @param xml
	 * @throws Exception
	 */
	private void createAllComponents(Model model, Element xml) throws Exception {
		for (String type : DynamicModelClassMapper.getAvailableModelTypes()) {		
			// the elements are contained in the tag XXList 
			Element element = XMLutil.getFirstElementByTag(xml, type + "List");
			ArrayList<Element> list = XMLutil.getChildElements(element);
			for (Element e : list) {
				BasicComponent newComponent = createComponentFromXML(e, false);
				model.addComponent(newComponent);		
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
	private void readComponentDetailsFromXML(Element xml, BasicComponent dummy) throws Exception{		
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
	private void readChildComponents(Element xml, BasicComponent comp, boolean isCloneOfTemplate) throws Exception{
		// Walk through the object hierarchy
		Class<?> classIterate = comp.getClass();	
		
		while(classIterate != Object.class) {
			Field [] fields = classIterate.getDeclaredFields();		
			for (Field field : fields) {
				if( ! field.isAnnotationPresent(ChildComponents.class))
					continue;
				
				ChildComponents annotation = field.getAnnotation(ChildComponents.class);
				
				Element parentNode = XMLutil.getFirstElementByTag(xml, field.getName().toUpperCase());
				
				if(parentNode == null) {
					// not set!
					continue;
				}
				
				field.setAccessible(true);
				
				ArrayList<Element> elements = XMLutil.getChildElements(parentNode);
				
				// create child components
				for(Element e: elements){
					BasicComponent newComponent = createComponentFromXML(e, isCloneOfTemplate);
					
					// now set the child's parent components if needed:
					newComponent.setParentComponent(comp);
					
					if(Collection.class.isAssignableFrom(field.getType()) ){
						((Collection<BasicComponent>) field.get(comp)).add(newComponent);
					}else{						
						field.set(comp, newComponent);
					}
				}
				
				
				field.setAccessible(false);				
			}
			
			classIterate = classIterate.getSuperclass();
		}
	}
}
