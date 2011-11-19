package de.hd.pvs.piosim.simulator.tests.regression.systemtests;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.Random;

import org.junit.Test;

import de.hd.pvs.piosim.model.ModelBuilder;
import de.hd.pvs.piosim.model.components.ClientProcess.ClientProcess;
import de.hd.pvs.piosim.model.components.NetworkEdge.NetworkEdge;
import de.hd.pvs.piosim.model.components.NetworkNode.NetworkNode;
import de.hd.pvs.piosim.model.components.ServerCacheLayer.ServerCacheLayer;
import de.hd.pvs.piosim.model.dynamicMapper.CommandType;
import de.hd.pvs.piosim.model.inputOutput.FileDescriptor;
import de.hd.pvs.piosim.model.inputOutput.FileMetadata;
import de.hd.pvs.piosim.model.inputOutput.ListIO;
import de.hd.pvs.piosim.model.inputOutput.distribution.SimpleStripe;
import de.hd.pvs.piosim.model.networkTopology.RoutingAlgorithm.PaketFirstRoute;
import de.hd.pvs.piosim.model.networkTopology.RoutingAlgorithm.PaketRoutingAlgorithm;
import de.hd.pvs.piosim.model.program.Application;
import de.hd.pvs.piosim.model.program.ApplicationBuilder;
import de.hd.pvs.piosim.model.program.ApplicationXMLReader;
import de.hd.pvs.piosim.model.program.ProgramBuilder;
import de.hd.pvs.piosim.model.program.ProgramInMemory;
import de.hd.pvs.piosim.model.program.commands.superclasses.Command;
import de.hd.pvs.piosim.simulator.RunParameters;
import de.hd.pvs.piosim.simulator.tests.regression.systemtests.hardwareConfigurations.IOC;
import de.hd.pvs.piosim.simulator.tests.regression.systemtests.hardwareConfigurations.NICC;
import de.hd.pvs.piosim.simulator.tests.regression.systemtests.hardwareConfigurations.NetworkEdgesC;
import de.hd.pvs.piosim.simulator.tests.regression.systemtests.hardwareConfigurations.NetworkNodesC;
import de.hd.pvs.piosim.simulator.tests.regression.systemtests.hardwareConfigurations.NodesC;
import de.hd.pvs.piosim.simulator.tests.regression.systemtests.topologies.ClusterT;
import de.hd.pvs.piosim.simulator.tests.regression.systemtests.topologies.HardwareConfiguration;
import de.hd.pvs.piosim.simulator.tests.regression.systemtests.topologies.IOServerCreator;
import de.hd.pvs.piosim.simulator.tests.regression.systemtests.topologies.NodeT;
import de.hd.pvs.piosim.simulator.tests.regression.systemtests.topologies.SMTNodeT;
import de.hd.pvs.piosim.simulator.tests.regression.systemtests.topologies.SMTSocketNodeT;

public class ValidationArtur  extends ModelTest {

	String [] configs = new String[]{"1-1","1-2","1-3","1-4","1-5","1-6","1-7","1-8","1-9","1-10","1-11","1-12",
			"2-2","2-3","2-4","2-5","2-7","2-9", "2-11",
			"3-3","3-6","4-4","4-8","5-5","5-10","6-6","6-12","7-7","7-14","8-8","8-16","9-9","9-18","10-10","10-20"};

	int [] sizes = {10240, 1048576, 10485760, 104857600};
	int [] sizes100KiB = {10240, 1048576};


	long procSpeed =  2660l*1000000;
	long randomComputeCyclesMax = procSpeed / 100000; // speed of the proc max 10 ns.
	Random random = new Random(1);

	@Override
	protected void postSetup() {
	}

	protected void setup(int nodeCount, int smtPerNode) throws Exception {
		SMTNodeT smtNodeT = new SMTNodeT(smtPerNode,
				NICC.PVSNIC(),
				NodesC.PVSSMPNode(smtPerNode),
				NetworkNodesC.LocalNodeQPI(),
				NetworkEdgesC.QPI()
				);
		super.setup( new ClusterT(nodeCount,
				NetworkEdgesC.GIGE(),
				NetworkNodesC.GIGSwitch(),
				smtNodeT) );
	}

	protected void setupAnalytical(int nodeCount, int smtPerNode) throws Exception {
		SMTNodeT smtNodeT = new SMTNodeT(smtPerNode,
				NICC.NICAnalytical(),
				NodesC.PVSSMPNode(smtPerNode),
				NetworkNodesC.LocalNodeQPI(),
				NetworkEdgesC.QPI()
				);
		super.setup( new ClusterT(nodeCount,
				NetworkEdgesC.GIGE(),
				NetworkNodesC.GIGSwitch(),
				smtNodeT) );
	}

	protected void setupAnalyticalNonSMP(int nodeCount) throws Exception {
		NodeT smtNodeT = new NodeT(	NICC.NICAnalytical(),
				NodesC.PVSSMPNode(1));
		super.setup( new ClusterT(nodeCount,
				NetworkEdgesC.GIGE(),
				NetworkNodesC.GIGSwitch(),
				smtNodeT) );
	}



	protected void setupSMP(int smtPerSocket) throws Exception {
		setupSMP(smtPerSocket, 1);
	}


