
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

/**
 *
 */
package de.hd.pvs.piosim.simulator.components.ServerCacheLayer;

import de.hd.pvs.piosim.simulator.base.SPassiveComponent;
import de.hd.pvs.piosim.simulator.components.IGComponent;
import de.hd.pvs.piosim.simulator.components.NIC.InterProcessNetworkJob;
import de.hd.pvs.piosim.simulator.network.jobs.NetworkIOData;
import de.hd.pvs.piosim.simulator.network.jobs.requests.RequestFlush;
import de.hd.pvs.piosim.simulator.network.jobs.requests.RequestRead;
import de.hd.pvs.piosim.simulator.network.jobs.requests.RequestWrite;

/**
 * Simulates the cache layer on the server.
 *
 * Method invocation flow is as follows:
 * once a new request is received on the server:
 * For write requests:
 * - announceIORequest
 * - For each block of data received on the NIC
 *   Check if the cache likes this data (otherwise it is stored on the NIC, and further RECV is blocked)
 *   - canIPutDataIntoCache
 *   If yes: (otherwise further RECV is blocked)
 *   - writeDataToCache
 *   - dataWrittenCompletelyToDisk
 *
 * For read requests:
 * - announceIORequest
 * - startReadRequest
 * - dataReadCompletelyFromDisk
 * - readDataFragmentSendByNIC
 *
 * @author Julian M. Kunkel
 *
 */
public interface IGServerCacheLayer<Type extends SPassiveComponent>
extends IGComponent<Type>
{

	/**
	 * This method is called to push new Data of a WriteRequest to the Cache Layer.
	 *
	 * @param ioData
	 * @param clientJob
	 */
	public void writeDataToCache(NetworkIOData ioData, InterProcessNetworkJob clientJob, long bytesToWrite, Object userData, IServerCacheLayerJobCallback callback);

	/**
	 * This method is called when data (which should be fragmented) got sent successfully by the NIC.
	 * Right now it is only used to transfer Data read from Disk to Clients (i.e. by ReadRequests).
	 * @param ioData
	 * @param clientJob
	 */
	public void readDataFragmentSendByNIC(RequestRead req, long bytesSendByNIC);

	/**
	 * The server should invoke this method to check if a particular amount of data
	 * can be put into the cache. It can be put into the cache if there is enough cache free
	 * and if the implementation decides to allow this request to be put into the cache.
	 *
	 * @param clientJob
	 * @param bytesOfWrite
	 * @return
	 */
	public boolean canIPutDataIntoCache(RequestWrite clientJob, long bytesOfWrite);

	/**
	 * This method is called once a new read request comes in.
	 *
	 * @param req
	 * @param request
	 */
	public void announceIORequest( RequestRead req, Object userData, IServerCacheLayerJobCallback callback);

	/**
	 * This method is called once a new write request comes in.
	 *
	 * @param req
	 * @param request
	 */
	public void announceIORequest( RequestWrite req, Object userData, IServerCacheLayerJobCallback callback);

	/**
	 * This method is called once a new flush request comes in.
	 *
	 * @param req
	 * @param request
	 */
	public void announceIORequest( RequestFlush req, InterProcessNetworkJob request);

}
