package de.hd.pvs.traceConverter;

import java.io.File;
import java.io.FilenameFilter;
import java.lang.management.ThreadMXBean;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import de.hd.pvs.piosim.model.util.XMLutil;
import de.hd.pvs.traceConverter.Input.Statistics.ExternalStatisticsGroup;
import de.hd.pvs.traceConverter.Input.Statistics.ExternalStatisticsGroup.StatisticType;

public class ApplicationTraceReader {
	ExistingTraceFiles traceFiles;
	
	HashMap<String, ExternalStatisticsGroup> statisticGroupDescriptions = new HashMap<String, ExternalStatisticsGroup>();
	
	private ExternalStatisticsGroup parseStatisticGroupInXML(Element root){
		ExternalStatisticsGroup stat = new ExternalStatisticsGroup();
		stat.setName(root.getNodeName());
		//System.out.println("Statistics: " + root.getNodeName());
		
		ArrayList<Element> children = XMLutil.getChildElements(root);
		for(Element child: children){
			stat.addStatistic(child.getNodeName(), StatisticType.valueOf( child.getAttribute("type").toUpperCase() ));
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
		prefix = prefix.substring(0, prefix.lastIndexOf('.')) + "_";		
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
			if(file.startsWith(prefix)){
				String remainder = file.substring(prefix.length(), file.lastIndexOf('.'));				
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
		
		traceFiles = new ExistingTraceFiles(prefix, map);
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
