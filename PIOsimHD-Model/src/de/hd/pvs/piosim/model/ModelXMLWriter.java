
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
import java.util.HashMap;

import de.hd.pvs.piosim.model.inputOutput.IORedirection;
import de.hd.pvs.piosim.model.interfaces.IDynamicImplementationObject;
import de.hd.pvs.piosim.model.interfaces.IDynamicModelComponent;
import de.hd.pvs.piosim.model.networkTopology.INetworkEdge;
import de.hd.pvs.piosim.model.networkTopology.INetworkNode;
import de.hd.pvs.piosim.model.networkTopology.INetworkTopology;
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
	final SerializationHandler serializationHandler = new SerializationHandler();

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

		serializationHandler.writeXML("GlobalSettings", model.globalSettings, sb);

		sb.append("<ComponentList>\n\n");
		sb.append("<NodeList>\n");
		for (IDynamicImplementationObject com : model.getNodes()) {
			serializationHandler.createXMLFromInstance(com, sb);
		}
		sb.append("</NodeList>\n\n");

		sb.append("<NetworkEdgeList>\n");
		for (IDynamicImplementationObject com : model.getNetworkEdges()) {
			serializationHandler.createXMLFromInstance(com, sb);
		}
		sb.append("</NetworkEdgeList>\n\n");

		sb.append("<NetworkNodeList>\n");
		for (IDynamicImplementationObject com : model.getNetworkNodes()) {
			serializationHandler.createXMLFromInstance(com, sb);
		}
		sb.append("</NetworkNodeList>\n\n");

		sb.append("</ComponentList>\n\n");

		// write out all topologies in sequence
		sb.append("<TopologyList>\n");
		for (INetworkTopology topology : model.getTopologies()) {
			sb.append("<Topology ");

			serializationHandler.writeXMLBody(topology, sb);

			for(INetworkNode node: topology.getSourceGraph().keySet()){

				sb.append("\t<Node id=\"" + node.getIdentifier().getID() +  "\">\n");
				for(INetworkEdge edge: topology.getEdges(node)){
					sb.append("\t\t<Edge id=\"" + edge.getIdentifier().getID() +
							"\" to=\"" + topology.getEdgeTarget(edge).getIdentifier().getID()
							+ "\"/>\n");
				}
				sb.append("\t</Node>\n");
			}

			sb.append("</Topology>\n");
		}
		sb.append("</TopologyList>\n\n");

		// write out I/O redirection layers:
		sb.append("<IORedirectionList>\n");
		for(IORedirection redirect: model.getIORedirectionLayers()){
			sb.append("<IORedirect default=\"" + redirect.getDefaultRouteID() + "\">\n");
			for(Integer server : redirect.getRedirects().keySet()){
				sb.append("<Srv id=\"" + server + "\" via=\""+  redirect.getRedirects().get(server) + "\"/>\n");
			}
			for(Integer component : redirect.getModifyingComponentIDs()){
				sb.append("<Component id=\"" + component +"\"/>\n");
			}
			sb.append("</IORedirect>\n");
		}
		sb.append("</IORedirectionList>\n");

		// end top XML Tag
		sb.append("\n</Project>\n");
	}

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
		for (IDynamicModelComponent com :  manager.getTemplates()){
			serializationHandler.createXMLFromInstance(com, buff);
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
}