	protected void setupSMP(int smtPerSocket, int sockets) throws Exception {
		SMTSocketNodeT smtNodeT = new SMTSocketNodeT(smtPerSocket,
				sockets,
				NICC.PVSNIC(),
				NodesC.PVSSMPNode(smtPerSocket*sockets),
				NetworkNodesC.SocketLocalNode(),
				NetworkEdgesC.SocketLocalEdge(),
				NetworkNodesC.QPI(),
				NetworkEdgesC.QPI()
				);
		super.setup( smtNodeT );
	}

	protected void setupSocketCluster(int nodeCount, int smtPerSocket, int sockets) throws Exception {
		SMTSocketNodeT smtNodeT = new SMTSocketNodeT(smtPerSocket,
				sockets,
				NICC.PVSNIC(),
				NodesC.PVSSMPNode(smtPerSocket*sockets),
				NetworkNodesC.SocketLocalNode(),
				NetworkEdgesC.SocketLocalEdge(),
				NetworkNodesC.QPI(),
				NetworkEdgesC.QPI()
				);
		super.setup( new ClusterT(nodeCount,
				NetworkEdgesC.GIGE(),
				NetworkNodesC.GIGSwitch(),
				smtNodeT) );
	}

	/**
	 * Setup a configuration equal to the WR cluster configuration.
	 * @param processNodes
	 * @param processes
	 * @throws Exception
	 */
	protected void setupWrCluster(int processNodes, int processes, int overlappingServerCount, int additionalServerNodes, ServerCacheLayer cacheLayer, long RAM) throws Exception {
		final int socketCount ;
		final int procsPerSocket ;

		if(true){ // true is always the real setting
			socketCount = 2;
		}else{
			socketCount = 1;
		}

		procsPerSocket = processes / (processNodes * socketCount) + ( processes % (processNodes * socketCount) == 0 ? 0 : 1 );

		final int procsPerNode = procsPerSocket * socketCount;

		SMTSocketNodeT smtNodeT;

		if (true){ // real vs. analytical NIC?
			smtNodeT = new SMTSocketNodeT(procsPerSocket,
				socketCount,
				NICC.PVSNIC(),
				NodesC.PVSSMPNode(procsPerSocket * socketCount, RAM),
				NetworkNodesC.SocketLocalNode(),
				NetworkEdgesC.SocketLocalEdge(),
				NetworkNodesC.QPI(),
				NetworkEdgesC.QPI() );
		}else{
			smtNodeT = new SMTSocketNodeT(procsPerSocket,
					socketCount,
					NICC.NICAnalytical(),
					NodesC.PVSSMPNode(procsPerSocket * socketCount, RAM),
					NetworkNodesC.SocketLocalNode(),
					NetworkEdgesC.SocketLocalEdge(),
					NetworkNodesC.QPI(),
					NetworkEdgesC.QPI() );
		}

		HardwareConfiguration config;

		if(true){ // real cluster value vs. GiGE throughput
			config = new ClusterT(processNodes, NetworkEdgesC.GIGEPVS(),NetworkNodesC.GIGSwitch(), smtNodeT);
		}else{ // faster network
			config = new ClusterT(processNodes, NetworkEdgesC.GIGE(),NetworkNodesC.GIGSwitch(), smtNodeT);
		}

		if(true){ // real vs. latency bound.
		}else{
			smtNodeT = new SMTSocketNodeT(procsPerSocket,
					socketCount,
					NICC.PVSNIC(),
					NodesC.PVSSMPNode(procsPerNode, RAM),
					NetworkNodesC.SocketLocalNode(),
					NetworkEdgesC.SocketLocalNoLatencyEdge(),
					NetworkNodesC.QPI(),
					NetworkEdgesC.QPINoLatency() );
			config = new ClusterT(processNodes, NetworkEdgesC.GIGEPVSNoLatency(),NetworkNodesC.GIGSwitch(), smtNodeT);
		}


		PaketRoutingAlgorithm routingAlgorithm = new PaketFirstRoute();
		mb = new ModelBuilder();
		topology = mb.createTopology("LAN");
		topology.setRoutingAlgorithm(routingAlgorithm);

		// create servers & overhaul existing configuration if necessary
		if(additionalServerNodes > 0 || overlappingServerCount > 0){
			final NetworkEdge nodeEdge = NetworkEdgesC.GIGEPVS();
			final NetworkNode Switch = NetworkNodesC.GIGSwitch();
			mb.addTemplateIf(nodeEdge);
			mb.addTemplateIf(Switch);

			NetworkNode testSW = mb.cloneFromTemplate(Switch);

			testSW.setName("Switch" + testSW.getName());

			mb.addNetworkNode(testSW);

			// the cluster nodes
			ArrayList<NetworkNode> nodes = new ArrayList<NetworkNode>();

			// server configuration
			final IOServerCreator ios = new IOServerCreator(IOC.PVSServer(), IOC.PVSDisk(), cacheLayer);

			// create client only nodes:
			for(int i = 0; i < processNodes - overlappingServerCount ; i++){
				nodes.add(smtNodeT.createModel("" + nodes.size(), mb, topology));
			}


			// overlapping of clients and servers
			SMTSocketNodeT myIOServerGen = new SMTSocketNodeT(procsPerSocket,
					socketCount,
					NICC.PVSNIC(),
					NodesC.PVSSMPNode(procsPerNode, RAM),
					NetworkNodesC.SocketLocalNode(),
					NetworkEdgesC.SocketLocalEdge(),
					NetworkNodesC.QPI(),
					NetworkEdgesC.QPI(), ios );
			if(overlappingServerCount > 0){
				for (int i=0; i < overlappingServerCount; i++){
					nodes.add(myIOServerGen.createModel("" + nodes.size(), mb, topology));
				}
			}

			// servers only
			myIOServerGen = new SMTSocketNodeT(0,
					1,
					NICC.PVSNIC(),
					NodesC.PVSSMPNode(1, RAM),
					NetworkNodesC.SocketLocalNode(),
					NetworkEdgesC.SocketLocalEdge(),
					NetworkNodesC.QPI(),
					NetworkEdgesC.QPI(), ios );
			for (int i=0; i < additionalServerNodes; i++){
				nodes.add(myIOServerGen.createModel("" + nodes.size(), mb, topology));
			}


			for (int i = 0; i < nodes.size(); i++) {
				NetworkNode n = nodes.get(i);

				NetworkEdge edge1 = mb.cloneFromTemplate(nodeEdge);
				NetworkEdge edge2 = mb.cloneFromTemplate(nodeEdge);
				edge1.setName(i + "_TX " + edge1.getName());
				edge2.setName(i + "_RX "+ edge2.getName());
				mb.connect(topology, n, edge1 , testSW);
				mb.connect(topology, testSW, edge2 , n);
			}
		}else{
			config.createModel("", mb, topology);
		}

		parameters.setLoggerDefinitionFile("loggerDefinitionFiles/example");
		parameters.setTraceEnabled(false);
		parameters.setTraceInternals(false);
		parameters.setTraceClientSteps(true);
		parameters.setTraceServers(true);


		aB = new ApplicationBuilder("Validate", "Validation runs", processes, 1);
		app = aB.getApplication();

		// build a dummy app for all nodes
		ApplicationBuilder dummy = new ApplicationBuilder("Test", "Test", procsPerSocket * socketCount * processNodes, 1);
		int cur = 0;
		for(ClientProcess c : mb.getModel().getClientProcesses()){
			c.setRank(cur++);
			c.setApplication("Test");
		}
		mb.setApplication("Test", dummy.getApplication());

		pb = new ProgramBuilder(aB);

		// number => clients
		final ClientProcess [] clients = mb.getModel().getClientProcesses().toArray(new ClientProcess[0]);

		int curNode = 0;
		int curSocket = 0;
		int curProc = 0;

		// placement of the processes
		if(true){ //  => nodes, sockets, PEs

			for(int rank = 0; rank < processes; rank++){

				int physicalCPU = curNode * procsPerNode + curSocket * procsPerSocket + curProc;
				//System.out.println("rank: " + rank + " node: " + curNode + " socket: " + curSocket + " proc: " + curProc + " physicalCPU: " + physicalCPU);

				curNode++;
				if (curNode >= processNodes){
					curNode = 0;
					curSocket++;
					if(curSocket == socketCount){
						curSocket = 0;
						curProc++;
					}
				}

				ClientProcess c = clients[physicalCPU]; // 6 * 2 * nodes
				c.setApplication("Validate");
				c.setRank(rank);
				c.setName("" + rank);
			}
		}else{ // => PEs on one node, one socket, i.e. 2 nodes 5 procs => procs 0,1 and 2,3,4 are together
			int numberOfProcsPerNode = processes / processNodes;
			int restProcs = processes % processNodes;
			int rank = 0;
			for(int node = 0; node < processNodes; node++){
				for(int pCPU = 0; pCPU < numberOfProcsPerNode ; pCPU++){
					ClientProcess c = clients[procsPerNode * node + pCPU]; // 6 * 2 * nodes
					c.setApplication("Validate");
					c.setRank(rank);
					c.setName("" + rank);

					rank++;
				}
			}
			// last node gets the remainder
			for(int rest = 0; rest < restProcs; rest++){
				ClientProcess c = clients[procsPerNode * (processNodes-1) + numberOfProcsPerNode + rest];
				c.setApplication("Validate");
				c.setRank(rank);
				c.setName("" + rank);

				rank++;
			}
		}

		mb.setApplication("Validate", app);


		world = aB.getWorldCommunicator();
		model = mb.getModel();

		// set useful defaults:

		mb.getGlobalSettings().setClientFunctionImplementation(	new CommandType("Allreduce"), "de.hd.pvs.piosim.simulator.program.Allreduce.BinaryTree");
		mb.getGlobalSettings().setClientFunctionImplementation(	new CommandType("Allgather"), "de.hd.pvs.piosim.simulator.program.Allgather.AllgatherMPICH2");
		mb.getGlobalSettings().setClientFunctionImplementation(	new CommandType("Barrier"), "de.hd.pvs.piosim.simulator.program.Barrier.BarrierMPICH2");
		mb.getGlobalSettings().setClientFunctionImplementation(	new CommandType("Reduce"), "de.hd.pvs.piosim.simulator.program.Reduce.ReduceScatterGatherMPICH2");

		mb.getGlobalSettings().setClientFunctionImplementation(	new CommandType("Gather"), "de.hd.pvs.piosim.simulator.program.Gather.GatherBinaryTreeMPICH2");
		mb.getGlobalSettings().setClientFunctionImplementation(	new CommandType("Scatter"), "de.hd.pvs.piosim.simulator.program.Scatter.ScatterMPICH2");
		mb.getGlobalSettings().setClientFunctionImplementation(	new CommandType("ReduceScatter"), "de.hd.pvs.piosim.simulator.program.ReduceScatter.ReduceScatterPowerOfTwo");
		mb.getGlobalSettings().setClientFunctionImplementation(	new CommandType("Bcast"), "de.hd.pvs.piosim.simulator.program.Bcast.BroadcastScatterGatherall");


		mb.getGlobalSettings().setMaxEagerSendSize(100 * KiB);
		model.getGlobalSettings().setTransferGranularity(100 * KiB);
	}


