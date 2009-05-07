
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

package de.hd.pvs.TraceFormat.project;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import de.hd.pvs.TraceFormat.topology.TopologyNode;
import de.hd.pvs.TraceFormat.xml.XMLHelper;
import de.hd.pvs.TraceFormat.xml.XMLTag;


/**
 * This class is used to write a project description to an XML file.
 *  
 * @author Julian M. Kunkel
 */
public class ProjectDescriptionXMLWriter {
	
	/**
	 * Convert a project to an XML file.  
	 * 
	 * @param desc
	 * @param unparsedTags if provided these tags are written below the root tag.
	 * @throws IOException
	 */
	public void writeXMLToProjectFile(ProjectDescription desc, List<XMLTag> unparsedTags) throws IOException{
		StringBuffer buff = new StringBuffer(1000);
		
		buff.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		
		buff.append("<Application name=\"" + desc.getApplicationName() + "\">\n");
		buff.append("<Description>" + desc.getDescription() +  "</Description>\n");

		// topology labels:
		buff.append("<Topology>\n");
		for(String label: desc.getTopologyTypes().getTypes()){
			 buff.append("<Level type=\"" + XMLHelper.escapeAttribute(label) + "\">\n");
		}
		for(String label: desc.getTopologyTypes().getTypes()){
			buff.append("</Level>\n");
		}
		
		if(desc.getTopologyRoot() != null){
			for(TopologyNode topo: desc.getTopologyRoot().getChildElements().values()){
				writeTopologyRecursive(buff, desc, topo);
			}
		}
		
		buff.append("</Topology>\n");
		
		buff.append("<ExternalStatistics>\n");
		
		for(final String groupName: desc.getStatisticsGroupNames()){
			buff.append("<" + groupName + "/>");			
		}
		
		buff.append("</ExternalStatistics>\n");

		// append all the tags which are not parsed:
		if(unparsedTags != null){
			for(XMLTag tag: unparsedTags)
				buff.append(tag +"\n");
		}
		
		buff.append("</Application>\n");
		writeToFile(desc.getAbsoluteFilenameOfProject(), buff);
	}
	
	private void writeTopologyRecursive(StringBuffer buff, ProjectDescription desc, TopologyNode topologyInternalLevel){
		buff.append("<Node name='" + 
				XMLHelper.escapeAttribute(topologyInternalLevel.getName()) + "'");
		
		if(topologyInternalLevel.getType() != null){
			buff.append(" type=\"" + XMLHelper.escapeAttribute(topologyInternalLevel.getType()) + "\"");
		}
			
		
		if(! topologyInternalLevel.isLeaf()){			
			buff.append(">\n");
			for(TopologyNode child: topologyInternalLevel.getChildElements().values()){
				writeTopologyRecursive(buff, desc, child);
			}
			buff.append("</Node>\n");
		}else{ // add close tag ...
			buff.append("/>\n");
		}
	}
	
	/**
	 * Writes the content of a StringBuffer to a file
	 */
	private void writeToFile(String file, StringBuffer buff) throws IOException{
		FileWriter f = new FileWriter(file);
		f.write(buff.toString());
		f.close();	
	}

}
