
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

package de.hd.pvs.TraceFormat.index;

import de.hd.pvs.TraceFormat.project.ProjectDescription;
import de.hd.pvs.TraceFormat.project.ProjectDescriptionXMLReader;
import de.hd.pvs.TraceFormat.statistics.ExternalStatisticsGroup;
import de.hd.pvs.TraceFormat.statistics.StatisticEntry;
import de.hd.pvs.TraceFormat.statistics.StatisticsReader;
import de.hd.pvs.TraceFormat.trace.StAXTraceFileReader;
import de.hd.pvs.TraceFormat.trace.XMLTraceEntry;

/**
 * Create an index file for a statistic group or trace file.
 * Indices could be created in parallel.
 * 
 * @author Julian M. Kunkel
 *
 */
public class IndexCreator {
	
	/**
	 * Create an index file for a particular trace file
	 * @param inputFile
	 * @throws Exception
	 */
	public void createIndexForTraceFile(String inputFile) throws Exception{
		final StAXTraceFileReader reader = new StAXTraceFileReader(inputFile, false);
		
		final int dotPos = inputFile.lastIndexOf('.');		
		final String outFileName = inputFile.substring(0, dotPos) + ".idx";

		final IndexWriter writer = new IndexWriter(outFileName);	
		
		XMLTraceEntry entry = reader.getNextInputData();
		while(entry != null){
			
			writer.writeNextEntry(entry.getTime(), reader.getFilePosition());
			
			entry = reader.getNextInputData();
		}
		
		reader.close();
		writer.finalize();		
	}
	
	/**
	 * Create an index for an particular statistic file.
	 * 
	 * @param projectFile
	 * @param group
	 * @param rank
	 * @param thread
	 * @throws Exception
	 */
	public void createIndexForStatisticFile(String projectFile, String group, int rank, int thread) throws Exception{
		final ProjectDescriptionXMLReader preader = new ProjectDescriptionXMLReader();
		
		final ProjectDescription desc = new ProjectDescription();
		
		preader.readProjectDescription(desc, projectFile);
				
		final String inputFile = desc.getAbsoluteFilenameOfStatistics(rank, thread, group);
		final ExternalStatisticsGroup realGroup = desc.getExternalStatisticsGroup(group);
		if(realGroup == null){
			throw new IllegalArgumentException("Group \"" + group + "\" does not exisist");
		}
		
		createIndexForStatisticFile(inputFile, realGroup);
	}
	

	/**
	 * Create an index file for a statistic group
	 * 
	 * @param inputFile
	 * @param group
	 * @throws Exception
	 */
	public void createIndexForStatisticFile(String inputFile, ExternalStatisticsGroup group) throws Exception{
		final int dotPos = inputFile.lastIndexOf('.');		
		final String outFileName = inputFile.substring(0, dotPos) + ".idx";
		
		final StatisticsReader reader = new StatisticsReader(inputFile, group);
	
		final IndexWriter writer = new IndexWriter(outFileName);	
		
		StatisticEntry entry = reader.getNextStatisticEntry();
		while(entry != null){
			
			writer.writeNextEntry(entry.getTimeStamp(), reader.getFilePosition());
			
			entry = reader.getNextStatisticEntry();
		}
		
		reader.close();
		writer.finalize();		
	}
	
	
	public static void main(String[] args) throws Exception{
		IndexCreator creator = new IndexCreator();
		
		creator.createIndexForTraceFile("/home/Julian M. Kunkel/workspace/PIOsimHD/HDTraceConverter/Example/test_0_0.xml");
		creator.createIndexForStatisticFile("/home/Julian M. Kunkel/workspace/PIOsimHD/HDTraceConverter/Example/test.xml", "Energy", 0, 0);
	}
}
