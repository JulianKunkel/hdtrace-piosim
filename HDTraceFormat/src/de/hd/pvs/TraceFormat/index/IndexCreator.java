
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

package de.hd.pvs.TraceFormat.index;

import de.hd.pvs.TraceFormat.project.ProjectDescription;
import de.hd.pvs.TraceFormat.project.ProjectDescriptionXMLReader;
import de.hd.pvs.TraceFormat.statistics.StatisticsGroupEntry;
import de.hd.pvs.TraceFormat.statistics.StatisticsReader;
import de.hd.pvs.TraceFormat.topology.TopologyNode;
import de.hd.pvs.TraceFormat.trace.StAXTraceFileReader;
import de.hd.pvs.TraceFormat.trace.ITraceEntry;

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
		
		ITraceEntry entry = reader.getNextInputEntry();
		while(entry != null){
			
			writer.writeNextEntry(entry.getEarliestTime(), reader.getFilePosition());
			
			entry = reader.getNextInputEntry();
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
	public void createIndexForStatisticFile(String projectFile, String group, TopologyNode topology) throws Exception{
		final ProjectDescriptionXMLReader preader = new ProjectDescriptionXMLReader();
		
		final ProjectDescription desc = new ProjectDescription();
		
		preader.readProjectDescription(desc, projectFile);
				
		final String inputFile = topology.getStatisticFileName(group);

		createIndexForStatisticFile(inputFile);
	}
	

	/**
	 * Create an index file for a statistic group
	 * 
	 * @param inputFile
	 * @param group
	 * @throws Exception
	 */
	public void createIndexForStatisticFile(String inputFile) throws Exception{
		final int dotPos = inputFile.lastIndexOf('.');		
		final String outFileName = inputFile.substring(0, dotPos) + ".idx";
		
		final StatisticsReader reader = new StatisticsReader(inputFile);
	
		final IndexWriter writer = new IndexWriter(outFileName);	
		
		StatisticsGroupEntry entry = reader.getNextInputEntry();
		while(entry != null){
			
			writer.writeNextEntry(entry.getEarliestTime(), reader.getFilePosition());
			
			entry = reader.getNextInputEntry();
		}
		
		writer.finalize();		
	}

}
