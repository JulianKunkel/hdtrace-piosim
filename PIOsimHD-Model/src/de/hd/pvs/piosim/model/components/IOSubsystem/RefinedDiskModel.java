
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

package de.hd.pvs.piosim.model.components.IOSubsystem;

import de.hd.pvs.TraceFormat.util.Epoch;
import de.hd.pvs.piosim.model.annotations.Attribute;
import de.hd.pvs.piosim.model.annotations.restrictions.NotNegativeOrZero;
import de.hd.pvs.piosim.model.annotations.restrictions.NotNull;

/**
 * RefinedDiskModel takes more parameters into account: http://www.storagereview.com/guide2000/ref/hdd/index.html
 * Uses a modern disk layout which assumes amongst other features for instance sector-interleaving.
 * If access to a particular file is performed which is considered to be near the last access position,
 * then the trackToTrackSeekTime is used.  
 * 
 * @author Julian M. Kunkel
 */
public class RefinedDiskModel extends IOSubsystem {

	@Attribute
	@NotNull
	/**
	 * The minimum access time which must be spent before the access arm is moved to position.
	 */
	private Epoch  trackToTrackSeekTime = Epoch.ZERO;
	
	@Attribute
	@NotNull
	/**
	 * The average time to place the access arm 
	 */
	private Epoch  averageSeekTime = Epoch.ZERO;
	
	
	@Attribute
	@NotNegativeOrZero
	/**
	 * Rotations per minute, used to calculate rotational latency.
	 */
	private int RPM = -1;
	
	@Attribute
	@NotNegativeOrZero
	/**
	 * Sustained transfer rate, sequential transfer rate, already takes trackToTrackSeekTime into account for 
	 * sequential reads.
	 * 
	 * see http://www.storagereview.com/guide2000/ref/hdd/perf/perf/spec/transSTR.html for computation.
	 */
	private long sequentialTransferRate = -1;		

	@Attribute
	@NotNegativeOrZero
	/**
	 * The position of the last access within the access is considered to be close to the last access i.e. track-to-track
	 * seek time can be applied.
	 */
	private long positionDifferenceConsideredToBeClose = -1;
	
	/**
	 * @param averageSeekTime the averageSeekTime to set
	 */
	public void setAverageSeekTime(Epoch averageSeekTime) {
		this.averageSeekTime = averageSeekTime;
	}
	
	/**
	 * @param sequentialTransferRate the sequentialTransferRate to set
	 */
	public void setSequentialTransferRate(long sequentialTransferRate) {
		this.sequentialTransferRate = sequentialTransferRate;
	}
	
	/**
	 * @param rpm the rPM to set
	 */
	public void setRPM(int rpm) {
		RPM = rpm;
	}
	
	/**
	 * @param trackToTrackSeekTime the trackToTrackSeekTime to set
	 */
	public void setTrackToTrackSeekTime(Epoch trackToTrackSeekTime) {
		this.trackToTrackSeekTime = trackToTrackSeekTime;
	}
	
	/**
	 * @return the averageSeekTime
	 */
	public Epoch getAverageSeekTime() {
		return averageSeekTime;
	}
	
	/**
	 * @return the rPM
	 */
	public int getRPM() {
		return RPM;
	}
	
	/**
	 * @return the sequentialTransferRate
	 */
	public long getSequentialTransferRate() {
		return sequentialTransferRate;
	}
	
	/**
	 * @return the trackToTrackSeekTime
	 */
	public Epoch getTrackToTrackSeekTime() {
		return trackToTrackSeekTime;
	}
	
	/**
	 * @return the positionDifferenceConsideredToBeClose
	 */
	public long getPositionDifferenceConsideredToBeClose() {
		return positionDifferenceConsideredToBeClose;
	}
	
	/**
	 * @param positionDifferenceConsideredToBeClose the positionDifferenceConsideredToBeClose to set
	 */
	public void setPositionDifferenceConsideredToBeClose(long positionDifferenceConsideredToBeClose) {
		this.positionDifferenceConsideredToBeClose = positionDifferenceConsideredToBeClose;
	}
}
