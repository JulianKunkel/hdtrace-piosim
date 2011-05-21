package de.viewer.common;

import java.awt.Color;

import de.hd.pvs.TraceFormat.TracableObjectType;
import de.hd.pvs.TraceFormat.trace.ITraceEntry;
import de.hd.pvs.TraceFormat.trace.TraceEntry;
import de.viewer.timelines.FilterTokenInterface.DoubleFilterToken;
import de.viewer.timelines.FilterTokenInterface.FilterTokenType;
import de.viewer.timelines.FilterTokenInterface.StringFilterToken;


public class HeatMap {
	
	/**
	 * Contain the actual variable names
	 */
	private final String [] attributeNames;

	private final char [] operations;
	
	/**
	 * Store the min and max value
	 */
	private double maxValue;
	private double minValue;
	private double diff;
	
	private HeatMap(String [] attributeNames, char [] operations) {
		this.attributeNames = attributeNames;
		this.operations = operations;
	}
	
	public static HeatMap createHeatMap(String heatMap){
		// sort of tolerate multiple spaces 
		heatMap = heatMap.trim().replaceAll(" +", " ");
		
		String names = heatMap.replaceAll("[+*^,]", " ").trim().replaceAll(" +", " ");
			 
		String [] attributeNames = names.split(" ");
					
		String data = heatMap.replaceAll("[A-Za-z0-9]+", "p").trim().replace(" ", "");
		
		char [] operations = data.toCharArray();		
		
		// System.out.println(operations.length + " " + attributeNames.length + " " + data + " - " + names);
		if( operations.length + 1 != attributeNames.length * 2 ){
			System.err.println("The number of operations does not match the number of attributes");
			return null;
		}
		
		return new HeatMap(attributeNames, operations); 
	}

	public boolean isSingleAttributeWithName(String name){
		return attributeNames[0].equals(name) && attributeNames.length == 1;
	}
	
	public String[] getAttributeNames() {
		return attributeNames;
	}
	

	/**
	 * This function is used to draw statistics.
	 * The value must been between scaled between 0 and 1.
	 * @param value
	 * @return
	 */	
	public Color determineColor(float value){
		// the higher the value is in the direction to maxValue, the more redish!
		assert(value >= 0.0f);
		assert(value <= 1.0f);
		
		if(value <= 0.5){
			return new Color(0,  255 - (int) (255 * value * 2), 0);			
		}else{		
			return new Color( (int) (255 * value), 0, 0);
		}
	}

	/**
	 * Change the global min/max
	 * @param val
	 */
	private void updateMinMaxValues(double val){
		maxValue = maxValue < val ? val: maxValue;
		minValue = minValue < val ? minValue : val;
	}
	
	private Double getAttributeValue(TraceEntry e, String attribute){
		final String strAttrib = e.getAttribute(attribute); 

		if(strAttrib == null)
			return null;
		
		return Double.parseDouble(strAttrib);
	}
	
	/**
	 * Determine the color for the given object,
	 * if it contains no attributes as required, then use the given color.
	 * 
	 * @param color
	 * @param obj
	 * @return
	 */
	public Color determineColor(Color color, ITraceEntry object){
		// check if obj has the required attributes we need
		
		if(object.getType() != TracableObjectType.EVENT &&  object.getType() != TracableObjectType.STATE){
			return color;			
		}
		
		final TraceEntry entry = (TraceEntry) object;
		
		// fetch all attributes and store them in values
		double [] values = new double[attributeNames.length];		
				
		for(int i=0; i < attributeNames.length; i++){
			final Double val = getAttributeValue(entry, attributeNames[i]);
			
			// if val is not found, then this entry does not have the required attribute => the original color must be returned
			if (val == null)
				return color;
			
			values[i] = val;
		}		
		
		// compute the mathematical result from the Polnish expression
		
		// the index at which we currently are in the value array.
		int posInVal = values.length - 1;
		
		// process from right to left
		for(int i = operations.length - 1; i >= 0 ; i--){
			
			final char op = operations[i];
			
			if(op == 'p'){
				posInVal--;
				continue;
			}

			// if it is a real op take the right two values
			
			if (op == '+'){
				values[posInVal + 1] = values[posInVal+1] + values[posInVal+2];
			}else if (op == '*'){
				values[posInVal + 1] = values[posInVal+1] * values[posInVal+2]; 
			}else if (op == '^'){
				values[posInVal + 1] = values[posInVal+1] > values[posInVal+2] ? values[posInVal+1] : values[posInVal+2]; 
			}else if (op == ','){
				values[posInVal + 1] = values[posInVal+1] < values[posInVal+2] ? values[posInVal+1] : values[posInVal+2]; 
			}
		}
		
		// use determineColor method to compute color based on attributes and mathematical expression
		// value is now stored on the first entry of the array
		double result = values[0];
		
		// first round, update maximum values
		if(diff == 0){
			updateMinMaxValues(result);
			return color;
		}else{
			return determineColor( (float) ((result - minValue) / diff) );	
		}		
	}

	
	/**
	 * Reset the heat map colors for another iteration
	 */
	public void resetHeatMapColors(){
		minValue = Double.MAX_VALUE;
		maxValue = 0
		;
		diff = 0;
	}
	
	public void firstIterationDone(){
		diff = (maxValue - minValue);
		
		if(diff == 0.0){
			System.out.println("Difference between min and max is 0 => heatmap is disabled");
		}
	}

}