	/**
	 * Setup a configuration equal to the WR cluster configuration.
	 * @param nodeCount
	 * @param processes
	 * @throws Exception
	 */
	protected void setupWrCluster(int nodeCount, int processes) throws Exception {
		setupWrCluster(nodeCount, processes, 0, 0, null, 12000);
	}


	private void addComputeOp(int rank){

		if (randomComputeCyclesMax > 0){
			long cycles = random.nextLong() % randomComputeCyclesMax ;
			if(cycles < 0 ) cycles = - cycles;

			cycles += (long) ((double) procSpeed * 0.000150); // time between two subsequent calls in the parabench trace files.

			if(cycles > 0){
				pb.addCompute(rank,  cycles );
			}
		}
	}

	private void levelXOperation(boolean isWrite, int level, FileDescriptor fd, int clientProcesses, int repeats, long size){

		switch(level){
		case 0: {
			int pos = 0;

			// level 0
			for(int c=0; c < repeats; c++){

				for(int i=0 ; i < clientProcesses ; i++){
					if (isWrite){
						pb.addWriteSequential(i, fd, size * pos, size);
					}else{
						pb.addReadSequential(i, fd,  size * pos, size);
					}
					addComputeOp(i);
					pos++;
				}
			}
			break;
		}
		case 1: {
			int pos = 0;
			for(int c=0; c < repeats; c++){

				LinkedList<ListIO> ios = new LinkedList<ListIO>();

				for(int i=0 ; i < clientProcesses ; i++){
					ListIO listio = new ListIO();
					listio.addIOOperation(size * pos, size);

					ios.add(listio);
					pos++;
				}
				if (isWrite){
					pb.addWriteCollective(fd, ios);
				}else{
					pb.addReadCollective(fd, ios);
				}

				for(int i=0 ; i < clientProcesses ; i++){
					addComputeOp(i);
				}
			}

			break;
		}

		case 2: {
			//level2:
			for(int i=0 ; i < clientProcesses ; i++){
				ListIO listio = new ListIO();
				for(int c=0; c < repeats; c++){
					listio.addIOOperation(size * (c*clientProcesses + i), size);
				}
				if (isWrite){
					pb.addWriteIndependentNoncontiguous(i, fd, listio);
				}else{
					pb.addReadIndependentNoncontiguous(i, fd, listio);
				}
			}
			break;
		}
		case 3:{
			// level3:
			LinkedList<ListIO> ios = new LinkedList<ListIO>();
			for(int i=0 ; i < clientProcesses ; i++){
				ListIO listio = new ListIO();
				for(int c=0; c < repeats; c++){
					listio.addIOOperation(size * (c*clientProcesses + i), size);
				}
				ios.add(listio);
			}
			if (isWrite){
				pb.addWriteCollective(fd, ios);
			}else{
				pb.addReadCollective(fd, ios);
			}

			break;
		}
		}
	}

