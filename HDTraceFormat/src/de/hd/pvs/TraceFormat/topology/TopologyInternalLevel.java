package de.hd.pvs.TraceFormat.topology;

import java.util.HashMap;

import de.hd.pvs.TraceFormat.statistics.StatisticSource;

public class TopologyInternalLevel {	
	
	String label;
	
	int position;
	
	final HashMap<String, StatisticSource> statisticSources = new HashMap<String, StatisticSource>();
	
	final HashMap<String, TopologyInternalLevel>    childElements = new HashMap<String, TopologyInternalLevel>();
	
	final TopologyInternalLevel parent;
	
	public TopologyInternalLevel(String label, TopologyInternalLevel parent) {
		this.label = label;
		this.parent = parent;
	}
	
	public HashMap<String, StatisticSource> getStatisticSources() {
		return statisticSources;
	}
	
	public void setStatisticReader(String group, StatisticSource reader){
		statisticSources.put(group, reader);
	}

	public void setChild(String name, TopologyInternalLevel child){
		childElements.put(name, child);
	}
	
	public HashMap<String, TopologyInternalLevel> getChildElements() {
		return childElements;
	}
	
	public TopologyInternalLevel getChild(String name){
		return childElements.get(name);
	}
	
	public int getSize(){
		return childElements.size();
	}
	
	public TopologyInternalLevel getParent() {
		return parent;
	}
	
	private String getUnifiedLabel(){
		// TODO account for more invalid characters.
		return label.toLowerCase().replace(' ', '_');
	}
	
	public String getFilePrefix(){
		if (parent != null){
			return parent.getFilePrefix() + "_" + getUnifiedLabel();
		}
		
		return getUnifiedLabel();
	}
	
	public String getTraceFileName(){
		return getFilePrefix() + ".xml";
	}
	
	public String getStatisticFileName(String group){
		return getFilePrefix() + "_stat_" + group + ".dat";
	}
	
	public String getLabel() {
		return label;
	}
	
	public void setLabel(String label) {
		this.label = label;
	}
	
	public boolean isLeaf(){
		return false;
	}
	
	@Override
	public String toString() {
		if (parent != null){
			return parent.toString() + "-" + label; 
		}
		return label; 
	}
	
	public void setPositionInParent(int pos){
		this.position = pos;
	}
	
	public int getPositionInParent() {
		return position;
	}
}
