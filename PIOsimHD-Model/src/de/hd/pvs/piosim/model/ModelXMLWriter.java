
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

import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;

import de.hd.pvs.piosim.model.annotations.ChildComponents;
import de.hd.pvs.piosim.model.components.superclasses.BasicComponent;
import de.hd.pvs.piosim.model.components.superclasses.IBasicComponent;
import de.hd.pvs.piosim.model.dynamicMapper.CommandType;
import de.hd.pvs.piosim.model.dynamicMapper.DynamicCommandClassMapper;
import de.hd.pvs.piosim.model.networkTopology.INetworkNode;
import de.hd.pvs.piosim.model.networkTopology.INetworkTopology;
import de.hd.pvs.piosim.model.networkTopology.ITopologyEdge;
import de.hd.pvs.piosim.model.program.Application;
import de.hd.pvs.piosim.model.program.ApplicationXMLWriter;

/**
 * This class serializes a components or a complete Model to XML.
 * The class <code>ModelXMLReader</code> reads XML and creates objects.
 * Uses reflection to read fields of the objects.
 *
 * @author Julian M. Kunkel
 */
public class ModelXMLWriter {
	AttributeAnnotationHandler commonAttributeHandler = new AttributeAnnotationHandler();

	/**
	 * Write XML of the model and all programs to a specified directory into XML files.
	 * According to the Attributes a application's file name is choosen.
	 *
	 * @param model Model to serialize
	 * @param dirname Folder to which all files are stored.
	 * @param projectFileName Relative path to the dirname.
	 * @throws Exception
	 */
	public void writeXMLFromProject(Model model, String dirname,
			String projectFileName) throws Exception{

		writeXMLFromModel(model, dirname + "/" + projectFileName);

		ApplicationXMLWriter writer = new ApplicationXMLWriter();

		for (String appAlias: model.getApplicationNameMap().keySet()){
			Application app = model.getApplicationNameMap().get(appAlias);
			writer.writeXMLFromApplication(app, dirname + "/" + app.getProjectFilename());
		}

	}

	/**
	 * Write XML only of the model and not the applications.
	 * @param model Model to serialize
	 * @param file Absolute path to the project filename
	 * @throws Exception
	 */
	public void writeXMLFromModel(Model model, String file) throws Exception{
		StringBuffer buff = new StringBuffer();
		createXMLFromModel(model, buff);
		writeToFile(file, buff);
	}

	/**
	 * Serialize only the model into the <code>StringBuffer</code>
	 * @param model
	 * @param sb The StringBuffer to which the XML data is written.
	 * @throws Exception
	 */
	public void createXMLFromModel(Model model, StringBuffer sb) throws Exception{
		sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?> \n<Project " +
				" xmlns=\"http://www.uni-heidelberg.de/PIOsimHD\" \n" +
				" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" \n" +
				" xsi:schemaLocation=\"http://www.uni-heidelberg.de/PIOsimHD\" " +
		">	\n\n");

		sb.append("<Templates>\n");
		createTemplateXML(model.templateManager, sb);
		sb.append("</Templates>\n\n");

		sb.append("<ApplicationList>\n");
		createApplicationMappingXML(model, sb);
		sb.append("</ApplicationList>\n\n");

		sb.append("<GlobalSettings>\n");
		createGlobalSettingsXML(model.globalSettings, sb);
		sb.append("</GlobalSettings>\n\n");

		sb.append("<ComponentList>\n\n");
		sb.append("<NodeList>\n");
		for (BasicComponent com : model.getNodes()) {
			createXMLFromComponent(com, sb);
		}
		sb.append("</NodeList>\n\n");

		sb.append("<NetworkEdgeList>\n");
		for (IBasicComponent com : model.getNetworkEdges()) {
			createXMLFromComponent(com, sb);
		}
		sb.append("</NetworkEdgeList>\n\n");

		sb.append("<NetworkNodeList>\n");
		for (IBasicComponent com : model.getNetworkNodes()) {
			createXMLFromComponent(com, sb);
		}
		sb.append("</NetworkNodeList>\n\n");

		// write out all topologies in sequence
		sb.append("<TopologyList>\n");
		for (INetworkTopology topology : model.getTopologies()) {
			sb.append("<Topology name=\"" + topology.getName() + "\">\n");

			for(INetworkNode node: topology.getGraph().keySet()){

				sb.append("\t<Node id=\"" + node.getIdentifier().getID() +  "\">\n");
				for(ITopologyEdge edge: topology.getEdges(node)){
					sb.append("\t\t<Edge id=\"" + edge.getIdentifier().getID() +
							"\" to=\"" + edge.getTarget().getIdentifier().getID() + "\"/>\n");
				}
				sb.append("\t</Node>\n");
			}

			sb.append("</Topology>\n");
		}
		sb.append("</TopologyList>\n\n");


