
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

import de.hd.pvs.piosim.simulator.event.IOJob;
import de.hd.pvs.piosim.simulator.output.STraceWriter.TraceType;

public class IOSubsytemHelper {
	
	public static void traceIOStart(IGIOSubsystem<?> subsystem, IOJob job){
		traceIOStart(subsystem, job, "");	
	}
	
	public static void traceIOStart(IGIOSubsystem<?> subsystem, IOJob job, String postFix){
		subsystem.getSimulator().getTraceWriter().start(TraceType.IOSERVER, subsystem.getSimulatorObject(), job.getType().toString() + postFix);
	}
	
	
	public static void traceIOEnd(IGIOSubsystem<?> subsystem, IOJob job, String postFix){		
		subsystem.getSimulator().getTraceWriter().end(TraceType.IOSERVER, subsystem.getSimulatorObject(), job.getType().toString() + postFix);		
	}
	
	public static void traceIOEnd(IGIOSubsystem<?> subsystem, IOJob job){
		traceIOEnd(subsystem, job, "");
	}
}
