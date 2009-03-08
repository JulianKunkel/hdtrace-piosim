package de.hd.pvs.traceConverter;

import de.hd.pvs.traceConverter.Input.InputData;
import de.hd.pvs.traceConverter.Output.TraceOutputConverter;

public class HDTraceConverter {
	
	/**
	 * Start the conversion of the input trace to the given output trace.
	 * @param param
	 */
	public void process(RunParameters param) throws Exception{
		// try to instantiate the appropriate output converter:
		TraceOutputConverter converter = instantiateOutputWriter(
				"de.hd.pvs.traceConverter.Output." + 
				param.getOutputFormat() + "." +
				param.getOutputFormat() + "Converter");
		
		// scan for all the XML files which must be opened during conversion:
		// general description
		// external statistics
		// trace files: rank + thread id are defined in the file name

		// Test with just one trace file:
		SaxTraceFileTokenizer tokenizer = new SaxTraceFileTokenizer(param.getInputTraceFile());	
		
		InputData data = tokenizer.getNextInputData();
		while(data != null){
			System.out.println("Data");
			data = tokenizer.getNextInputData();
		}
	}
	
	/** 
	 * Simple factory method to instantiate the selected TraceOutput implementation
	 * via reflection 
	 */
	private TraceOutputConverter instantiateOutputWriter(String which){
		Class<TraceOutputConverter> cls;
		try{
			cls = (Class<TraceOutputConverter>) Class.forName(which);
		}catch(ClassNotFoundException e){
			throw new IllegalArgumentException("Output converter for " + which + " not found!");
		}
		TraceOutputConverter converter;
		try{
			converter = cls.newInstance();
		}catch(Exception e){
			throw new IllegalArgumentException("Invalid converter for " + which + "!", e);
		}
		return converter;
	}
	
}
