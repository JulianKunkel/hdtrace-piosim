
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

/**
 *
 */
package de.hd.pvs.piosim.simulator;

import java.util.ArrayList;
import java.util.Collections;

import de.hd.pvs.TraceFormat.util.Epoch;
import de.hd.pvs.piosim.model.dynamicMapper.CommandType;
import de.hd.pvs.piosim.model.dynamicMapper.DynamicTraceEntryToCommandMapper;



/**
 * Simple command line interface to control the simulator.
 * Reads command line arguments and starts the simulation.
 *
 * @author Julian M. Kunkel
 */
public class CommandLineInterface {

	private void printHelpText(){
		System.out.println("Syntax: \n" +
				"  [options] -i <model.mxml> \n"+
				"Options are a subset of: \n" +
				"  --load-program-on-demand Load the program on demand (otherwise load it to RAM)\n " +
				"  -m <time> min time in s between trace entries to create a new compute job" +
				"  -t  enable tracing\n" +
				"  -tcs enable client step tracing\n" +
				"  -ts enable server tracing\n" +
				"  -ti enable internal tracing\n" +
				"  -tf <tracefile> file to store trace\n" +
				"  -d <debugOptions> debuging configuration file, requires enabled assertions! \n" +
				"  -D enable all debugging information, requires enabled assertions! \n" +
				" -h show help\n" +
				"\nBoolean values can be set 0 or 1, e.g. \"-tcs 0\" is also valid\n"
		);

		System.exit(1);
	}

	/**
	 * Run the simulation as given by the arguments.
	 *
	 * @param args
	 * @throws Exception
	 */
	public void run(String[] args) throws Exception{
    int i = 0;

    String inp_filename = null;

    // create runtime parameters based on the arguments.
    RunParameters runParameters = new RunParameters();


    while (i < args.length && args[i].startsWith("-")) {
        String param = args[i++];

        boolean boolArgument =  true;
        String  stringArgument = "";

        if(i < args.length &&  ! args[i].startsWith("-")) {
        	boolArgument = args[i].equals("1");
        	stringArgument = args[i];

        	i++;
        }

        if(param.equals("-i")) {
        	inp_filename = stringArgument;
        }else if(param.equals("-m")){
        	runParameters.setMinTimeDiffForComputation(Epoch.parseTime(stringArgument));
        }else if(param.equals("--load-program-on-demand")){
        	runParameters.setLoadProgramToRamOnLoad(false);
        }else if (param.equals("-tcs")){
        	runParameters.setTraceClientSteps(boolArgument);
        }else if (param.equals("-t")){
        	runParameters.setTraceEnabled(boolArgument);
        }else if (param.equals("-ts")){
        	runParameters.setTraceServers(boolArgument);
        }else if (param.equals("-ti")){
        	runParameters.setTraceInternals(boolArgument);
        }else if (param.equals("-tf")){
        	runParameters.setTraceFile(stringArgument);
        }else if (param.equals("-D")) {
        	runParameters.setDebugEverything(true);
        }else if (param.equals("-d")) {
        	runParameters.setLoggerDefinitionFile(stringArgument);
        }else if(param.equals("-h")) {
        	printHelpText();
        }else {
        		System.err.println("Unknown parameter \"" + param + "\"");
        		printHelpText();
        }
    }

		if(inp_filename == null) {
			printHelpText();
		}

		// load model and programs:
		SimulationResults results = Simulator.runProjectDescription(inp_filename, runParameters);

		// print trace mapping:
		System.out.println("Mapping from XMLCommands to model commands");
		ArrayList<String> xmlCommands = new ArrayList<String>(DynamicTraceEntryToCommandMapper.getTraceEntryMapping().keySet());
		Collections.sort(xmlCommands);
		for(String xmlCmd: xmlCommands){
			// print mapping from real command names to implementing command.
			final CommandType cmdType = DynamicTraceEntryToCommandMapper.getTraceEntryMapping().get(xmlCmd);
			System.out.println( "  " + xmlCmd + ": " + cmdType);
		}

		// print the results
		SimulationResultSerializer simResultSerializer = new SimulationResultSerializer();
		System.out.println(simResultSerializer.serializeResults(results));
	}

	/**
	 * Main executable:
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		CommandLineInterface cli = new CommandLineInterface();
		cli.run(args);
	}
}
