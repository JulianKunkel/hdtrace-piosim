
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

package de.hd.pvs.TraceFormat;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import de.hd.pvs.TraceFormat.statistics.ExternalStatisticsGroup;
import de.hd.pvs.TraceFormat.statistics.StatisticDescription;
import de.hd.pvs.TraceFormat.statistics.StatisticType;
import de.hd.pvs.TraceFormat.util.Epoch;
import de.hd.pvs.TraceFormat.xml.XMLutil;

public class ApplicationTraceReader {
	ExistingTraceFiles traceFiles;
	
	HashMap<String, ExternalStatisticsGroup> statisticGroupDescriptions = new HashMap<String, ExternalStatisticsGroup>();
	
	private ExternalStatisticsGroup parseStatisticGroupInXML(Element root){
		ExternalStatisticsGroup stat = new ExternalStatisticsGroup();
		stat.setName(root.getNodeName());
		//System.out.println("Statistics: " + root.getNodeName());
		
		final String tT = root.getAttribute("timestampDatatype");		
		if(tT != null  && ! tT.isEmpty()){
			StatisticType type = StatisticType.valueOf(tT);
			stat.setTimestampDatatype(type);
		}
		
		final String tR = root.getAttribute("timeResulution");		
		if (tR != null && ! tR.isEmpty()){
			if(tR.equals("Mikroseconds")){
				stat.setTimeResolutionMultiplier(1000);
			}else if(tR.equals("Milliseconds")){
				stat.setTimeResolutionMultiplier(1000 * 1000);
			}else{
				throw new IllegalArgumentException("Invalid timestampResulution " + tR + "  in statistic group: " + stat.getName());
			}
		}
		
		final String toffset = root.getAttribute("timeOffset");
		if (toffset != null && ! toffset.isEmpty()){
			stat.setTimeOffset(Epoch.parseTime(toffset));
		}
		
		ArrayList<Element> children = XMLutil.getChildElements(root);
		for(Element child: children){
			int multiplier = 1;
			if(child.getAttribute("multiplier").length() > 0){
				multiplier = Integer.parseInt(child.getAttribute("multiplier"));			
			}
			StatisticDescription desc = new StatisticDescription(child.getNodeName(), 
					StatisticType.valueOf( child.getAttribute("type").toUpperCase() ),
					child.getAttribute("unit"),
					multiplier);
			
			stat.addStatistic(desc);
		}
		
		return stat;
	}
	
	public ApplicationTraceReader(String projectFileName) throws Exception {
		File projectFile = new File(projectFileName);
		
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document document = builder.parse(projectFile);
		Element applicationNode = document.getDocumentElement();

		int processCount = XMLutil.getIntValueAttribute(applicationNode, "processCount", 0);
		
		
		// parse the descriptions of the external statistics:
		Element element = XMLutil.getFirstElementByTag(applicationNode, "ExternalStatistics");
		if(element != null){
			ArrayList<Element> children = XMLutil.getChildElements(element);
			for(Element stat: children){
				ExternalStatisticsGroup out = parseStatisticGroupInXML(stat);				
				statisticGroupDescriptions.put(out.getName(), out);
			}
		}
	
		traceFiles = new ExistingTraceFiles(projectFileName, processCount);
	}
	
	public ExistingTraceFiles getTraceFiles() {
		return traceFiles;
	}
	
	public Collection<String> getExternalStatisticGroups(){
		return statisticGroupDescriptions.keySet();
	}
	
	public ExternalStatisticsGroup getExternalStatisticsDescription(String groupName){
		return statisticGroupDescriptions.get(groupName);
	}
}
