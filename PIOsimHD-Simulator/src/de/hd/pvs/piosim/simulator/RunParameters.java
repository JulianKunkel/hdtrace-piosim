
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

package de.hd.pvs.piosim.simulator;

/**
 * This class stores relevant runtime parameters used during the simulator,
 * typically parameters are read by the command line interface.
 *
 * @author Julian M. Kunkel
 */
public class RunParameters {
	/**
	 * is a trace of the simulation run created?
	 */
	boolean traceEnabled = true;

	/**
	 * is internal communication traced, i.e. between a NIC and a Switch?
	 */
	boolean traceInternals = true;

	/**
	 * a client operation is divided into subsequent atomic operations, each operation step can be traced,
	 * if disabled only the whole operation is traced.
	 */
	boolean traceClientSteps = true;

	/**
	 * are any operations on an IOServer traced?
	 */
	boolean traceServers = true;

	/**
	 * If more than this number of events got started, then the simulation is aborted.
	 */
	int maximumNumberOfEventsToSimulate = -1;

	/**
	 * If the complete program shall be loaded, this allows checking of the program upon load and not during runtime
	 */
	boolean loadProgramToRamOnLoad = true;

	/**
	 * the file stores the tau trace
	 */
	String traceFile = "run-trace";

	/**
	 * Define the console logger definition.
	 */
	String loggerDefinitionFile = "";

	/**
	 * If true then all debug messages are printed.
	 */
	boolean debugEverything = false;

	public int getMaximumNumberOfEventsToSimulate() {
		return maximumNumberOfEventsToSimulate;
	}

	public void setMaximumNumberOfEventsToSimulate(
			int maximumNumberOfEventsToSimulate) {
		this.maximumNumberOfEventsToSimulate = maximumNumberOfEventsToSimulate;
	}

	public void setDebugEverything(boolean debugEverything) {
		this.debugEverything = debugEverything;
	}

	public boolean isDebugEverything() {
		return debugEverything;
	}

	public void setLoggerDefinitionFile(String loggerDefinitionFile) {
		this.loggerDefinitionFile = loggerDefinitionFile;
	}

	public String getLoggerDefinitionFile() {
		return loggerDefinitionFile;
	}

	/**
	 * @param traceEnabled the traceEnabled to set
	 */
	public void setTraceEnabled(boolean traceEnabled) {
		this.traceEnabled = traceEnabled;
	}

	/**
	 * @return the enableTrace
	 */
	public boolean isTraceEnabled() {
		return traceEnabled;
	}

	/**
	 * @return the traceInternals
	 */
	public boolean isTraceInternals() {
		return traceInternals;
	}

	/**
	 * @return the traceServers
	 */
	public boolean isTraceServers() {
		return traceServers;
	}

	/**
	 * @param traceInternals the traceInternals to set
	 */
	public void setTraceInternals(boolean traceInternals) {
		this.traceInternals = traceInternals;
	}

	/**
	 * @param traceServers the traceServers to set
	 */
	public void setTraceServers(boolean traceServers) {
		this.traceServers = traceServers;
	}

	/**
	 * @return the traceFile
	 */
	public String getTraceFile() {
		return traceFile;
	}

	/**
	 * @param traceFile the traceFile to set
	 */
	public void setTraceFile(String traceFile) {
		this.traceFile = traceFile;
	}

	/**
	 * @param traceClientSteps the traceClientSteps to set
	 */
	public void setTraceClientSteps(boolean traceClientSteps) {
		this.traceClientSteps = traceClientSteps;
	}

	/**
	 * @return the traceClientSteps
	 */
	public boolean isTraceClientSteps() {
		return traceClientSteps;
	}


	public boolean isLoadProgramToRamOnLoad() {
		return loadProgramToRamOnLoad;
	}

	public void setLoadProgramToRamOnLoad(boolean loadProgramToRamOnLoad) {
		this.loadProgramToRamOnLoad = loadProgramToRamOnLoad;
	}
}
