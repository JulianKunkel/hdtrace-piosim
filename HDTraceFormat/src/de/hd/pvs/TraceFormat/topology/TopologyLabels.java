package de.hd.pvs.TraceFormat.topology;

import java.util.ArrayList;

public class TopologyLabels {
	private final ArrayList<String> labels = new ArrayList<String>();
	
	public void addLabelOfNextDepth(String label) {
		labels.add(label);
	}
	
	public ArrayList<String> getLabels() {
		return labels;
	}
}