	void runMPIIOLevelValidationSingle(int level, boolean write, String prefix, int clients, int servers, ServerCacheLayer cacheLayer, int processes, int overlapping, int repeats, long size, long ramSize, boolean tracing, BufferedWriter modelTime) throws Exception{
		if (modelTime == null)
			modelTime = new BufferedWriter(new FileWriter("/tmp/mpi-iolevelUnnamed-modelTime.txt"));

		String configStr = prefix + " N" + (clients + servers - overlapping) + "-P1-C" + clients + "-P" + processes + "-S" + servers + "-RAM" + ramSize + "-Size" + size + "-rep" + repeats + " " + (write ? "WRITE" : "READ") + "-lvl" + level;

		setupWrCluster(clients, processes, overlapping, servers, cacheLayer, ramSize);
		parameters.setTraceFile("/tmp/io-level" + prefix + level + (write ? "WRITE" : "READ"));
		parameters.setTraceEnabled(tracing);

		SimpleStripe dist = new SimpleStripe();
		dist.setChunkSize(64 * KiB);
		FileMetadata file =  aB.createFile("test", 100 * GiB, dist );

		FileDescriptor fd = pb.addFileOpen(file, world, false);

		if( write ){
			levelXOperation(true, level, fd, processes, repeats, size);
		}else{
			levelXOperation(false, level, fd, processes, repeats, size);
		}
		pb.addFileClose(fd);

		runSimulationAllExpectedToFinish();
		modelTime.write(configStr + " " + simRes.getVirtualTime().getDouble() + "\n");
		modelTime.flush();
	}

