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
	 * Prefix of all files which will be created by the TraceConverter
	 */
	String outputFilePrefix = "/tmp/converted";
	
	/**
	 * If set true then the statistics are only written to the output converter when they change 
	 * more than statisticModificationUntilUpdate
	 */
	boolean updateStatisticsOnlyIfTheyChangeTooMuch = false; 
	
	/**
	 * if true AND updateStatisticsOnlyIfTheyChangeTooMuch, then the average of the value
	 * is written, otherwise the current (last) value gets written if the statistics fluctuated too much.
	 */
	boolean computeAverageFromStatistics = false;
	
	/**
	 * Used if updateStatisticsOnlyIfTheyChangeTooMuch is true. Then the statistics are only written 
	 * to the output converter when they change 
	 * more than statisticModificationUntilUpdate
	 */
	float statisticModificationUntilUpdate = 0.01f;	

	
	/**
	 * Determines whether compute events are processed or ignored.
	 */
	boolean processAlsoComputeEvents = false;
	
	public void setProcessAlsoComputeEvents(boolean processAlsoComputeEvents) {
		this.processAlsoComputeEvents = processAlsoComputeEvents;
	}
	
	public boolean isProcessAlsoComputeEvents() {
		return processAlsoComputeEvents;
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
	
	public void setOutputFilePrefix(String outputFilePrefix) {
		this.outputFilePrefix = outputFilePrefix;
	}
	
	public String getOutputFilePrefix() {
		return outputFilePrefix;
	}
	
	public void setStatisticModificationUntilUpdate(
			float statisticModificationUntilUpdate) {
		this.statisticModificationUntilUpdate = statisticModificationUntilUpdate;
	}
	
	public void setUpdateStatisticsOnlyIfTheyChangeTooMuch(
			boolean updateStatisticsOnlyIfTheyChangeTooMuch) {
		this.updateStatisticsOnlyIfTheyChangeTooMuch = updateStatisticsOnlyIfTheyChangeTooMuch;
	}
	
	public boolean isUpdateStatisticsOnlyIfTheyChangeTooMuch() {
		return updateStatisticsOnlyIfTheyChangeTooMuch;
	}
	
	public float getStatisticModificationUntilUpdate() {
		return statisticModificationUntilUpdate;
	}
	
	public boolean isComputeAverageFromStatistics() {
		return computeAverageFromStatistics;
	}
	
	public void setComputeAverageFromStatistics(
			boolean computeAverageFromStatistics) {
		this.computeAverageFromStatistics = computeAverageFromStatistics;
	}
}
