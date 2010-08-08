//	Copyright (C) 2009 Michael Kuhn, 2010 Julian Kunkel
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
package de.hd.pvs.piosim.simulator.tests.regression.systemtests.IOBenchmark;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import de.hd.pvs.piosim.model.components.ServerCacheLayer.ServerCacheLayer;
import de.hd.pvs.piosim.model.dynamicMapper.CommandType;
import de.hd.pvs.piosim.model.inputOutput.FileDescriptor;
import de.hd.pvs.piosim.model.program.commands.Filereadall;
import de.hd.pvs.piosim.model.program.commands.Filewriteall;
import de.hd.pvs.piosim.simulator.tests.regression.systemtests.hardwareConfigurations.IOC;


public class Collective extends Individual {

	/**
	 * The current client implementation for read/writeall.
	 */
	String currentImpl;

	@Test
	public void benchmarkServers() throws Exception{
		final List<ServerCacheLayer> cacheLayers = new ArrayList<ServerCacheLayer>();
		final List<Long> sizes = new ArrayList<Long>();

		cacheLayers.add(IOC.SimpleNoCache());
		cacheLayers.add(IOC.SimpleWriteBehindCache());
		cacheLayers.add(IOC.AggregationCache());
		cacheLayers.add(IOC.AggregationReorderCache());
		//cacheLayers.add(new ServerDirectedIO());

		//		sizes.add((long)512);
		sizes.add((long)5 * KBYTE);
		sizes.add((long)50 * KBYTE);
		sizes.add((long)500 * KBYTE);
		sizes.add((long)5000 * KBYTE);

		for (String impl: new String []{"Direct", "TwoPhase", "ContiguousTwoPhase"} ){
			this.currentImpl = impl;
			super.benchmarkServers("/tmp/benchmark-collective-" + impl + ".txt", cacheLayers, sizes);
		}
	}

	@Override
	public void doWrite(List<FileDescriptor> files) throws Exception {
		getGlobalSettings().setClientFunctionImplementation(new CommandType("Filewriteall"), "de.hd.pvs.piosim.simulator.program.Filewriteall." + currentImpl);

		createIOOps(files, Filewriteall.class);
	}

	@Override
	public void doRead(List<FileDescriptor> files) throws Exception {
		getGlobalSettings().setClientFunctionImplementation(new CommandType("Filereadall"), "de.hd.pvs.piosim.simulator.program.Filereadall." + currentImpl);
		createIOOps(files, Filereadall.class);
	}


}