	void runMPIIOLevelValidation(String prefix, int clients, int servers, ServerCacheLayer cacheLayer, int processes, int overlapping, int repeats, long size, long ramSize, boolean tracing, BufferedWriter modelTime) throws Exception{

		// iterate through read and write
		for(int level = 0; level < 4 ; level ++){
			for(int i = 0 ; i < 2; i++){
				boolean write = i == 0 ? true : false;
				runMPIIOLevelValidationSingle(level, write, prefix, clients, servers, cacheLayer, processes, overlapping, repeats, size, ramSize, tracing, modelTime);
			}
		}
	}

	public void MPIIOLevelValidationBlocks(long size, int repeats, ServerCacheLayer cacheLayer, BufferedWriter modelTime) throws Exception{
			// test with 10000 MiB main memory
			for(int i=1; i <= 5 ; i++){
				runMPIIOLevelValidation("10000MB ", i,i,cacheLayer,i,0,repeats, size, 10000,false, modelTime);
			}
			runMPIIOLevelValidation("10000MB ",3 , 2,cacheLayer,3, 0,repeats, size, 10000,false, modelTime);

			// test with 1000 MiB main memory
			for(int i=1; i <= 5 ; i++){
				runMPIIOLevelValidation("1GiG ", i,i,cacheLayer,i,0,repeats, size, 1000, false, modelTime);
			}
			runMPIIOLevelValidation("1GiG ",3 , 2,cacheLayer,3, 0,repeats, size, 1000, false, modelTime);

			// test to run multiple processes on the client nodes
			for(int i=2; i <= 6 ; i++){
				runMPIIOLevelValidation("multiple ", 5, 5, cacheLayer,i*5,0,repeats, size, 1000, false, modelTime);
			}

			// overlapping test
			runMPIIOLevelValidation("overlapping ", 8,8,cacheLayer, 8, 8, repeats, size, 2000, false, modelTime);

			// 100 MiB main memory
			for(int i=1; i <= 5 ; i++){
				runMPIIOLevelValidation("100M ", i,i,cacheLayer,i,0,repeats, size, 100, false, modelTime);
			}
			runMPIIOLevelValidation("100M ",3 , 2,cacheLayer,3, 0,repeats, size, 100, false, modelTime);
		System.out.println("Completed");
	}

	FilenameFilter projFilter = new FilenameFilter() {
        public boolean accept(File dir, String name) {
             return name.endsWith(".proj");
         }
	};

	String getProjectFile(String folder){
		File f = new File(folder);
		String files[] = f.list(projFilter);
		if (files == null || files.length != 1){
			System.err.println("Invalid configuration: " + folder);
			return "";
		}
		return folder + "/" + files[0];
	}

	public void runCollectiveTest(int nodes, int processes, String experiment, String strSize, BufferedWriter outputFile, BufferedWriter modelTime, boolean doCompute, boolean addBarrier, RunParameters parameters, int repeatReading) throws Exception{

		if(outputFile == null){
			outputFile = new BufferedWriter(new FileWriter("/tmp/collectives-runTime.txt"));
			outputFile.write("#Proc\tEvents\tRuntime\tSysModelT\tProgramMT\n");
		}
		if(modelTime == null){
			modelTime = new BufferedWriter(new FileWriter("/tmp/collectives-modelTime.txt"));
		}

		if(addBarrier){
			System.out.println("Added barrier");
		}

		final ApplicationXMLReader axml = new ApplicationXMLReader();
		final String prefix = "/home/kunkel/Dokumente/Dissertation/Trace/results-git/compute-only/extracted-communication-patterns/";


		long sTime, setupSystemTime, setupProgramTime;
		// dual socket configuration.
		sTime = new Date().getTime();

		final String config = nodes + "-" + processes;

		if(parameters != null)
			this.parameters = parameters;

		setupSystemTime = (new Date().getTime() - sTime);

		sTime = new Date().getTime();

		// load traces
		final String folder = prefix + config + "/" + experiment + "/" + strSize;
		String proj= getProjectFile(folder);
		if(proj == ""){
			outputFile.write("Invalid configuration: " + folder);
			// we know the time is 0.0 for the configuration 1-1
			modelTime.write("0.0 ");
			return;
		}

		System.out.println(proj);
		outputFile.write(proj);
		outputFile.flush();

		// don't do any computation ...
		// TODO run both tests at the same time
		if (! doCompute){
			mb.getGlobalSettings().setClientFunctionImplementation(	new CommandType("Compute"), "de.hd.pvs.piosim.simulator.program.Global.NoOperation");
		}


		// load program:
		final Application app = axml.parseApplication(proj, true);
		mb.setApplication("Validate", app);

		if(repeatReading > 0){
			for(int r = 0 ; r < app.getProcessCount(); r++){
				ProgramInMemory p = (ProgramInMemory) app.getClientProgram(r, 0);
				ArrayList<Command> prevCommands = p.getCommands();
				int commandCount = prevCommands.size();

				for(int c=0; c < repeatReading; c++){
					for(int i=0; i < commandCount; i++){
						p.addCommand(prevCommands.get(i));
					}
				}
			}
		}

		if(addBarrier){
			String barrierProj= getProjectFile(prefix + config + "/Barrier");
			final Application barrierApp;
			barrierApp = axml.parseApplication(barrierProj, true);

			for(int r = 0 ; r < barrierApp.getProcessCount(); r++){
				ProgramInMemory bp = (ProgramInMemory) barrierApp.getClientProgram(r, 0);
				ProgramInMemory p = (ProgramInMemory) app.getClientProgram(r, 0);

				ArrayList<Command> prevCommands = bp.getCommands();
				int commandCount = prevCommands.size();

				for(int i=0; i < commandCount; i++){
					p.addCommand(prevCommands.get(i));
				}
			}

		}


		setupProgramTime = (new Date().getTime() - sTime);
		runSimulationWithoutOutput();

		outputFile.write("\t" + config + "\t" + simRes.getEventCount() + "\t" + simRes.getWallClockTime() + "\t" + setupSystemTime  / 1000.0 + "\t" + setupProgramTime  / 1000.0 + "\n");
		outputFile.flush();

		modelTime.write(simRes.getVirtualTime().getDouble() + " ");
		System.out.println("Modeltime: " + simRes.getVirtualTime().getDouble());
		modelTime.flush();
	}


