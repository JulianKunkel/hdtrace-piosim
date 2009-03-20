
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
import java.util.LinkedList;

import de.hd.pvs.TraceFormat.statistics.ExternalStatisticsGroup;
import de.hd.pvs.TraceFormat.statistics.StatisticDescription;
import de.hd.pvs.TraceFormat.statistics.StatisticType;
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

		// now add available thread count per process, by checking for existing files:
		for(int i=0; i < descriptionInOut.getProcessCount(); i++){
			int t = 0;
			while(true){
				File threadFile = new File(descriptionInOut.getAbsoluteFilenameOfTrace(i, t));
				if(! threadFile.exists()) break;
				t++;
			}
			if(t == 0){
				throw new IOException("No thread found for rank " + i + " project " + projectFilename);
			}
			descriptionInOut.setProcessThreadCount(i, t);
		}
		
		// parse the descriptions of the external statistics:
		XMLTag element = rootTag.getAndRemoveFirstNestedXMLTagWithName("ExternalStatistics");
		if(element != null){
			final LinkedList<XMLTag> children = element.getNestedXMLTags(); 
			for(XMLTag stat: children){
				ExternalStatisticsGroup out = parseStatisticGroupInXML(stat);
				descriptionInOut.addExternalStatisticsGroup(out);
			}
		}
	}
		
	private ExternalStatisticsGroup parseStatisticGroupInXML(XMLTag root){
		ExternalStatisticsGroup stat = new ExternalStatisticsGroup();
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
		
		final LinkedList<XMLTag> children = root.getNestedXMLTags(); 
		for(XMLTag child: children){
			int multiplier = 1;
			if(child.getAttribute("multiplier").length() > 0){
				multiplier = Integer.parseInt(child.getAttribute("multiplier"));			
			}
			StatisticDescription desc = new StatisticDescription(child.getName(), 
					StatisticType.valueOf( child.getAttribute("type").toUpperCase() ),
					child.getAttribute("unit"),
					multiplier);
			
			stat.addStatistic(desc);
		}
		
		return stat;
	}	
	
	public LinkedList<XMLTag> getUnparsedChildTags() {
		return rootTag.getNestedXMLTags();
	}
}
