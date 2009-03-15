package de.hd.pvs.piosim.model.program;

import de.hd.pvs.TraceFormat.trace.SaxTraceFileReader;
import de.hd.pvs.TraceFormat.trace.XMLTraceEntry;
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