	public void sendRecvPaired(BufferedWriter modelTime ) throws Exception{
		// test cases run on the WR cluster
		for(int size: sizes){
			modelTime.write("SendRecvPaired" + size + " ");
			for(String config : configs){
				final int nodes = Integer.parseInt(config.split("-")[0]);
				final int processes = Integer.parseInt(config.split("-")[1]);


				if (processes % 2 != 0){ // invalid test
					modelTime.write("0.0 ");
					continue;
				}

				setupWrCluster(nodes, processes);
				int repeats = 1;

				if(nodes == 1 && size <= 100*1024){
					repeats = 100;
				}

				for(int i=0; i < repeats; i++){
					for(int rank=0; rank < processes ; rank++){
					    int dest = rank % 2 == 0 ? rank + 1 : rank -1;

						pb.addSendRecv(world, rank, dest, dest, size, 4711, 4711);
					}
				}
				pb.addBarrier(world);


				runSimulationWithoutOutput();

				modelTime.write(simRes.getVirtualTime().getDouble() + " ");
				modelTime.flush();
			}
			modelTime.write("\n");
		}
		System.out.println("Completed!");
	}



	public void sendRootWhichReceives(BufferedWriter modelTime) throws Exception{
		// test cases run on the WR cluster

		for(int size: sizes100KiB){
			modelTime.write("SendRoot" + size + " ");
			for(String config : configs){
				final int nodes = Integer.parseInt(config.split("-")[0]);
				final int processes = Integer.parseInt(config.split("-")[1]);

				setupWrCluster(nodes, processes);

				if(processes == 1){
					modelTime.write("0.0 ");
					continue;
				}

				int repeats = 1;

				if(nodes == 1 && size <= 100*1024){
					repeats = 100;
				}

				for(int i=0; i < repeats; i++){
					for(int rank=1; rank < processes ; rank++){
						pb.addSendAndRecv(world, rank, 0, size, 4711);
					}
				}
				pb.addBarrier(world);



				runSimulationWithoutOutput();

				modelTime.write(simRes.getVirtualTime().getDouble() + " ");
				modelTime.flush();
			}
			modelTime.write("\n");
		}
		System.out.println("Completed!");
	}


	public void sendRecvRoot(BufferedWriter modelTime) throws Exception{
		// test cases run on the WR cluster

		for(int size: sizes100KiB){
			modelTime.write("SendRecvRoot" + size + " ");
			for(String config : configs){
				final int nodes = Integer.parseInt(config.split("-")[0]);
				final int processes = Integer.parseInt(config.split("-")[1]);

				setupWrCluster(nodes, processes);

				if(processes == 1){
					modelTime.write("0.0 ");
					continue;
				}

				int repeats = 1;

				if(nodes == 1 && size <= 100*1024){
					repeats = 100;
				}

				for(int i=0; i < repeats; i++){
					for(int rank=1; rank < processes ; rank++){
						pb.addSendRecv(world, rank, 0, 0, size, 4711, 4711);
						pb.addSendRecv(world, 0, rank, rank, size, 4711, 4711);
					}

				}
				pb.addBarrier(world);

				runSimulationWithoutOutput();

				modelTime.write(simRes.getVirtualTime().getDouble() + " ");
				modelTime.flush();
			}
			modelTime.write("\n");
		}
		System.out.println("Completed!");
	}

	public void sendRecvRingRightLeftNeighbor(BufferedWriter modelTime) throws Exception{
		// test cases run on the WR cluster

		for(int size: sizes){
			modelTime.write("SendRecvRightNeighbor" + size + " ");
			for(String config : configs){
				final int nodes = Integer.parseInt(config.split("-")[0]);
				final int processes = Integer.parseInt(config.split("-")[1]);

				setupWrCluster(nodes, processes);

				if(processes == 1){
					modelTime.write("0.0 ");
					continue;
				}

				int repeats = 1;

				if(nodes == 1 && size <= 100*1024){
					repeats = 100;
				}

				for(int i=0; i < repeats; i++){
					for(int rank=0; rank < processes ; rank++){
						int dest = (rank == processes - 1) ? 0 : rank + 1;
						int src = (rank == 0) ? processes - 1 : rank - 1;

						pb.addSendRecv(world, rank, src, dest, size, 4711, 4711);
					}
				}
				pb.addBarrier(world);


				runSimulationWithoutOutput();

				modelTime.write(simRes.getVirtualTime().getDouble() + " ");
				modelTime.flush();
			}
			modelTime.write("\n");
		}
		System.out.println("Completed!");
	}


