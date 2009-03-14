/**
 * 
 */
package de.hd.pvs.traceConverter.Input.Statistics;


public class StatisticDescription{
	final StatisticType type;
	final String name;
	
	final String unit;
	final int multiplier;
	
	public StatisticDescription(String name, StatisticType type, String unit, int multiplier) {
		this.name = name;
		this.type = type;
		this.multiplier = multiplier;
		this.unit = unit;
	}
	
	public String getName() {
		return name;
	}
	
	public StatisticType getType() {
		return type;
	}
	
	public int getMultiplier() {
		return multiplier;
	}
	
	public String getUnit() {
		return unit;
	}
}