package de.hd.pvs.TraceFormat;

import java.io.File;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import de.hd.pvs.TraceFormat.statistics.ExternalStatisticsGroup;
import de.hd.pvs.TraceFormat.statistics.StatisticDescription;
import de.hd.pvs.TraceFormat.statistics.StatisticType;
import de.hd.pvs.TraceFormat.util.Epoch;
import de.hd.pvs.TraceFormat.xml.XMLutil;

/**
 * This class is used to create a primitive project description from an XML file.
 *  
 * @author Julian M. Kunkel
 */

public class ProjectDescriptionXMLReader {

	/**
	 * Contains the DOM, read during readProjectDescription
	 */
	protected Document DOMdocument;
	
	/**
	 * Protected DOM Builder
	 */
	protected DocumentBuilder DOMbuilder;
	
	public void readProjectDescription(ProjectDescription descriptionInOut, String projectFilename) throws Exception{
		File XMLFile = new File(projectFilename);
		if (! XMLFile.canRead()) {
			throw new IllegalArgumentException("Project file not readable: " + XMLFile.getAbsolutePath());
		}
		descriptionInOut.setProjectFilename(XMLFile.getAbsolutePath());
		
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DOMbuilder = factory.newDocumentBuilder();
		DOMdocument = DOMbuilder.parse(XMLFile);

		Element applicationNode = DOMdocument.getDocumentElement();

		// read standard descriptions:
		descriptionInOut.setApplicationName( XMLutil.getAttributeText(applicationNode, "name"));

		descriptionInOut.setDescription(XMLutil.getPlainText(applicationNode, "Description"));
		
		try {
			String val = XMLutil.getAttributeText(applicationNode, "processCount");
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
		Element element = XMLutil.getFirstElementByTag(applicationNode, "ExternalStatistics");
		if(element != null){
			ArrayList<Element> children = XMLutil.getChildElements(element);
			for(Element stat: children){
				ExternalStatisticsGroup out = parseStatisticGroupInXML(stat);
				descriptionInOut.addExternalStatisticsGroup(out);
			}
		}
	}
		
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
}
