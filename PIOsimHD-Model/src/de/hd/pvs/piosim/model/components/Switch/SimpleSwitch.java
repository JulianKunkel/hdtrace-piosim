
//	Copyright (C) 2008, 2009 Julian M. Kunkel
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

package de.hd.pvs.piosim.model.components.Switch;

import java.util.ArrayList;
import java.util.Collection;

import de.hd.pvs.piosim.model.annotations.Attribute;
import de.hd.pvs.piosim.model.annotations.AttributeGetters;
import de.hd.pvs.piosim.model.annotations.ChildComponents;
import de.hd.pvs.piosim.model.annotations.restrictions.NotNegativeOrZero;
import de.hd.pvs.piosim.model.components.Port.Port;
import de.hd.pvs.piosim.model.components.superclasses.NetworkComponent;


/**
 * A Switch contains of several Ports and allows to transfer data between two arbitrary 
 * Ports.
 * 
 * @author Julian M. Kunkel
 */
public class SimpleSwitch extends Switch {
  /** The total bandwidth of the <code>Switch</code>. 
   * The switch needs some time to transfer data incoming from some ports to target Ports */
	@Attribute
	@NotNegativeOrZero
  private long totalBandwidth = -1;

	/**
	 * The List of Ports available in this Switch. 
	 * Note that they can be connected or not.
	 */
	@ChildComponents
  private ArrayList<Port> ports = new ArrayList<Port>();

  @Override
  public String toString() {
    StringBuffer buf = new StringBuffer();
    for (Port p : getPorts()) {
      buf.append("\n    " + p);
    }

    return super.toString() + " TotalBandwidth: " + totalBandwidth + " Ports: " + buf.toString() + "\n";
  }

  /**
   * Return a list of connected Components.
   * @return
   */
  @Override
  public Collection<NetworkComponent> getConnectedComponents() {
    ArrayList<NetworkComponent> list = new ArrayList<NetworkComponent>();

    for (Port p : getPorts()) {
      list.add(p.getConnectedComponent());
    }

    return list;
  }

  /**
   * @return the totalBandwidth
   */
  @AttributeGetters
  public final long getTotalBandwidth() {
    return totalBandwidth;
  }

  /**
   * Set the total bandwidth the Switch is capable to process.
   */
  public final void setTotalBandwidth(long totalBandwidth) {
    this.totalBandwidth = totalBandwidth;
  }
  
  @Override
  public void addNewPort(Port port) {
  	ports.add(port);  	
  }

  @Override
  public ArrayList<Port> getPorts() {
		return ports;
	}
  
}
