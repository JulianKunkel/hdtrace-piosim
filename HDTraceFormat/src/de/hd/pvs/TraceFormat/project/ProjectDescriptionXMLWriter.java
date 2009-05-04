
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

import de.hd.pvs.TraceFormat.statistics.StatisticDescription;
import de.hd.pvs.TraceFormat.statistics.StatisticsGroupDescription;
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
		
		buff.append("<Application name=\"" + desc.getApplicationName() + "\" processCount=\"" + desc.getProcessCount() + "\">\n");

		// topology labels:
		buff.append("<Topology>\n");
		for(String label: desc.getTopologyLabels().getLabels()){
			 buff.append("<Level name=\"" + XMLHelper.escapeAttribute(label) + "\">\n");
		}
		for(String label: desc.getTopologyLabels().getLabels()){
			buff.append("</Level>\n");
		}
		
		if(desc.getTopologyRoot() != null){
			for(TopologyNode topo: desc.getTopologyRoot().getChildElements().values()){
				writeTopologyRecursive(buff, topo);
			}
		}
		
		buff.append("</Topology>\n");
		
		buff.append("<ExternalStatistics>\n");
		
		for(String groupName: desc.getExternalStatisticGroupNames()){
			final StatisticsGroupDescription group = desc.getExternalStatisticsGroup(groupName);
			buff.append("<" + group.getName() + " timestampDatatype=\"" + group.getTimestampDatatype()  + "\" timeOffset=\"" +
					group.getTimeAdjustment()  + "\"");
			if(group.getTimeResolutionMultiplierName() != null){
				buff.append(" timeResulution=\"" + group.getTimeResolutionMultiplierName() + "\"");
			}
			buff.append(">\n");
			
			for(StatisticDescription stat: group.getStatisticsOrdered()){								
				buff.append("<" + stat.getName());
				
				if(stat.getUnit() != null){
					buff.append(" unit=\"" + stat.getUnit()  + "\"");
				}
				
				buff.append(" multiplier=\"" + stat.getMultiplier() + "\" type=\"" + stat.getType()  + "\"/>\n");
			}
			
			buff.append("</" + group.getName() + ">\n");
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
	
	private void writeTopologyRecursive(StringBuffer buff, TopologyNode topologyInternalLevel){		
		if(! topologyInternalLevel.isLeaf()){
			buff.append("<Label value='" + 
					XMLHelper.escapeAttribute(topologyInternalLevel.getName()) + "'>\n");
			for(TopologyNode child: topologyInternalLevel.getChildElements().values()){
				writeTopologyRecursive(buff, child);
			}
			buff.append("</Label>\n");
		}else{ // add close tag ...
			buff.append("<Label value='" + XMLHelper.escapeAttribute(topologyInternalLevel.getName())
					+ "'/>\n");
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
