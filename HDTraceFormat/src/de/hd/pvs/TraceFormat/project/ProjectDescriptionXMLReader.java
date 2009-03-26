
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

import java.io.File;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.LinkedList;

import de.hd.pvs.TraceFormat.statistics.StatisticDescription;
import de.hd.pvs.TraceFormat.statistics.StatisticType;
import de.hd.pvs.TraceFormat.statistics.StatisticsGroupDescription;
import de.hd.pvs.TraceFormat.topology.TopologyInternalLevel;
import de.hd.pvs.TraceFormat.topology.TopologyLabels;
import de.hd.pvs.TraceFormat.topology.TopologyLeafLevel;
import de.hd.pvs.TraceFormat.util.Epoch;
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
		File XMLFile = new File(projectFilename);
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
		
		try {
			String val = rootTag.getAttribute("processCount");
			descriptionInOut.setProcessCount(Integer.parseInt(val));
		} catch (NumberFormatException e) {
			throw new InvalidParameterException(
			"Invalid XML, \"processCount\" missing in project description");
		}

		final XMLTag xmlTopology = rootTag.getAndRemoveFirstNestedXMLTagWithName("Topology");
		TopologyLabels labels = new TopologyLabels();
		
		if(xmlTopology == null){
			throw new IllegalArgumentException("Topology Tag not found! Invalid XML!");
		}
		
		parseTopologyLabel(xmlTopology, labels);
		
		descriptionInOut.setTopologyLabels(labels);
		
		descriptionInOut.setTopologyRoot( parseTopology(xmlTopology, descriptionInOut.getFilesPrefix(), null) );

		// parse the descriptions of the external statistics:
		XMLTag element = rootTag.getAndRemoveFirstNestedXMLTagWithName("ExternalStatistics");
		if(element != null){
			final ArrayList<XMLTag> children = element.getNestedXMLTags(); 
			for(XMLTag stat: children){
				StatisticsGroupDescription out = parseStatisticGroupInXML(stat);
				descriptionInOut.addExternalStatisticsGroup(out);
			}
		}
	}
	
	/**
	 * Recursivly read topology labels
	 * @param topologyTag
	 * @param labels
	 */
	private void parseTopologyLabel(XMLTag topologyTag, TopologyLabels labels){
		final XMLTag curLabel = topologyTag.getFirstNestedXMLTagWithName("Level");
		if(curLabel == null){
			return;
		}
		final String name = curLabel.getAttribute("name");		
		labels.addLabelOfNextDepth(name);
		
		parseTopologyLabel(curLabel, labels);
	}
	
	
	private TopologyInternalLevel parseTopology(XMLTag xmlTopology, String name, TopologyInternalLevel parent){		
		LinkedList<XMLTag> children =  xmlTopology.getNestedXMLTagsWithName("Label"); 
		
		if(children.size() == 0){
			// leaf level
			TopologyLeafLevel level = new TopologyLeafLevel(name, parent);
			return level;
		}
		
		TopologyInternalLevel level = new TopologyInternalLevel(name, parent);
		
		int pos = 0;
		for(XMLTag tag: children){
			String childName = tag.getAttribute("value");
			
			TopologyInternalLevel child = parseTopology(tag,  childName, level);
			level.setChild(childName, child);
			
			level.setPositionInParent(pos++);
		}
		
        return level;
	}
	
		
	private StatisticsGroupDescription parseStatisticGroupInXML(XMLTag root){
		StatisticsGroupDescription stat = new StatisticsGroupDescription();
		stat.setName(root.getName());
		//System.out.println("Statistics: " + root.getNodeName());
		
		final String tT = root.getAttribute("timestampDatatype");		
		if(tT != null  && ! tT.isEmpty()){
			StatisticType type = StatisticType.valueOf(tT);
			stat.setTimestampDatatype(type);
		}
		
		final String tR = root.getAttribute("timeResulution");		
		if (tR != null && ! tR.isEmpty()){
			stat.setTimeResolutionMultiplier(tR);
		}
		
		final String toffset = root.getAttribute("timeOffset");
		if (toffset != null && ! toffset.isEmpty()){
			stat.setTimeOffset(Epoch.parseTime(toffset));
		}
		
		final ArrayList<XMLTag> children = root.getNestedXMLTags();
		
		// the next number of the statistic group:
		int currentNumberInGroup = 0;
		for(XMLTag child: children){
			int multiplier = 1;
			if(child.getAttribute("multiplier").length() > 0){
				multiplier = Integer.parseInt(child.getAttribute("multiplier"));			
			}
			StatisticDescription desc = new StatisticDescription(child.getName(), 
					StatisticType.valueOf( child.getAttribute("type").toUpperCase() ),
					currentNumberInGroup,
					child.getAttribute("unit"),
					multiplier);
			
			stat.addStatistic(desc);
			
			currentNumberInGroup++;
		}
		
		return stat;
	}	
	
	public ArrayList<XMLTag> getUnparsedChildTags() {
		return rootTag.getNestedXMLTags();
	}
}
