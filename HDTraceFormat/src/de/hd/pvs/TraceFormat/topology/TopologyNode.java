
/** Version Control Information $Id$
 * @lastmodified    $Date$
 * @modifiedby      $LastChangedBy$
 * @version         $Revision$ 
 */

//	Copyright (C) 2009 Julian M. Kunkel
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


package de.hd.pvs.TraceFormat.topology;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import de.hd.pvs.TraceFormat.statistics.StatisticsSource;
import de.hd.pvs.TraceFormat.trace.TraceSource;

public class TopologyNode {	

	private String name;

	/**
	 * Real label of this topology node
	 */
	private String label;

	private final HashMap<String, StatisticsSource> statisticsSources = new HashMap<String, StatisticsSource>();

	private final HashMap<String, TopologyNode>    childElements = new HashMap<String, TopologyNode>();
	
	private final TopologyNode parent;

	private TraceSource traceSource = null;

	/**
	 * Create a node as child of a parent. Also add this node to the parent if necessary.
	 * @param text
	 * @param parent
	 */
	public TopologyNode(String text, TopologyNode parent, String label) {
		this.name = text;
		this.parent = parent;
		this.label = label;
		
		assert(label != null);
		assert(name != null);
		
		if(parent != null){
			parent.setChild(text, this);
		}
	}

	public HashMap<String, StatisticsSource> getStatisticsSources() {
		return statisticsSources;
	}

	public StatisticsSource getStatisticsSource(String group) {
		return statisticsSources.get(group);
	}


	public void setStatisticsReader(String group, StatisticsSource reader){
		statisticsSources.put(group, reader);
	}

	private void setChild(String name, TopologyNode child){
		if(childElements.containsKey(name))
			throw new IllegalArgumentException("Child element already present in topology");
		childElements.put(name, child);
	}

	public HashMap<String, TopologyNode> getChildElements() {
		return childElements;
	}

	public TopologyNode getChild(String name){
		return childElements.get(name);
	}

	public int getSize(){
		return childElements.size();
	}

	public TopologyNode getParent() {
		return parent;
	}

	/**
	 * Return the label but remove invalid characters in the label
	 * @return 
	 */
	private String getUnifiedLabel(){		
		return name.replaceAll("[^a-zA-Z0-9-.]", "");
	}

	/**
	 * Construct the file prefix of this topology entry.
	 * This does not include the directory the files are located. 
	 * @return
	 */
	public String getFilePrefix(){
		if (parent != null){
			return parent.getFilePrefix() + "_" + getUnifiedLabel();
		}

		return getUnifiedLabel();
	}

	/**
	 * Construct the trace file name of this topology entry.
	 * This does not include the directory the files are located. 
	 * @return
	 */
	public String getTraceFileName(){
		return getFilePrefix() + ".trc";
	}

	/**
	 * Construct the static group file name of a particular group 
	 * located under this topology entry.
	 * This does not include the directory the files are located. 
	 * @return
	 */
	public String getStatisticFileName(String group){
		return getFilePrefix() + "_" + group + ".stat";
	}

	public String getName() {
		return name;
	}

	public void setText(String text) {
		this.name = text;
	}

	/**
	 * @return true if no other children exist
	 */
	public boolean isLeaf(){
		return childElements.size() == 0;
	}

	final public String toRecursiveString() {
		if (parent != null){
			return parent.toRecursiveString() + "-" + name; 
		}
		return name; 
	}


	/**
	 * Recursively get all child topology including this topology.
	 * TODO: could be done with an enumeration.
	 * @return
	 */
	public ArrayList<TopologyNode> getSubTopologies(){
		ArrayList<TopologyNode> sub = new ArrayList<TopologyNode>();
		sub.add(this);
		for(TopologyNode child: childElements.values()){
			sub.addAll(child.getSubTopologies());
		}

		return sub;
	}

	/**
	 * Return the parent topologies up to root, the first element is a child of root.
	 *  
	 * @return
	 */
	public LinkedList<TopologyNode> getParentTopologies(){
		LinkedList<TopologyNode> list = new LinkedList<TopologyNode>();
		TopologyNode par = parent;
		if(par == null)
			return list;
		while(par.parent != null){
			list.addFirst(par);
			par = par.parent;
		}
		return list;
	}

	public TraceSource getTraceSource() {
		return traceSource;
	}

	public void setTraceSource(TraceSource traceSource) {
		this.traceSource = traceSource;
	}

	/**
	 * Check if this topology entry has a parent entry. 
	 * @return true if yes.
	 */
	public boolean hasParent(){
		return parent != null;
	}

	public LinkedList<TopologyNode> getChildrenOfDepth(int depth){
		// drill down to the rank topology level with a BFS
		LinkedList<TopologyNode> bfsTopos = new LinkedList<TopologyNode>();

		bfsTopos.add(this);
		for(int curDepth = 0; curDepth <= depth; curDepth++){
			final LinkedList<TopologyNode> tmp = new LinkedList<TopologyNode>();

			for(TopologyNode cur: bfsTopos){
				tmp.addAll(cur.getChildElements().values());
			}
			bfsTopos = tmp;
		}
		
		return bfsTopos;
	}
	
	 /* Search node and parent nodes recursively to find the topology node which is labeled with
	 * the given text.
	 * @param text
	 * @return
	 */
	public TopologyNode getNodeWithTopologyLabelRecursivly(String text){		
		TopologyNode cur = this;
		
		// lookup parents until label is found		
		while(cur != null){
			if(label.equalsIgnoreCase(text)){
				// found correct node:
				return cur;
			}
			
			cur = cur.getParent();
		}
		
		return null;
	}

	public String getLabel() {
		return label;
	}	
}

