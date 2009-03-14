
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

package de.hd.pvs.traceConverter;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import de.hd.pvs.piosim.model.util.Epoch;
import de.hd.pvs.piosim.model.util.XMLutil;
import de.hd.pvs.traceConverter.Input.Statistics.ExternalStatisticsGroup;
import de.hd.pvs.traceConverter.Input.Statistics.StatisticDescription;
import de.hd.pvs.traceConverter.Input.Statistics.StatisticType;

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

		// read standard descriptions:

		String appName = XMLutil.getAttributeText(applicationNode, "name");
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
		
		// scan for the trace files
		String prefix = projectFile.getName().toString();
		prefix = prefix.substring(0, prefix.lastIndexOf('.'));		
		HashMap<Integer, HashMap<Integer, ArrayList<String>>> map = new HashMap<Integer, HashMap<Integer,ArrayList<String>>>();
		
		for (int i=0 ; i < processCount; i++){
			HashMap<Integer, ArrayList<String>> threadFileMap = new HashMap<Integer, ArrayList<String>>();
			map.put(i, threadFileMap);				
		}
		
		File parent = projectFile.getParentFile();
		if(parent == null){
			parent = new File(".");
		}
		// scan for files with the prefix
		for (String file: parent.list()){
			if(file.startsWith(prefix + "_")){
				String remainder = file.substring(prefix.length() + 1, file.lastIndexOf('.'));				
				String[] splits = remainder.split("_");				
				if(splits.length == 2 || (splits.length == 4 && splits[2].equals("stat"))){
					// rank, thread id
					int rank = Integer.parseInt(splits[0]);
					int thread = Integer.parseInt(splits[1]);
					
					HashMap<Integer, ArrayList<String>> threadFileMap =  map.get(rank);
					
					ArrayList<String> files = threadFileMap.get(thread);
					if(files == null){
						files = new ArrayList<String>();
						threadFileMap.put(thread, files);
					}
					
					if(splits.length == 4){
						// statistics
						String statFilename = splits[3];
						files.add(statFilename);
					}
				}else{
					// discarding file
					System.out.println("Ignoring file " + file);
				}
				
			}
		}
		
		traceFiles = new ExistingTraceFiles(parent.getAbsolutePath() + "/" + prefix, map);
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
