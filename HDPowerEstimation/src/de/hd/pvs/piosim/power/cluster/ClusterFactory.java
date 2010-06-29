//	Copyright (C) 2010 Timo Minartz
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
package de.hd.pvs.piosim.power.cluster;

import java.math.BigDecimal;

import de.hd.pvs.piosim.power.devices.SimpleSwitch;

public class ClusterFactory {

	private ClusterFactory() {

	}

	public static SimpleCluster createSimpleClusterWithSimpleNodes(int size) {
		SimpleCluster cluster = new SimpleCluster();

		for (int i = 0; i < size; ++i)
			cluster.add(NodeFactory.createSimpleNode());

		return cluster;
	}

	public static ExtendedCluster createExtendedClusterWithSimpleNodes(int size) {
		ExtendedCluster cluster = new ExtendedCluster();

		for (int i = 0; i < size; ++i)
			cluster.add(NodeFactory.createSimpleNode());

		cluster.setSimpleSwitch(new SimpleSwitch());

		return cluster;
	}

	public static ExtendedCluster createExtendedClusterWithExtendedNodes(
			int size, BigDecimal powerSupplyEfficiency) {
		ExtendedCluster cluster = new ExtendedCluster();

		for (int i = 0; i < size; ++i)
			cluster.add(NodeFactory.createExtendedNode(powerSupplyEfficiency));

		cluster.setSimpleSwitch(new SimpleSwitch());

		return cluster;
	}

	public static ExtendedCluster createExtendedClusterWithExtendedNodes(
			int size) {
		ExtendedCluster cluster = new ExtendedCluster();

		for (int i = 0; i < size; ++i)
			cluster.add(NodeFactory.createExtendedNode());

		cluster.setSimpleSwitch(new SimpleSwitch());

		return cluster;
	}
}