	 public void validationRunCollectiveWithSendRecv(BufferedWriter modelTime) throws Exception{
		BufferedWriter outputFile = new BufferedWriter(new FileWriter("/tmp/collectives-runTime.txt"));
		outputFile.write("#Proc\tEvents\tRuntime\tSysModelT\tProgramMT\n");

		final String prefix = "/home/kunkel/Dokumente/Dissertation/Latex/results/mpi-bench-current/extracted-communication-patterns/";


		// test cases run on the WR cluster


		String [] experiments = new String[]{"Reduce", "Allreduce", "Bcast", "Barrier",  "Allgather", "Gather", "Scatter"};


		for(String experiment: experiments){
			for(int size: sizes){
				String strSize = "" + size;

				if(experiment.equals("Barrier")){
					if(size != sizes[0]){
						continue;
					}
					strSize = "";
				}

				modelTime.write(experiment + strSize + " ");

				for(String config: configs){

					final int nodes = Integer.parseInt(config.split("-")[0]);
					final int processes = Integer.parseInt(config.split("-")[1]);

					// those values are set by the real-world test...
					int repeats = 0;

					if(! experiment.equals("Barrier")){
						if(nodes == 1 && size <= 100*1024){
							repeats = 99;
						}
					}else if (experiment.equals("Barrier") && nodes == 1){
						repeats = 99;
					}

					setupWrCluster(nodes, processes);
					if(size <= 1*MiB){
						model.getGlobalSettings().setTransferGranularity(512);
					}else{
						model.getGlobalSettings().setTransferGranularity(100 * KiB);
					}
					runCollectiveTest(nodes, processes, experiment, strSize, outputFile, modelTime,  true,  ! experiment.equals("Barrier"), null, repeats);
				}
				modelTime.write("\n");

				if(experiments.equals("Reduce") || experiments.equals("Allreduce") || experiments.equals("Broadcast")){
					// add a dummy
					modelTime.write(experiment + "1000M\n");
				}
			}
		}
		outputFile.close();
	}

	private void printTiming(String header, double[] times) throws IOException{
		final FileWriter fo = new FileWriter("/tmp/timing-" + this.getClass().getSimpleName() + ".txt", true);

		fo.write(header + " timing:\n");
		System.out.println(header + " timing");

		for(int i=1; i < times.length; i++){
			if(times[i] != 0){
				fo.write(i + " " + times[i] + "\n");
				System.out.println(i + " " + times[i]);
			}
		}
		fo.write("\n");
		fo.close();
	}

	@Test public void Reduce8() throws Exception{
		setupWrCluster(8, 8);

		mb.getGlobalSettings().setMaxEagerSendSize(100 * KiB);
		mb.getGlobalSettings().setClientFunctionImplementation(
				new CommandType("Reduce"), "de.hd.pvs.piosim.simulator.program.Reduce.ReduceScatterGatherMPICH2");  //andere Implementation
		//CommandToSimulationMapper Eintrag, Standard letzter

		parameters.setTraceFile("/tmp/reduce8");

		parameters.setTraceEnabled(true);

		pb.addReduce(world, 0, 10 * MiB);

		runSimulationAllExpectedToFinish();
	}

	@Test public void Reduce10() throws Exception{
		setupWrCluster(10, 10);

		mb.getGlobalSettings().setMaxEagerSendSize(100 * KiB);
		mb.getGlobalSettings().setClientFunctionImplementation(
				new CommandType("Reduce"), "de.hd.pvs.piosim.simulator.program.Reduce.ReduceScatterGatherMPICH2");  //andere Implementation
		//CommandToSimulationMapper Eintrag, Standard letzter

		parameters.setTraceFile("/tmp/reduce10");

		parameters.setTraceEnabled(true);

		pb.addReduce(world, 0, 10 * MiB);

		runSimulationAllExpectedToFinish();
	}

	@Test public void AllGather8() throws Exception{
		setupWrCluster(8, 8);

		mb.getGlobalSettings().setMaxEagerSendSize(100 * KiB);
		mb.getGlobalSettings().setClientFunctionImplementation(
				new CommandType("Allgather"), "de.hd.pvs.piosim.simulator.program.Allgather.AllgatherMPICH2");  //andere Implementation
		//CommandToSimulationMapper Eintrag, Standard letzter

		parameters.setTraceFile("/tmp/allgather8");

		parameters.setTraceEnabled(true);

		pb.addAllgather(world, 10 * MiB);

		runSimulationAllExpectedToFinish();
	}

	@Test public void AllGather10() throws Exception{
		setupWrCluster(10, 10);

		mb.getGlobalSettings().setMaxEagerSendSize(100 * KiB);
		mb.getGlobalSettings().setClientFunctionImplementation(
				new CommandType("Allgather"), "de.hd.pvs.piosim.simulator.program.Allgather.AllgatherMPICH2");  //andere Implementation
		//CommandToSimulationMapper Eintrag, Standard letzter

		parameters.setTraceFile("/tmp/allgather10");

		parameters.setTraceEnabled(true);

		pb.addAllgather(world, 10 * MiB);

		runSimulationAllExpectedToFinish();
	}

