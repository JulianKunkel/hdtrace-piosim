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

	private void printHelpText() {
		System.out
				.println("Syntax: \n"
						+ "  [options] -i <trace.xml> [-- [Format specific options] ] \n"
						+ "Options are a subset of: \n"
						+ "  -F <outputFormat>  \n"
						+ "     supported are None and TAU \n"
						+ "  -d <debugOptions> debuging configuration file, requires enabled assertions! \n"
						+ "  -D enable all debugging information, requires enabled assertions! \n"						
						+ " -h show help\n"
						+ "\nBoolean values can be set 0 or 1\n");

		System.exit(1);
	}

	/**
	 * Run the simulation as given by the arguments.
	 * 
	 * @param args
	 * @throws Exception
	 */
	public void run(String[] args) throws Exception {
		int i = 0;

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

			if (param.equals("-i")) {
				runParameters.setInputTraceFile( stringArgument );
			} else if (param.equals("-F")) {
				runParameters.setOutputFormat(stringArgument);
			} else if (param.equals("--")) {
				startOutputSpecificOptions = true;
			} else if (param.equals("-h")) {
				printHelpText();
			} else {
				System.err.println("Unknown parameter \"" + param + "\"");
				printHelpText();
			}
		}

		if (runParameters.getInputTraceFile().length() < 3) {
			printHelpText();
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
