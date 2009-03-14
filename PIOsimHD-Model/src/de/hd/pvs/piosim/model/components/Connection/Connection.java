
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

/**
 * 
 */
package de.hd.pvs.piosim.model.components.Connection;

import de.hd.pvs.TraceFormat.util.Epoch;
import de.hd.pvs.piosim.model.annotations.Attribute;
import de.hd.pvs.piosim.model.annotations.AttributeGetters;
import de.hd.pvs.piosim.model.annotations.restrictions.NotNegativeOrZero;
import de.hd.pvs.piosim.model.components.superclasses.NetworkComponent;



/**
 * A connection is a single uni-directional interconnect between two NetworkComponents. 
 * 
 * @author Julian M. Kunkel
 */
public class Connection  extends NetworkComponent {
	
	/**
	 * The component this connection is linked to (if any).
	 */
	private NetworkComponent connectedComp;
	
	@Attribute
	@NotNegativeOrZero

	/** The <code>latency</code> of this interconnection. */
	protected Epoch latency = Epoch.ZERO;

	
	@Attribute
	@NotNegativeOrZero
	/** The <code>bandwidth</code> == throughput of this component. */
	protected long bandwidth = -1;
	
	/**
	 * Gets the <code>latency</code>.
	 * 
	 * @return the latency
	 */
	@AttributeGetters 
	public Epoch getLatency() {
		assert(this.latency.getDouble() >= 0);
		return this.latency;
	}
	
	/**
	 * Gets the <code>bandwidth</code>.
	 * 
	 * @return the bandwidth
	 */
	@AttributeGetters public long getBandwidth() {
		
		assert(this.bandwidth > 0);
		
		return this.bandwidth;
	}
	
	/**
	 * Sets the <code>bandwidth</code>.
	 * 
	 * set the bandwidth
	 */
	public void setBandwidth(long b) {
		this.bandwidth = b;
	}
	
	/**
	 * Sets the <code>latency</code>.
	 * 
	 * set the latency
	 */
	public void setLatency(Epoch latency) {
		this.latency = latency;
	}
	
	/**
	 * Get the component this connection is connecting to if any.
	 * @return
	 */
	public NetworkComponent getConnectedComponent() {
		return connectedComp;
	}
	
	/**
	 * Set the connected partner.
	 */
	@Override
	public void setConnectedComponent(NetworkComponent connectedComponent) {
			connectedComp = connectedComponent;
	}


	@Override
	public String toString() {
		if (connectedComp != null){
			return super.toString() + " Bandwidth:" + bandwidth + " Latency:" + latency + " " + " to <" + connectedComp.getIdentifier() + "> ";
		}else{
			return super.toString() + " Bandwidth:" + bandwidth + " Latency:" + latency;
		}
	}
	
	
	@Override
	public String getComponentType() {		
		return Connection.class.getSimpleName();
	}
}