	@Test public void AllReduce8() throws Exception{
		setupWrCluster(8, 8);

		mb.getGlobalSettings().setMaxEagerSendSize(100 * KiB);
		mb.getGlobalSettings().setClientFunctionImplementation(
				new CommandType("Allreduce"), "de.hd.pvs.piosim.simulator.program.Allreduce.ReduceBroadcast");  //andere Implementation
		//CommandToSimulationMapper Eintrag, Standard letzter

		parameters.setTraceFile("/tmp/allreduce8");

		parameters.setTraceEnabled(true);

		pb.addAllreduce(world, 10 * MiB);

		runSimulationAllExpectedToFinish();
	}

	@Test public void AllReduce10() throws Exception{
		setupWrCluster(10, 10);

		mb.getGlobalSettings().setMaxEagerSendSize(100 * KiB);
		mb.getGlobalSettings().setClientFunctionImplementation(
				new CommandType("Allreduce"), "de.hd.pvs.piosim.simulator.program.Allreduce.ReduceBroadcast");  //andere Implementation
		//CommandToSimulationMapper Eintrag, Standard letzter

		parameters.setTraceFile("/tmp/allreduce10");

		parameters.setTraceEnabled(true);

		pb.addAllreduce(world, 10 * MiB);

		runSimulationAllExpectedToFinish();
	}

	@Test public void Barrier8() throws Exception{
		setupWrCluster(8, 8);

		mb.getGlobalSettings().setMaxEagerSendSize(100 * KiB);
		mb.getGlobalSettings().setClientFunctionImplementation(
				new CommandType("Barrier"), "de.hd.pvs.piosim.simulator.program.Barrier.BarrierMPICH2");  //andere Implementation
		//CommandToSimulationMapper Eintrag, Standard letzter

		parameters.setTraceFile("/tmp/barrier8");

		parameters.setTraceEnabled(true);

		pb.addBarrier(world);

		runSimulationAllExpectedToFinish();
	}

	@Test public void Barrier10() throws Exception{
		setupWrCluster(10, 10);

		mb.getGlobalSettings().setMaxEagerSendSize(100 * KiB);
		mb.getGlobalSettings().setClientFunctionImplementation(
				new CommandType("Barrier"), "de.hd.pvs.piosim.simulator.program.Barrier.BarrierMPICH2");  //andere Implementation
		//CommandToSimulationMapper Eintrag, Standard letzter

		parameters.setTraceFile("/tmp/barrier10");

		parameters.setTraceEnabled(true);

		pb.addBarrier(world);

		runSimulationAllExpectedToFinish();
	}

	@Test public void Gather8() throws Exception{
		setupWrCluster(8, 8);

		mb.getGlobalSettings().setMaxEagerSendSize(100 * KiB);
		mb.getGlobalSettings().setClientFunctionImplementation(
				new CommandType("Gather"), "de.hd.pvs.piosim.simulator.program.Gather.GatherMPICH2");  //andere Implementation
		//CommandToSimulationMapper Eintrag, Standard letzter

		parameters.setTraceFile("/tmp/gather8");

		parameters.setTraceEnabled(true);

		pb.addGather(world, 0, 10 * MiB);

		runSimulationAllExpectedToFinish();
	}

	@Test public void Gather10() throws Exception{
		setupWrCluster(10, 10);

		mb.getGlobalSettings().setMaxEagerSendSize(100 * KiB);
		mb.getGlobalSettings().setClientFunctionImplementation(
				new CommandType("Gather"), "de.hd.pvs.piosim.simulator.program.Gather.GatherMPICH2");  //andere Implementation
		//CommandToSimulationMapper Eintrag, Standard letzter

		parameters.setTraceFile("/tmp/gather10");

		parameters.setTraceEnabled(true);

		pb.addGather(world, 0, 10 * MiB);

		runSimulationAllExpectedToFinish();
	}

	@Test public void Scatter8() throws Exception{
		setupWrCluster(8, 8);

		mb.getGlobalSettings().setMaxEagerSendSize(100 * KiB);
		mb.getGlobalSettings().setClientFunctionImplementation(
				new CommandType("Scatter"), "de.hd.pvs.piosim.simulator.program.Scatter.ScatterMPICH2");  //andere Implementation
		//CommandToSimulationMapper Eintrag, Standard letzter

		parameters.setTraceFile("/tmp/scatter8");

		parameters.setTraceEnabled(true);

		pb.addScatter(world, 0, 10 * MiB);

		runSimulationAllExpectedToFinish();
	}

	@Test public void Scatter10() throws Exception{
		setupWrCluster(10, 10);

		mb.getGlobalSettings().setMaxEagerSendSize(100 * KiB);
		mb.getGlobalSettings().setClientFunctionImplementation(
				new CommandType("Scatter"), "de.hd.pvs.piosim.simulator.program.Scatter.ScatterMPICH2");  //andere Implementation
		//CommandToSimulationMapper Eintrag, Standard letzter

		parameters.setTraceFile("/tmp/scatter10");

		parameters.setTraceEnabled(true);

		pb.addScatter(world, 0, 10 * MiB);

		runSimulationAllExpectedToFinish();
	}
}
