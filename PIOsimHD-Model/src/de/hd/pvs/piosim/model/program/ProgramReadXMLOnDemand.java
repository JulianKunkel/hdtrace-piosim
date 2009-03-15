
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

package de.hd.pvs.piosim.model.program;

import de.hd.pvs.TraceFormat.trace.SaxTraceFileReader;
import de.hd.pvs.TraceFormat.trace.XMLTraceEntry;
import de.hd.pvs.piosim.model.ModelVerifier;
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
	
	final CommandXMLReader cmdReader;

	private SaxTraceFileReader traceFileReader;
	private final String filename;
	
	private XMLTraceEntry nextCmdEntry;
	
	public ProgramReadXMLOnDemand(String filename, Application app) throws Exception{
		this.filename = filename;
		restartWithFirstCommand();
		this.cmdReader = new CommandXMLReader(app);
	}

	@Override
	public Command getNextCommand() {
		if (nextCmdEntry == null)
			return null;
		
		try{
			Command cmd = cmdReader.readCommandXML(nextCmdEntry, this);
			
			// check if this command is valid:
			modelVerifier.checkConsistency(cmd);
			
			nextCmdEntry = traceFileReader.getNextInputData();			
			return cmd;
		}catch(Exception e){
			throw new IllegalStateException(e);
		}		
	}

	@Override
	public boolean isFinished() {
		return nextCmdEntry == null;
	}

	@Override
	public void restartWithFirstCommand() {
		try{
			traceFileReader = new SaxTraceFileReader(filename, false);
			nextCmdEntry = traceFileReader.getNextInputData();
		}catch(Exception e){
			throw new IllegalArgumentException(e);
		}
	}
}
