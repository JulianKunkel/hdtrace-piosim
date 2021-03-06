
 /** Version Control Information $Id: IOSubsytemHelper.java 718 2009-10-16 13:22:41Z kunkel $
  * @lastmodified    $Date: 2009-10-16 15:22:41 +0200 (Fr, 16. Okt 2009) $
  * @modifiedby      $LastChangedBy: kunkel $
  * @version         $Revision: 718 $
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

package de.hd.pvs.piosim.simulator.components.IOSubsystem;

import de.hd.pvs.piosim.simulator.components.ServerCacheLayer.IOJob;
import de.hd.pvs.piosim.simulator.components.ServerCacheLayer.IOOperationData.IOOperationType;
import de.hd.pvs.piosim.simulator.components.ServerCacheLayer.IOOperationData.StreamIOOperation;
import de.hd.pvs.piosim.simulator.output.STraceWriter.TraceType;

public class IOSubsytemHelper {

	public static void traceIOStart(IGIOSubsystem<?> subsystem, IOJob job){
		traceIOStart(subsystem, job, "");
	}

	public static void traceIOStart(IGIOSubsystem<?> subsystem, IOJob job, String postFix){
		subsystem.getSimulator().getTraceWriter().startState(TraceType.IOSERVER, subsystem, job.getOperationType().toString() + postFix);
	}


	public static void traceIOEnd(IGIOSubsystem<?> subsystem, IOJob job, String postFix){
		if(job.getOperationType() == IOOperationType.FLUSH){
			subsystem.getSimulator().getTraceWriter().endState(TraceType.IOSERVER, subsystem, "FLUSH" + postFix, null);
		}else{
			final StreamIOOperation io = (StreamIOOperation) job.getOperationData();
			subsystem.getSimulator().getTraceWriter().endState(TraceType.IOSERVER, subsystem, job.getOperationType().toString() + postFix, new String[]{"size", ""+io.getSize(), "offset", ""+io.getOffset()});
		}
	}

	public static void traceIOEnd(IGIOSubsystem<?> subsystem, IOJob job){
		traceIOEnd(subsystem, job, "");
	}
}
