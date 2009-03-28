
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

package de.hd.pvs.traceConverter.Input;

import java.io.IOException;

import de.hd.pvs.TraceFormat.topology.TopologyEntry;
import de.hd.pvs.TraceFormat.util.Epoch;
import de.hd.pvs.traceConverter.RunParameters;
import de.hd.pvs.traceConverter.Output.TraceOutputWriter;

/**
 * An {@link AbstractTraceProcessor} fetches data from a trace source i.e. a particular file and
 * manufactures trace events from the trace file for a {@link TraceOutputWriter}.
 * 
 * Trace events processed by a {@link AbstractTraceProcessor} must be returned with an increasing start time.
 * 
 * @author Julian M. Kunkel
 *
 */
abstract public class AbstractTraceProcessor implements Comparable<AbstractTraceProcessor> {
	/**
	 * The implementation of the {@link TraceOutputWriter} i.e. the file format and spec.
	 */
	private TraceOutputWriter outputConverter;
	
	private TopologyEntry topology;
	
	private RunParameters      runParameters;
	
	/**
	 * Are there more events to process
	 * @return
	 */
	abstract public boolean isFinished();
	
	/**
	 * Return the next "trace event" to process.
	 */
	abstract public void processEarliestEvent(Epoch now);
	
	/**
	 * Return the next event in the trace source.
	 * 
	 * @return
	 */
	abstract public Epoch peekEarliestTime();
	
	/**
	 * Called once all information is set on this TraceProcessor
	 */
	abstract public void initalize();
	
	/**
	 * Get the current position in the file
	 * @return
	 * @throws IOException
	 */
	abstract public long getFilePosition() throws IOException;
	
	/**
	 * Set the implementation of the {@link TraceOutputWriter} i.e. the file format and spec.
	 * @param outputConverter
	 */
	final public void setOutputConverter(TraceOutputWriter outputConverter) {
		this.outputConverter = outputConverter;
	}
	
	final public TraceOutputWriter getOutputConverter() {
		return outputConverter;
	}
	

	public void setTopologyEntryResponsibleFor(TopologyEntry topology) {
		this.topology = topology;
	}
	
	public TopologyEntry getTopologyEntryResponsibleFor() {
		return topology;
	}
	
	@Override
	public int compareTo(AbstractTraceProcessor o) {	
		return this.peekEarliestTime().compareTo(o.peekEarliestTime());
	}

	
	public RunParameters getRunParameters() {
		return runParameters;
	}
	
	public void setRunParameters(RunParameters runParameters) {
		this.runParameters = runParameters;
	}
}
