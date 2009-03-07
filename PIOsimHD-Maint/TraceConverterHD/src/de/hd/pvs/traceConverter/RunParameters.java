//	Copyright (C) 2009, 2009 Julian M. Kunkel
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

package de.hd.pvs.traceConverter;

import java.util.Properties;

/**
 * This class stores relevant parameters, typically parameters are read by the
 * command line interface.
 * 
 * @author Julian M. Kunkel
 */
public class RunParameters {
	/**
	 * the file is the input trace file
	 */
	String inputTraceFile = "";

	/**
	 * The trace output format
	 */
	String outputFormat = "Tau";

	/**
	 * Output file format specific options
	 */
	Properties outputFileSpecificOptions = new Properties();
	
	/**
	 * If true then all debug messages are printed.
	 */
	boolean debugEverything = false;
	
	
	public void setDebugEverything(boolean debugEverything) {
		this.debugEverything = debugEverything;
	}

	public boolean isDebugEverything() {
		return debugEverything;
	}

	public Properties getOutputFileSpecificOptions() {
		return outputFileSpecificOptions;
	}
	
	public void setOutputFileSpecificOption(String option, String value){
		outputFileSpecificOptions.setProperty(option, value);
	}
	
	/**
	 * Set the output trace format
	 * 
	 * @param outputFormat
	 */
	public void setOutputFormat(String outputFormat) {
		this.outputFormat = outputFormat;
	}

	public String getOutputFormat() {
		return outputFormat;
	}

	/**
	 * @return the traceFile
	 */
	public String getInputTraceFile() {
		return inputTraceFile;
	}

	/**
	 * @param traceFile
	 *            the traceFile to set
	 */
	public void setInputTraceFile(String traceFile) {
		this.inputTraceFile = traceFile;
	}
}
