/** Version Control Information $Id$
 * @lastmodified    $Date$
 * @modifiedby      $LastChangedBy$
 * @version         $Revision$
 */

//	Copyright (C) 2008, 2009 Julian M. Kunkel
//	Copyright (C) 2009 Michael Kuhn
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

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.hd.pvs.piosim.model.components.ServerCacheLayer.ServerCacheLayer;
import de.hd.pvs.piosim.model.inputOutput.FileDescriptor;
import de.hd.pvs.piosim.model.inputOutput.distribution.SimpleStripe;
import de.hd.pvs.piosim.simulator.SimulationResults;
import de.hd.pvs.piosim.simulator.base.ComponentRuntimeInformation;
import de.hd.pvs.piosim.simulator.components.IOSubsystem.GRefinedDiskModel.GRefinedDiskModelInformation;
import de.hd.pvs.piosim.simulator.tests.regression.systemtests.ModelTest;
import de.hd.pvs.piosim.simulator.tests.regression.systemtests.hardwareConfigurations.IOC;
import de.hd.pvs.piosim.simulator.tests.regression.systemtests.hardwareConfigurations.NICC;
import de.hd.pvs.piosim.simulator.tests.regression.systemtests.hardwareConfigurations.NetworkEdgesC;
import de.hd.pvs.piosim.simulator.tests.regression.systemtests.hardwareConfigurations.NetworkNodesC;
import de.hd.pvs.piosim.simulator.tests.regression.systemtests.hardwareConfigurations.NodesC;
import de.hd.pvs.piosim.simulator.tests.regression.systemtests.topologies.ClusterT;
import de.hd.pvs.piosim.simulator.tests.regression.systemtests.topologies.IODisjointConfiguration;
import de.hd.pvs.piosim.simulator.tests.regression.systemtests.topologies.IOServerCreator;
import de.hd.pvs.piosim.simulator.tests.regression.systemtests.topologies.SMTNodeT;

abstract public class IOBenchmark extends ModelTest {

	// number of I/O servers
	protected int serverNum = 10;

	// number of I/O clients
	protected int clientNum = 10;

	// number of outer iterations == repeats of the inner loop
	protected int outerIterations = 1;

	// number of data blocks accessed per inner iteration
	protected int innerNonContigIterations = 100;

	// number of files accessed
	protected int fileNum = 1;

	// block size of the contiguous access
	protected long blockSize = 0;

	// PVFS default
	protected long stripeSize = 100 * KiB;

	abstract public void doWrite(List<FileDescriptor> files) throws Exception;

	abstract public void doRead(List<FileDescriptor> files) throws Exception;

	protected long computeFileSize(){
		return blockSize * innerNonContigIterations * outerIterations * clientNum;
	}

	protected ServerCacheLayer cacheLayer = null;

	protected void initGlobals(){

		mb.getGlobalSettings().setMaxEagerSendSize(100 * KiB);
		mb.getGlobalSettings().setTransferGranularity(100 * KiB);
		mb.getGlobalSettings().setIOGranularity(MiB);
	}

	protected void setup(int nodeCount, int smtPerNode, int serverCount, ServerCacheLayer cacheLayer)
		throws Exception
	{
		final IOServerCreator ios = new IOServerCreator(IOC.PVSServer(), IOC.PVSDisk(), cacheLayer);

		SMTNodeT smtNodeT = new SMTNodeT(smtPerNode,
				NICC.PVSNIC(),
				NodesC.PVSSMPNode(smtPerNode),
				NetworkNodesC.QPI(),
				NetworkEdgesC.QPI()
				);
		ClusterT clientCluster =  new ClusterT(nodeCount,
				NetworkEdgesC.GIGE(),
				NetworkNodesC.GIGSwitch(),
				smtNodeT);


		SMTNodeT serverNodeT = new SMTNodeT(0,
				NICC.PVSNIC(),
				NodesC.PVSSMPNode(smtPerNode),
				NetworkNodesC.QPI(),
				NetworkEdgesC.QPI(), ios
				);
		ClusterT serverCluster =  new ClusterT(serverCount,
				NetworkEdgesC.GIGE(),
				NetworkNodesC.GIGSwitch(),
				serverNodeT) ;


		try{
		super.setup( new IODisjointConfiguration(NetworkEdgesC.TenGIGE(), NetworkNodesC.QPI(), clientCluster, serverCluster ) );
		}catch(IllegalArgumentException e){
			System.err.println("setup error - nodes: " + nodeCount + " smt: " + smtPerNode + " servers: " + serverCount + " " + cacheLayer);
			e.printStackTrace();
		}

		initGlobals();
	}

