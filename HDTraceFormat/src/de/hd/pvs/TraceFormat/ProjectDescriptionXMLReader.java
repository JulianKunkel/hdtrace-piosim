package de.hd.pvs.TraceFormat;

import java.io.File;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.LinkedList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import de.hd.pvs.TraceFormat.statistics.ExternalStatisticsGroup;
import de.hd.pvs.TraceFormat.statistics.StatisticDescription;
import de.hd.pvs.TraceFormat.statistics.StatisticType;
import de.hd.pvs.TraceFormat.util.Epoch;
import de.hd.pvs.TraceFormat.xml.XMLReaderToRAM;
import de.hd.pvs.TraceFormat.xml.XMLTag;
import de.hd.pvs.TraceFormat.xml.XMLutil;

/**
 * This class is used to create a primitive project description from an XML file.
 *  
 * @author Julian M. Kunkel
 */

public class ProjectDescriptionXMLReader {

	protected XMLTag rootTag;
	
	public void readProjectDescription(ProjectDescription descriptionInOut, String projectFilename) throws Exception{
		File XMLFile = new File(projectFilename);
		if (! XMLFile.canRead()) {
			throw new IllegalArgumentException("Project file not readable: " + XMLFile.getAbsolutePath());
		}
		descriptionInOut.setProjectFilename(XMLFile.getAbsolutePath());
		
		XMLReaderToRAM reader = new XMLReaderToRAM();
		rootTag = reader.readXML(projectFilename);
		
		// read standard descriptions:
		descriptionInOut.setApplicationName( rootTag.getAttribute("name"));

		descriptionInOut.setDescription( rootTag.getFirstNestedXMLTagWithName("Description").getContainedText());
		
		try {
			String val = rootTag.getAttribute("processCount");
			descriptionInOut.setProcessCount(Integer.parseInt(val));
		} catch (NumberFormatException e) {
			throw new InvalidParameterException(
			"Invalid XML, \"processCount\" missing in project description");
		}

		// now add available thread count per process, by checking for existing files:
		for(int i=0; i < descriptionInOut.getRankCount(); i++){
			int t = 0;
			while(true){
				File threadFile = new File(TraceFileNames.getFilenameXML(descriptionInOut.getAbsoluteFilesPrefix(), i, t));
				if(! threadFile.exists()) break;
				t++;
			}
			if(t == 0){
				throw new IOException("No thread found for rank " + i + " project " + projectFilename);
			}
			descriptionInOut.setProcessThreadCount(i, t);
		}
		
		// parse the descriptions of the external statistics:
		XMLTag element = rootTag.getFirstNestedXMLTagWithName("ExternalStatistics");
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
}
