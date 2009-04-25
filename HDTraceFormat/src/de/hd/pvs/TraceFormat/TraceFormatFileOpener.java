
/** Version Control Information $Id$
 * @lastmodified    $Date$
 * @modifiedby      $LastChangedBy$
 * @version         $Revision$ 
 */

//Copyright (C) 2009 Julian M. Kunkel

//This file is part of PIOsimHD.

//PIOsimHD is free software: you can redistribute it and/or modify
//it under the terms of the GNU General Public License as published by
//the Free Software Foundation, either version 3 of the License, or
//(at your option) any later version.

//PIOsimHD is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU General Public License for more details.

//You should have received a copy of the GNU General Public License
//along with PIOsimHD.  If not, see <http://www.gnu.org/licenses/>.


package de.hd.pvs.TraceFormat;

import java.io.File;

import de.hd.pvs.TraceFormat.project.ProjectDescription;
import de.hd.pvs.TraceFormat.project.ProjectDescriptionXMLReader;
import de.hd.pvs.TraceFormat.statistics.StatisticSource;
import de.hd.pvs.TraceFormat.statistics.StatisticsGroupDescription;
import de.hd.pvs.TraceFormat.topology.TopologyNode;
import de.hd.pvs.TraceFormat.topology.TopologyLabels;
import de.hd.pvs.TraceFormat.trace.TraceSource;

/**
 * This class opens all files belonging to a particular project.
 * 
 * @author Julian M. Kunkel
 *
 */
public class TraceFormatFileOpener {
	final ProjectDescription projectDescription;
	final ProjectDescriptionXMLReader projectDescrReader;

	public ProjectDescription getProjectDescription() {
		return projectDescription;
	}

	public ProjectDescriptionXMLReader getProjectDescriptionXMLReader() {
		return projectDescrReader;
	}

	// check if the file exists and is readable or not.
	private boolean fileExists(String filename){
		File file =  new File(filename);

		if(file.exists() && ! file.canRead()){
			throw new IllegalArgumentException("File " + filename + " exists but is not readable");
		}

		return file.exists();
	}

	private void recursivlyCreateTraceSources(TopologyNode currentTopo, 
			Class<? extends StatisticSource> statCls, 
			Class<? extends TraceSource> traceCls, 
			boolean readNested
		) throws Exception
{		
		final String filePath = projectDescription.getParentDir() + "/"; 

		// load statistics:
		for(StatisticsGroupDescription group: projectDescription.getExternalStatisticGroups()){
			String filename = filePath + currentTopo.getStatisticFileName(group.getName());
			
			SimpleConsoleLogger.Debug("Checking stat file for existence: " + filename);
			if( fileExists(filename) ){
				// read it
				SimpleConsoleLogger.Debug("Stat file exists: " + filename);				

				final StatisticSource statReader = statCls.getConstructor(new Class<?>[]{String.class, StatisticsGroupDescription.class}).newInstance(new Object[]{filename, group});
				currentTopo.setStatisticReader(group.getName(), statReader);
			}
		}

		final String traceFile = filePath + currentTopo.getTraceFileName();

		// ignore the root of the tree, because this is the project file
		if(currentTopo.hasParent()){ 
			SimpleConsoleLogger.Debug("Checking trace: " + traceFile);

			if( ! fileExists(traceFile) ){
				if( currentTopo.isLeaf() ){ // leafs should contain a trace file
					//throw new IllegalArgumentException("Trace file does not exist: " + traceFile);
					SimpleConsoleLogger.Debug("Topology leaf should contain a trace file: " + currentTopo.toRecursiveString() );					
				}
				currentTopo.setTraceSource(null);
			}else{
				TraceSource staxReader = traceCls.getConstructor(new Class<?>[]{String.class, boolean.class}).newInstance(new Object[]{traceFile, readNested});			
				currentTopo.setTraceSource(staxReader);
			}		
		}

		for(TopologyNode child: currentTopo.getChildElements().values()){
			recursivlyCreateTraceSources(child, statCls, traceCls, readNested);
		}


	}

	public TraceFormatFileOpener(String filename, boolean readNested, Class<? extends StatisticSource> statCls, Class<? extends TraceSource> traceCls) throws Exception{
		// scan for all the XML files which must be opened during conversion:
		// general description
		projectDescrReader = new ProjectDescriptionXMLReader();
		projectDescription = new ProjectDescription();
		projectDescrReader.readProjectDescription(projectDescription, filename);

		// start parsing of the trace files:
		// trace files: rank + thread id are defined in the file name

		TopologyNode root = projectDescription.getTopologyRoot();

		recursivlyCreateTraceSources(root, statCls, traceCls, readNested );
	}	

	public TopologyNode getTopology(){
		return projectDescription.getTopologyRoot();
	}

	public TopologyLabels getTopologyLabels(){
		return projectDescription.getTopologyLabels();
	}
}
