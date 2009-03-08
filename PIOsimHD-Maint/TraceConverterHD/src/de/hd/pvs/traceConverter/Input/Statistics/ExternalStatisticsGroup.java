package de.hd.pvs.traceConverter.Input.Statistics;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

public class ExternalStatisticsGroup{
	String name;
	
	// maps contained statistics to type
	final HashMap<String, String> statisticTypeMap = new HashMap<String, String>();
	
	public void addStatistic(String name, String type){
		statisticTypeMap.put(name, type);
	}
	
	public Collection<String> getStatistics() {
		return statisticTypeMap.keySet();
	}
	
	public String getType(String statistic){
		return statisticTypeMap.get(statistic);
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
}
