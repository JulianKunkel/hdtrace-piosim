
/** Version Control Information $Id$
 * @lastmodified    $Date$
 * @modifiedby      $LastChangedBy$
 * @version         $Revision$
 */


//Copyright (C) 2008, 2009 Julian M. Kunkel

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

package de.hd.pvs.piosim.model.program;

import de.hd.pvs.TraceFormat.trace.ITraceEntry;
import de.hd.pvs.TraceFormat.trace.StAXTraceFileReader;
import de.hd.pvs.TraceFormat.util.Epoch;
import de.hd.pvs.piosim.model.ModelVerifier;
import de.hd.pvs.piosim.model.program.commands.Compute;
import de.hd.pvs.piosim.model.program.commands.NoOperation;
import de.hd.pvs.piosim.model.program.commands.superclasses.Command;

/**
 * The program is generated on demand.
 *
 * @author Julian M. Kunkel
 *
 */
public class ProgramReadXMLOnDemand extends Program{

	/**
	 * Check the commands during run.
	 */
	final static ModelVerifier modelVerifier = new ModelVerifier();

	final private CommandXMLReader cmdReader;

	private Epoch lastTimeForComputeJob;
	private boolean lastTimeComputed;

	private StAXTraceFileReader traceFileReader;
	private String filename;

	private ITraceEntry curTraceEntry;
	private Command curCommand;

	public ProgramReadXMLOnDemand() {
		this.cmdReader = new CommandXMLReader(this);
	}

	@Override
	public void setFilename(String filename) {
		this.filename = filename;
	}

	private void setNextEntry(){
		do{
			curTraceEntry = traceFileReader.getNextInputEntry();

			if(curTraceEntry == null)
				return;
			try{
				curCommand = cmdReader.parseCommandXML(curTraceEntry);
				// check if this command is valid:
				modelVerifier.checkConsistency(curCommand);
			}catch(Exception e){
				throw new IllegalStateException("Error in file " + filename + " at pos: " +  traceFileReader.getFilePosition(), e);
			}
		}while(curCommand.getClass() == NoOperation.class);
	}

	@Override
	public Command getNextCommand() {
		if(lastTimeComputed == false){
			// add an appropriate compute job
			long cycles = curTraceEntry.getEarliestTime().subtract(lastTimeForComputeJob).getLongInNS() / 1000;
			if(cycles > 0){
				lastTimeComputed = true;

				Compute compute = new Compute();
				compute.setCycles( cycles );
				return compute;
			}
		}

		lastTimeComputed = false;

		lastTimeForComputeJob = curTraceEntry.getLatestTime();

		Command curComm = this.curCommand;
		setNextEntry();
		return curComm;
	}

	@Override
	public boolean isFinished() {
		return curTraceEntry == null;
	}

	@Override
	public void restartWithFirstCommand() {
		try{
			traceFileReader = new StAXTraceFileReader(filename, false, Epoch.ZERO);
			setNextEntry();

			if(curTraceEntry != null){
				lastTimeForComputeJob = curTraceEntry.getLatestTime();
			}
			lastTimeComputed = true;
		}catch(Exception e){
			throw new IllegalArgumentException(e);
		}
	}
}
