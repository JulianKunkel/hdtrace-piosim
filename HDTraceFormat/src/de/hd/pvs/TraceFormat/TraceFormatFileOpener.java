
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
import java.util.ArrayList;

import de.hd.pvs.TraceFormat.project.ProjectDescription;
import de.hd.pvs.TraceFormat.project.ProjectDescriptionXMLReader;
import de.hd.pvs.TraceFormat.statistics.StatisticsSource;
import de.hd.pvs.TraceFormat.topology.TopologyNode;
import de.hd.pvs.TraceFormat.topology.TopologyTypes;
import de.hd.pvs.TraceFormat.trace.RelationSource;
import de.hd.pvs.TraceFormat.trace.TraceSource;
import de.hd.pvs.TraceFormat.util.Epoch;

/**
 * This class opens all files belonging to a particular project and sets the provided reader class to the contained topology nodes.
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
			Class<? extends StatisticsSource> statCls, 
			Class<? extends TraceSource> traceCls,
			Class<? extends RelationSource> relationCls, 
			boolean readNested
	) throws Exception
	{		
		final String filePath = projectDescription.getParentDir() + "/"; 

		// load statistics:
		if(statCls != null){
			for(String group: projectDescription.getStatisticsGroupNames()){
				String filename = filePath + currentTopo.getStatisticFileName(group);

				SimpleConsoleLogger.Debug("Checking stat file for existence: " + filename);
				if( fileExists(filename) ){
					// read it
					SimpleConsoleLogger.Debug("Stat file exists: " + filename);				

					final StatisticsSource statReader = statCls.getConstructor(new Class<?>[]{String.class, String.class, Epoch.class}).newInstance(new Object[]{filename, group, Epoch.ZERO});
					currentTopo.setStatisticsReader(group, statReader);
				}
			}
		}

		final String traceFile = filePath + currentTopo.getTraceFileName();

		// ignore the root of the tree, because this is the project file
		if(currentTopo.hasParent()){ 
			SimpleConsoleLogger.Debug("Checking trace: " + traceFile);
			final String relationFile = filePath + currentTopo.getRelationFileName();			

			if( fileExists(traceFile) && traceCls != null){
				TraceSource staxReader = traceCls.getConstructor(new Class<?>[]{String.class, boolean.class, Epoch.class}).newInstance(new Object[]{traceFile, readNested, Epoch.ZERO});			
				currentTopo.setTraceSource(staxReader);
			}		
			
			SimpleConsoleLogger.Debug("Checking relation: " + relationFile);			
			if( fileExists(relationFile) && relationCls != null){
				currentTopo.setRelationSource(relationCls.getConstructor(new Class<?>[]{String.class, Epoch.class}).newInstance(new Object[]{relationFile, Epoch.ZERO}));
			}
			
			if( currentTopo.isLeaf() &&  currentTopo.getRelationSource() == null && currentTopo.getTraceSource() == null){ // leafs should contain a trace file
				//throw new IllegalArgumentException("Trace file does not exist: " + traceFile);
				SimpleConsoleLogger.Warning("Topology leaf should contain a trace file: " + currentTopo.toRecursiveString() );					
			}
		}

		for(TopologyNode child: currentTopo.getChildElements().values()){
			recursivlyCreateTraceSources(child, statCls, traceCls, relationCls, readNested);
		}
	}

	/**
	 * Open a trace project file.
	 * 
	 * @param filename
	 * @param readNested
	 * @param statCls Instantiate this statistical reader (if null, no statistics are read), if unsure use the on demand reader "StatisticsReader"
	 * @param traceCls Instantiate this trace reader (if null, no traces are read), if unsure use the on demand reader "StAXTraceFileReader"
	 * @throws Exception
	 */
	public TraceFormatFileOpener(String filename, boolean readNested, 
			Class<? extends StatisticsSource> statCls, 
			Class<? extends TraceSource> traceCls,
			Class<? extends RelationSource> relationCls) throws Exception{
		// scan for all the XML files which must be opened during conversion:
		// general description
		projectDescrReader = new ProjectDescriptionXMLReader();
		projectDescription = new ProjectDescription();
		projectDescrReader.readProjectDescription(projectDescription, filename);

		// start parsing of the trace files:
		// trace files: rank + thread id are defined in the file name

		final TopologyNode root = projectDescription.getTopologyRoot();
		
		recursivlyCreateTraceSources(root, statCls, traceCls, relationCls, readNested );
	}
	
	public ArrayList<TopologyNode> getTopologies(){
		return projectDescription.getTopologyRoot().getSubTopologies();
	}

	public TopologyNode getTopology(){
		return projectDescription.getTopologyRoot();
	}

	public TopologyTypes getTopologyLabels(){		
		return projectDescription.getTopologyTypes();
	}
	
	
}
