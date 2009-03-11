/**
 * 
 */
package de.hd.pvs.traceConverter.Input.Statistics;

import de.hd.pvs.traceConverter.Input.Statistics.ExternalStatisticsGroup.StatisticType;

public class StatisticDescription{
	final StatisticType type;
	final String name;
	
	public StatisticDescription(String name, StatisticType type) {
		this.name = name;
		this.type = type;
	}
	
	public String getName() {
		return name;
	}
	
	public StatisticType getType() {
		return type;
	}
}