		sb.append("</ComponentList>\n\n</Project>\n");
	}

	/**
	 * Serialize a given BasicComponent into the <code>StringBuffer</code>
	 * @param component The Component which should be serialized.
	 * @param sb The StringBuffer to which the XML data is written.
	 * @throws Exception
	 */
	public void createXMLFromComponent(IBasicComponent component, StringBuffer sb) throws Exception{
		String type =  component.getComponentType();

		sb.append("<" + type  +  " implementation=\"" + component.getClass().getCanonicalName() +"\"");
		// next we invoke the createComponentAttributes method for the object hierarchy
		Class<?> classIterate = component.getClass();
		while(classIterate != Object.class) {
			try{
				Method m = this.getClass().getDeclaredMethod("createComponentAttributes",
						new Class[]{classIterate, StringBuffer.class});
				m.invoke(this, new Object[]{component, sb});
			}catch(Exception e){
			}

			classIterate = classIterate.getSuperclass();
		}
		//////

		StringBuffer attributes = new StringBuffer();

		commonAttributeHandler.writeSimpleAttributeXML(component, attributes, sb);

		sb.append(">\n");

		sb.append(attributes);

		// next we invoke the createComponentTags method for the object hierarchy
		classIterate = component.getClass();
		while(classIterate != Object.class) {
			try{
				Method m = this.getClass().getDeclaredMethod("createComponentTags",
						new Class[]{classIterate, StringBuffer.class});
				m.invoke(this, new Object[]{component, sb});
			}catch(Exception e){
			}

			classIterate = classIterate.getSuperclass();
		}
		/////////////////////////////////////

		// Next create subcomponents
		createSubComponentXML(component, sb);

		sb.append("</" + type + ">\n");
	}

	///////////////////////////////////////////////////////////////////////////////

	/**
	 * Writes the content of a StringBuffer to a file
	 */
	private void writeToFile(String file, StringBuffer buff) throws IOException{
		FileWriter f = new FileWriter(file);
		f.write(buff.toString());
		f.close();
	}

	/**
	 * Create the XML for the templates.
	 * @param manager
	 * @param buff
	 * @throws Exception
	 */
	private void createTemplateXML(TemplateManager manager, StringBuffer buff) throws Exception{
		for (BasicComponent com :  manager.getTemplates()){
			createXMLFromComponent(com, buff);
		}
	}

	/**
	 * Write the application mapping to the StringBuffer.
	 *
	 * @param model
	 * @param buff
	 * @throws Exception
	 */
	private void createApplicationMappingXML(Model model, StringBuffer buff) throws Exception{
		HashMap<String, Application> mapping = model.applicationNameMap;

		for(String alias: mapping.keySet()){
			Application app = mapping.get(alias);
			buff.append("<Application alias=\"" + alias + "\" file=\"" + app.getProjectFilename() + "\"/>\n");
		}
	}

	/**
	 * Write the GlobalSettings to the StringBuffer.
	 * @param settings
	 * @param buff
	 * @throws Exception
	 */
	private void createGlobalSettingsXML(GlobalSettings settings, StringBuffer buff) throws Exception{
		commonAttributeHandler.writeSimpleAttributeXML(settings, buff, null);
		for(CommandType cm: DynamicCommandClassMapper.getAvailableCommands()){
			if(settings.getClientFunctionImplementation(cm) != null){
				buff.append("<ClientMethod name=\"" + cm.toString() + "\">" +
						settings.getClientFunctionImplementation(cm) + "</ClientMethod>\n");
			}
		}
	}


	/**
	 * Create the nested XML for all the contained child components.
	 * Each field annotated with the <code>ChildComponents</code> Annotation is seralized to XML.
	 * Collections or Simple Object references are followed.
	 *
	 * @param component The component which child components' XML should be created.
	 * @param buff The StringBuffer
	 * @throws Exception
	 */
	private void createSubComponentXML(IBasicComponent component, StringBuffer buff) throws Exception{
		Class<?> classIterate = component.getClass();

		while(classIterate != Object.class) {
			Field [] fields = classIterate.getDeclaredFields();
			for (Field field : fields) {
				if( ! field.isAnnotationPresent(ChildComponents.class))
					continue;

				//ChildComponents annotation = field.getAnnotation(ChildComponents.class);

				field.setAccessible(true);
				Object value = field.get(component);
				field.setAccessible(false);

				if(value == null)
					continue;

				buff.append("<" + field.getName().toUpperCase()  +  ">\n");

				// if it is a collection serialize all contained elements
				if(Collection.class.isAssignableFrom(value.getClass()) ){
					Collection<BasicComponent> coll = (Collection<BasicComponent>)  value;
					for(BasicComponent e: coll){
						createXMLFromComponent(e, buff);
					}
				}else{ // single object
					createXMLFromComponent((BasicComponent) value, buff);
				}

				buff.append("</" + field.getName().toUpperCase()  +  ">\n");

			}

			classIterate = classIterate.getSuperclass();
		}
	}

	//////////////////////////////////////////////////////////////////////////////

	// The following methods are called via reflection.
	// For each class special serializers could be used.

	private void createComponentAttributes(BasicComponent component, StringBuffer sb) throws Exception{
		if(component.getIdentifier().getID() != null)
			sb.append(" id=\"" + component.getIdentifier().getID() +	"\"");

		if(component.getIdentifier().getName() != null)
			sb.append(" name=\"" + component.getIdentifier().getName() + "\"");
	}
	//////////////////////////////////////////////////////////////////////////////
}
