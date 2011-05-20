package de.hdTraceInput;

import java.util.HashMap;

import de.hd.pvs.TraceFormat.statistics.StatisticsGroupEntry;

/**
 * Converts a textual expression into a mathematical expression
 * and apply them
 * 
 * @author julian
 */
public class MathematicalExpression {

	/**
	 * The metrics which are required to compute the function
	 */
	private String [] requiredMetrics = {"NET_IN", "NET_OUT"};
	
	public MathematicalExpression(String textualExpression) {

	}

	public String textualRepresentation() {
		return "1";
	}
	
	public String[] getRequiredMetrics() {
		return requiredMetrics;
	}
	
	public double computeFunction(double [] lastValues, String [] variableNames){
		return 1;
	}

}
