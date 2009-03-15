package de.hd.pvs.piosim.model.program;

import de.hd.pvs.TraceFormat.trace.SaxTraceFileReader;
import de.hd.pvs.piosim.model.ModelVerifier;
import de.hd.pvs.piosim.model.program.commands.superclasses.Command;

/**
 * The program is generated on demand.
 * 
 * @author julian
 *
 */
public class ProgramReadXMLOnDemand extends Program{

	/**
	 * Check the commands during run.
	 */
	final static ModelVerifier modelVerifier = new ModelVerifier();
	
	private SaxTraceFileReader traceFileReader;
	private final String filename;
	
	public ProgramReadXMLOnDemand(String filename) throws Exception{
		 this.filename = filename;
		 restartWithFirstCommand();
	}
	
	@Override
	public Command getNextCommand() {
		if(isFinished())
			return null;
		//traceFileReader.getNextInputData();
		return null;
		
	}
	
	@Override
	public boolean isFinished() {
		return traceFileReader.isFinished();
	}
	
	@Override
	public void restartWithFirstCommand() {
		try{
			traceFileReader = new  SaxTraceFileReader(filename, false);
		}catch(Exception e){
			throw new IllegalArgumentException(e);
		}
	}
}
