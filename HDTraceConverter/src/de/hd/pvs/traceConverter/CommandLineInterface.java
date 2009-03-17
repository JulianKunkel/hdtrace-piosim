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

/**
 * 
 */
package de.hd.pvs.traceConverter;

/**
 * Simple command line interface to control the simulator. Reads command line
 * arguments and starts the simulation.
 * 
 * @author Julian M. Kunkel
 */
public class CommandLineInterface {

	private void printHelpText(RunParameters runParameters) {
		System.out
				.println("Syntax: \n"
						+ "  [options] -i <trace.xml> [-- [Format specific options] ] \n"
						+ "Options are a subset of: \n"
						+ " -o <outputFilePrefix> (default: \"" + runParameters.outputFilePrefix + "\")\n"
						+ " -F <outputFormat> (default:" + runParameters.getOutputFormat() +  ")\n"
						+ "    supported are Text, Tau, HDTrace \n"
						+ " -D enable all debugging information (default: false) \n"					
						+ " -l <floatValue> update Statistics only if they vary more than value %\n"
						+ " -a Compute average statistics for omited values (otherwise latest value), (-l will be activated \n"
						+ "    with current value: " +	runParameters.getStatisticModificationUntilUpdate() + "%, change with -l \n"
						+ " -C Process also trace-events for compute \n"
						+ " -h show help\n"
						+ "\nBoolean values can be set 0 or 1\n");

		System.exit(1);
	}
	
	/**
	 * Print the arguments on the console
	 * @param args
	 */
	private void printArguments(String [] args){
		StringBuffer buf = new StringBuffer();
		for(String arg: args){
			buf.append(arg + " ");
		}
		System.out.println("Started with arguments: " + buf.toString());
	}
	
	/**
	 * Run the simulation as given by the arguments.
	 * 
	 * @param args
	 * @throws Exception
	 */
	public void run(String[] args) throws Exception {
		int i = 0;

		printArguments(args);
		
		// create runtime parameters based on the arguments.
		RunParameters runParameters = new RunParameters();
		
		boolean startOutputSpecificOptions = false;
		
		while (i < args.length && args[i].startsWith("-")) {
			String param = args[i++];

			boolean boolArgument = true;
			String stringArgument = "";

			if (i < args.length && !args[i].startsWith("-")) {
				boolArgument = args[i].equals("1");
				stringArgument = args[i];

				i++;
			}
			
			if (startOutputSpecificOptions){
				runParameters.setOutputFileSpecificOption(param, stringArgument);
				continue;
			}

			switch(param.charAt(1)){
			case('C'):
				runParameters.setProcessAlsoComputeEvents(true);
				break;
			case('a'):
				runParameters.setUpdateStatisticsOnlyIfTheyChangeTooMuch(true);
				runParameters.setComputeAverageFromStatistics(true);
				break;
			case('l'):
				runParameters.setUpdateStatisticsOnlyIfTheyChangeTooMuch(true);
				runParameters.setStatisticModificationUntilUpdate(Float.parseFloat(stringArgument));
				break;
			case('o'):
				runParameters.setOutputFilePrefix(stringArgument);
				break;
			case('i'):
				runParameters.setInputTraceFile( stringArgument );
				break;
			case('D'):
				SimpleConsoleLogger.setDebugEverything(true);
				break;
			case('F'):	
				runParameters.setOutputFormat(stringArgument);
				break;
			case('-'):
				startOutputSpecificOptions = true;
				break;
			case('h'):
				printHelpText(runParameters);
			default:
				System.err.println("Unknown parameter \"" + param + "\"");
				printHelpText(runParameters);
			}
		}

		if (runParameters.getInputTraceFile().length() < 3) {
			printHelpText(runParameters);
		}

		HDTraceConverter converter = new HDTraceConverter();
		converter.process(runParameters);
	}

	/**
	 * Main executable:
	 * 
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		CommandLineInterface cli = new CommandLineInterface();
		cli.run(args);
	}
}
