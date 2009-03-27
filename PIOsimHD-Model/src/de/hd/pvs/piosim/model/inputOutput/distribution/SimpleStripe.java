
 /** Version Control Information $Id$
  * @lastmodified    $Date$
  * @modifiedby      $LastChangedBy$
  * @version         $Revision$ 
  */


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

package de.hd.pvs.piosim.model.inputOutput.distribution;

import java.util.HashMap;
import java.util.List;

import de.hd.pvs.piosim.model.annotations.Attribute;
import de.hd.pvs.piosim.model.annotations.restrictions.NotNegativeOrZero;
import de.hd.pvs.piosim.model.components.Server.Server;
import de.hd.pvs.piosim.model.inputOutput.ListIO;
import de.hd.pvs.piosim.model.logging.ConsoleLogger;

/**
 * Very simple round-robin data striping.
 * Works like a RAID-0 with striping of a fixed size.
 * 
 * @author Julian M. Kunkel
 */
public class SimpleStripe extends Distribution {
	
	@Attribute
	@NotNegativeOrZero
	/**
	 * The strip size.
	 * The amount of contiguous data which is accessed on one server. 
	 */
	private long chunkSize = -1;

	/**
	 * Get the strip size.
	 * @return The amount of contiguous data which is accessed on one server.
	 */
	public long getChunkSize() {
		return chunkSize;
	}

	/**
	 * Set the strip size:
	 * the amount of contiguous data which is accessed on one server.
	 * @param chunkSize
	 */
	public void setChunkSize(long chunkSize) {
		this.chunkSize = chunkSize;
	}
	
	/**
	 * Return the server to manage a particular byte.
	 * @param offset
	 * @param serverCount
	 * @return
	 */
	private int getServerNumberForOffset(long offset, int serverCount){
		return (int) ((offset / chunkSize) % serverCount);
	}
	
	/**
	 * Compute the physical offset of a logical byte.
	 * 
	 * @param offset
	 * @param serverCount
	 * @return
	 */
	private long logicalToPhysicalOffset(long offset, int serverCount){
		return ((offset / chunkSize) / serverCount) * chunkSize + (offset % chunkSize);
	}
	
	@Override
	public HashMap<Server, ListIO> distributeIOOperation(ListIO iolist,
			List<Server> serverList) {
		assert(chunkSize > 0);
		
		final int serverCount = serverList.size();
		
		if (serverCount == 0)
			throw new IllegalArgumentException("No I/O server available!");
		
		/* maps server Number to ListIO */
		HashMap<Integer, ListIO> mapServerNumberRequest = new HashMap<Integer, ListIO>();

		for(ListIO.SingleIOOperation operation: iolist.getIOOperations()){
			final long offset = operation.getOffset();
			final long size   = operation.getAccessSize();
			if ( size == 0){
				continue;
			}
			/* determine servers */
			final int startServer = getServerNumberForOffset(offset, serverCount);
			final int numberOfChunksHit = (int) ((size+chunkSize-1 + offset % chunkSize) 
					/ chunkSize);
			final int numberOfFullChunksHit = (int) ((size + (offset % chunkSize)) 
					/ chunkSize); 
			

			final long alignedStartOffset = (offset / chunkSize) * chunkSize;
			final int lastServer = (numberOfChunksHit -1) % serverCount ;
			final long sizePerServer =  chunkSize * (numberOfFullChunksHit / serverCount);
			final int unevenChunks = (numberOfFullChunksHit % serverCount);
			final long firstServerUnalignedData = (offset+ chunkSize -1) / 	chunkSize * chunkSize - offset;
			
			ConsoleLogger.getInstance().debug(this, " ListIO <" + offset + "," + size + ">\n" + "\tNumberOfChunks: " + 
					numberOfChunksHit + " FullChunks: " + numberOfFullChunksHit + " unalignedData " + firstServerUnalignedData
					+ " sizePerServer " + sizePerServer + "  alignedStartOffset: " + alignedStartOffset);
			
			int unalignedBlockOnFirstServer = 0; 
			
			for(int i=0; i < serverCount; i++){
				int aktServer = ( startServer + i ) % serverCount;
												
				/* Compute the actual physical access data */
				long physicalOffsetForThisServer = 0;
				long myOffset;
				if( i == 0){
					myOffset = offset;
				}else{
					myOffset = alignedStartOffset + i * chunkSize;
				}
				physicalOffsetForThisServer = logicalToPhysicalOffset(myOffset, serverCount);
				
				long physicalSizeForThisServer = sizePerServer;
								
				if ( lastServer == i) {
					/* compute remainder */
					final long remainder =  size - (numberOfFullChunksHit * chunkSize + 
							firstServerUnalignedData);
					physicalSizeForThisServer += remainder ; //(size + offset) - ((size+offset) / chunkSize * chunkSize)
					ConsoleLogger.getInstance().debug(this, "last server: " + i + " " + remainder + " " + physicalSizeForThisServer);
				}
				
				if(i == 0 && firstServerUnalignedData > 0){ 
					physicalSizeForThisServer += firstServerUnalignedData; 
					unalignedBlockOnFirstServer = 1;
				}else if(i < unevenChunks + unalignedBlockOnFirstServer){
					physicalSizeForThisServer += chunkSize;
				}
				
				if (physicalSizeForThisServer == 0){
					break;
				}			
				
				ListIO aktIO = mapServerNumberRequest.get(aktServer);
				if (aktIO == null){
					aktIO = new ListIO();
					mapServerNumberRequest.put(aktServer, aktIO);
				}
				aktIO.addIOOperation(physicalOffsetForThisServer, physicalSizeForThisServer);
				
				ConsoleLogger.getInstance().debug(this, "Data is hitting Server: " + aktServer + " Physical Offset: " + 
						physicalOffsetForThisServer
						+ " Size: " + physicalSizeForThisServer);
			}
		}

		/* now create an output list */
		HashMap<Server, ListIO> out = new HashMap<Server, ListIO>();
		for(Integer val: mapServerNumberRequest.keySet()){
			out.put(serverList.get(val), mapServerNumberRequest.get(val));
		}
		
		return out;
	}

}
