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
package de.hd.pvs.piosim.power.replay;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import de.hd.pvs.piosim.power.acpi.ACPIComponent;
import de.hd.pvs.piosim.power.cluster.Node;

public class ComponentConsumption {
	
	private Map<String,BigDecimal[]> map = new HashMap<String,BigDecimal[]>();

	public int getSize() {
		return map.size();
	}

	public BigDecimal get(String replayName, ACPIComponent component, int i) {
		return map.get(encode(replayName,component))[i];
	}

	private String encode(String replayName, ACPIComponent component) {
		return "" + replayName.hashCode() + component.hashCode();
	}
	
	private String encode(String replayName, Node node) {
		return "" + replayName.hashCode() + node.hashCode();
	}

	public void add(String name, ACPIComponent component, BigDecimal[] powerConsumption) {
		map.put(encode(name,component), powerConsumption);		
	}
	
	public void add(String name, Node node, BigDecimal[] powerConsumption) {
		map.put(encode(name,node), powerConsumption);		
	}

	public BigDecimal get(String replayName, Node node, int i) {
		return map.get(encode(replayName,node))[i];
	}

}