	protected List<FileDescriptor> prepare(boolean isEmpty) throws Exception {
		List<FileDescriptor> files = new ArrayList<FileDescriptor>();

		assert(blockSize > 0);

		printStack();
		setup(clientNum, 1, serverNum, cacheLayer);

		SimpleStripe dist = new SimpleStripe();
		dist.setChunkSize(stripeSize);

		final long fileSize = computeFileSize();

		for (int i = 0; i < fileNum; i++) {
			files.add(pb.addFileOpen(aB.createFile("testfile" + i, ((isEmpty) ? 0 : fileSize) , dist), world, isEmpty));
		}

		return files;
	}

	protected void closeFiles (List<FileDescriptor> files) throws Exception {
		for (FileDescriptor f : files) {
			pb.addFileClose(f);
		}
	}

	public SimulationResults writeTest() throws Exception {
		List<FileDescriptor> files = prepare(true);
		doWrite(files);
		closeFiles(files);
		return runSimulationAllExpectedToFinish();
	}

	public SimulationResults readTest() throws Exception {
		List<FileDescriptor> files = prepare(false);
		doRead(files);
		closeFiles(files);
		return runSimulationAllExpectedToFinish();
	}

	private void writeTestResults(String type, final FileWriter out, SimulationResults res) throws IOException{
		final long fileSize = computeFileSize();
		final long iosize = (fileNum * fileSize);
		out.write("\n  Config<C,S,Inner,Outer,BS> <" + clientNum + "," + serverNum + "," + innerNonContigIterations + "," + outerIterations + "," + blockSize + ">\n");
		out.write("   " + blockSize + " " +  type + "   " + iosize/1024/1024 + " MiB == " + iosize + " B " + res.getVirtualTime().getDouble() + " s\n");
		out.write("   " + blockSize + " " +  type + "   " + iosize / res.getVirtualTime().getDouble() / 1024 / 1024 + " MiB/s\n");
		// flush to write configuration in case of an error.
		out.flush();

		long accessedAmount = 0;

		for (ComponentRuntimeInformation info : res.getComponentStatistics().values()) {
			if (info.getClass() == GRefinedDiskModelInformation.class) {
				out.write("    " + info + "\n");
				GRefinedDiskModelInformation diskInfo = (GRefinedDiskModelInformation) info;
				accessedAmount += diskInfo.getTotalAmountOfData();
			}
		}

		out.write("   Accessed Data: " + accessedAmount + " isEqual: " + (accessedAmount == iosize) + "\n");

		out.flush();
	}

	private void initTestConfiguration(ServerCacheLayer layer, long blockSize,
			int clientNum, int serverNum, int files, int outerIterations,
			int innerNonContigIterations)
	{
		this.cacheLayer = layer;
		this.blockSize = blockSize;
		this.clientNum = clientNum;
		this.serverNum = serverNum;
		this.fileNum = files;
		this.outerIterations = outerIterations;
		this.innerNonContigIterations = innerNonContigIterations;
	}

	public void runOneTestRead(ServerCacheLayer layer, long blockSize,
			int clientNum, int ServerNum, int files, int outerIterations,
			int innerNonContigIterations, FileWriter out ) throws Exception
	{
		initTestConfiguration(layer, blockSize, clientNum, ServerNum, files, outerIterations, innerNonContigIterations);

		writeTestResults("READ", out, readTest());
	}


	public void runOneTestWrite(ServerCacheLayer layer, long blockSize,
			int clientNum, int ServerNum, int files, int outerIterations,
			int innerNonContigIterations, FileWriter out ) throws Exception
	{

		initTestConfiguration(layer, blockSize, clientNum, ServerNum, files, outerIterations, innerNonContigIterations);

		writeTestResults("WRITE", out, writeTest());
	}

	public void benchmarkServers(String filename, List<ServerCacheLayer> cacheLayers, List<Long> sizes) throws Exception {
		final FileWriter out = new FileWriter(filename);
		for (ServerCacheLayer cacheLayer : cacheLayers) {
			this.cacheLayer = cacheLayer;

			out.write(cacheLayer.getClass().getSimpleName() + "\n");

			System.out.println(cacheLayer.getClass().getSimpleName() + "\n");

			for (long size : sizes) {
				runOneTestRead(cacheLayer, size, clientNum, serverNum, fileNum, outerIterations, innerNonContigIterations, out);
				runOneTestWrite(cacheLayer, size, clientNum, serverNum, fileNum, outerIterations, innerNonContigIterations, out);
			}

		}

		out.close();
	}
}