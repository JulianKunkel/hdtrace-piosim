package de.viewer.common;

import java.awt.Color;

import de.hd.pvs.TraceFormat.trace.ITraceEntry;


public class HeatMap {
	
	/**
	 * Contain the actual variable names
	 */
	private final String [] attributeNames;

	private final String [] operations;
	
	/**
	 * Store the min and max value
	 */
	private double maxValue;
	private double minValue;
	
	private HeatMap(String [] attributeNames, String [] operations) {
		this.attributeNames = attributeNames;
		this.operations = operations;
	}
	
	public static HeatMap createHeatMap(String heatMap){
		// sort of tolerate multiple spaces 
		heatMap = heatMap.trim().replaceAll(" +", " ");
		
		String names = heatMap.replaceAll("[+*]", " ").trim().replaceAll(" +", " ");
			 
		String [] attributeNames = names.split(" ");
					
		String data = heatMap.replaceAll("[A-Za-z0-9]+", "p").trim().replace(" ", "");
		System.out.println("heatmap attributes: " + names  + " instructions: "  + data);
		
		String [] operations = data.split(" ");
		
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
	
	public String[] getOperations() {
		return operations;
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
	
	/**
	 * Determine the color for the given object,
	 * if it contains no attributes as required, then use the given color.
	 * 
	 * @param color
	 * @param obj
	 * @return
	 */
	public Color determineColor(Color color, ITraceEntry obj){
		// check if obj has the required attributes we need
		
		// compute the values with the attributes
		
		// use determineColor method to compute color based on attributes and mathematical expression		 
		
		return color;
	}

	
	/**
	 * Reset the heat map colors for another iteration
	 */
	public void resetHeatMapColors(){
		minValue = Double.MAX_VALUE;
		maxValue = Double.MIN_VALUE;
	}

}
