
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

package de.hd.pvs.piosim.simulator.event;

import de.hd.pvs.piosim.simulator.component.NIC.GNIC;
import de.hd.pvs.piosim.simulator.network.SingleNetworkJob;

/**
 * A network message consists of several packets. 
 * 
 * @author Julian M. Kunkel
 *
 */
public class Message extends NetworkEventType {
	/**
	 * The total size of this message.
	 */
	final private long totalSize;
	
	/**
	 * The NetworkJob realized by this message. 
	 */
	final private SingleNetworkJob job;
			
	/**
	 * How much data has been already packed into smaller packets. 
	 */
	/* necessary to manage send and receive */
	private long createdPosition = 0;
	
	/**
	 * How much data got received from the target NIC.
	 */
	private long receivedPosition = 0;
	
	/** this is Data which is already available to transfer by the NIC, but
	 * not transferred so far. It can be transferred by the NIC as soon as possible. 
	 */
	private long availableDataPosition = 0;
	
	/**
	 * The NIC for the outgoing message.
	 */
	private final GNIC outgoingNIC;
	
	/**
	 * Return the size of this message
	 */
	public long getTotalSize() {
		return totalSize;
	}
	
	/**
	 * Splits the message into a new smaller part.
	 * @param splitSize How much data can be contained at most in the MessagePart
	 */
	public MessagePart createNextMessagePart(long splitSize){
		long position = createdPosition;
		long remainingAvailBytes = (availableDataPosition - position);
		if ( remainingAvailBytes == 0){
			/* send complete OR data is not availabe, yet */
			return null;
		}
		long size     = (remainingAvailBytes > splitSize) ? splitSize : remainingAvailBytes;
		MessagePart part = new MessagePart(this, size, position);
		createdPosition = position + size;
		return part;
	}
	
	/**
	 * Receive a MessagePart.
	 * @param part
	 */
	public void receivePart(MessagePart part){
		receivedPosition += part.getSize();
	}
		
	/**
	 * This constructor can be used to create messages which data is available upon call. 
	 * @param size
	 * @param job
	 * @param flowPart
	 */
	public Message(long size, SingleNetworkJob job, GNIC nic) {
		this.totalSize = size;
		this.availableDataPosition = size;
		this.job = job;
		this.outgoingNIC = nic;
	}
	
	/**
	 * This constructor is used to create messages which data is only 
	 * partially available upon call. However, transfer should start immediately. 
	 * @param size
	 * @param job
	 * @param currentPosition How much data is already available from the Message?
	 */
	public Message(long size, SingleNetworkJob job, long currentPosition, GNIC nic) {
		this.totalSize = size;
		this.availableDataPosition = currentPosition;
		this.job = job;
		this.outgoingNIC = nic;
	}
	
	/**
	 * This function signals the availability of more data (which can be sent by a NIC)
	 * @param count Data created.
	 */
	public void appendAvailableDataToSend(long count){
		this.availableDataPosition += count;
		assert(this.availableDataPosition <= this.totalSize);
	}		
	
	/**
	 * Return the parent NetworkJob realized by this message.
	 */
	public SingleNetworkJob getNetworkJob() {
		return job;
	}
	
	/**
	 * @return the availableDataPosition
	 */
	public long getAvailableDataPosition() {
		return availableDataPosition;
	}

	/**
	 * Checks if the message got received completely.
	 * @return
	 */
	public boolean isReceivedCompletely(){
		return (receivedPosition == totalSize);
	}
	
	/**
	 * Check if all available data got split into parts.
	 * @return
	 */
	public boolean isAllAvailableDataSplitIntoParts(){
		return (this.availableDataPosition == this.createdPosition);
	}	

	/**
	 * Check if all data which should be transferred is available.
	 * @return
	 */
	public boolean isAllMessageDataAvailable(){
		return this.availableDataPosition == this.totalSize; 
	}
	
	public GNIC getOutgoingNIC() {
		return outgoingNIC;
	}
}
