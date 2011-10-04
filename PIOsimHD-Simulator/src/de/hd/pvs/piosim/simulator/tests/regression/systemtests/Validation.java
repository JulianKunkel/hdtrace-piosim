package de.hd.pvs.piosim.simulator.tests.regression.systemtests;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
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
import de.hd.pvs.piosim.model.components.ServerCacheLayer.AggregationCache;
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
import de.hd.pvs.piosim.model.program.Communicator;
import de.hd.pvs.piosim.model.program.ProgramBuilder;
import de.hd.pvs.piosim.simulator.SimulationResultSerializer;
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

public class Validation  extends ModelTest {

	String [] configs = new String[]{"1-1","1-2","1-3","1-4","1-5","1-6","1-7","1-8","1-9","1-10","1-11","1-12",
			"2-2","2-3","2-4","2-5","2-7","2-9", "2-11",
			"3-3","3-6","4-4","4-8","5-5","5-10","6-6","6-12","7-7","7-14","8-8","8-16","9-9","9-18","10-10","10-20"};

	int [] sizes = {10240, 1048576, 10485760, 104857600};
	int [] sizes100KiB = {10240, 102400, 1048576, 10485760, 104857600};


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
			procsPerSocket = 6;
		}else{
			socketCount = 1;
			procsPerSocket = 12;
		}

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
		if(additionalServerNodes > 0){
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
		mb.getGlobalSettings().setMaxEagerSendSize(100 * KiB);
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



	@Test public void sendRecvData() throws Exception{
		final int pairs = 2;
		setupSMP(pairs * 2, 1);
		mb.getGlobalSettings().setMaxEagerSendSize(100 * KiB);
		for(int i=0; i < pairs ; i++){
			pb.addSendAndRecv(world, i, i+pairs, 1000* MiB, 4711);
		}

		runSimulationAllExpectedToFinish();
	}

	@Test public void sendRecvIntersocketValidate() throws Exception{
		System.out.println("Single socket");

		setupSMP(2, 1);
		mb.getGlobalSettings().setMaxEagerSendSize(100 * KiB);
		pb.addSendRecv(world, 0, 1, 1, 0, 100, 101);
		pb.addSendRecv(world, 1, 0, 0, 0, 101, 100);
		runSimulationAllExpectedToFinish();


		System.out.println("Across two sockets");
		setupSMP(1, 2);
		mb.getGlobalSettings().setMaxEagerSendSize(100 * KiB);
		pb.addSendRecv(world, 0, 1, 1, 0, 100, 101);
		pb.addSendRecv(world, 1, 0, 0, 0, 101, 100);
		runSimulationAllExpectedToFinish();


		System.out.println("Inter-node");
		setupSocketCluster(2, 1, 1);
		pb.addSendRecv(world, 0, 1, 1, 0, 100, 101);
		pb.addSendRecv(world, 1, 0, 0, 0, 101, 100);
		runSimulationAllExpectedToFinish();
	}



	@Test public void recv1MB() throws Exception{
		setupSMP(2); 		//2 = Anzahl Prozessoren
		mb.getGlobalSettings().setMaxEagerSendSize(100 * KiB);

		parameters.setTraceFile("/tmp/recv1MB");   //Ausgabedatei

		parameters.setTraceEnabled(true); 	//Trace An

		pb.addReduce(world, 0, 1* MiB);  	// (-,-,Größe)

		runSimulationAllExpectedToFinish();
	}


	@Test public void sendRecvPaired() throws Exception{

		setupWrCluster(1, 2);

		parameters.setTraceFile("/tmp/sendRecv");
		parameters.setTraceEnabled(true);

		pb.addSendRecv(world, 0, 1, 1, 100*MiB, 2020, 2020);
		pb.addSendRecv(world, 1, 0, 0, 100*MiB, 2020, 2020);

		runSimulationAllExpectedToFinish();
	}





	@Test public void allreduceRootComputes() throws Exception{
		setup(3, 1);
		mb.getGlobalSettings().setMaxEagerSendSize(100 * KiB);
		mb.getGlobalSettings().setClientFunctionImplementation(	new CommandType("Allreduce"), "de.hd.pvs.piosim.simulator.program.Allreduce.RootComputes");
		parameters.setTraceFile("/tmp/allreduce");

		parameters.setTraceEnabled(true);

		pb.addAllreduce(world, 10 * MiB);

		runSimulationAllExpectedToFinish();
	}


	@Test public void barrierTree() throws Exception{
		setup(8, 1);
		mb.getGlobalSettings().setMaxEagerSendSize(100 * KiB);
		mb.getGlobalSettings().setClientFunctionImplementation(	new CommandType("Barrier"), "de.hd.pvs.piosim.simulator.program.Barrier.BinaryTree");
		parameters.setTraceFile("/tmp/barrier");

		parameters.setTraceEnabled(true);

		pb.addBarrier(world);

		runSimulationAllExpectedToFinish();
	}


	@Test public void broadcastMultiplex() throws Exception{
		setup(5, 1);
		mb.getGlobalSettings().setMaxEagerSendSize(100 * KiB);
		mb.getGlobalSettings().setClientFunctionImplementation(	new CommandType("Bcast"), "de.hd.pvs.piosim.simulator.program.Bcast.BinaryTreeMultiplex");
		parameters.setTraceFile("/tmp/bcast");

		parameters.setTraceEnabled(true);

		pb.addBroadcast(world, 3, 100 * MiB);

		runSimulationAllExpectedToFinish();
	}

	@Test public void broadcastSimple() throws Exception{
		setup(5, 1);
		mb.getGlobalSettings().setMaxEagerSendSize(100 * KiB);
		mb.getGlobalSettings().setClientFunctionImplementation(	new CommandType("Bcast"), "de.hd.pvs.piosim.simulator.program.Bcast.BinaryTreeSimple");
		parameters.setTraceFile("/tmp/bcast");

		parameters.setTraceEnabled(true);

		pb.addBroadcast(world, 3, 100 * MiB);

		runSimulationAllExpectedToFinish();
	}

	abstract class ValidationExperiment{
		abstract String getName();
		abstract void addOperation(ProgramBuilder p);
		boolean createTrace(){
			return true;
		}
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

	@Test public void MPIIOLevelValidation() throws Exception{
		ServerCacheLayer cacheLayer = IOC.AggregationCache();

		BufferedWriter modelTime = new BufferedWriter(new FileWriter("/tmp/io-modelTime.txt"));

		modelTime.write("Cache settings: " + cacheLayer.toString() + "\n");
		// test with 10000 MiB main memory
		for(int i=1; i <= 5 ; i++){
			runMPIIOLevelValidation("10000MB ", i,i,cacheLayer,i,0,10, 104857600, 10000,false, modelTime);
		}
		runMPIIOLevelValidation("10000MB ",3 , 2,cacheLayer,3, 0,10, 104857600, 10000,false, modelTime);

		// test with 1000 MiB main memory
		for(int i=1; i <= 5 ; i++){
				runMPIIOLevelValidation("1GiG ", i,i,cacheLayer,i,0,10, 100 * MiB, 1000, false, modelTime);
		}
		runMPIIOLevelValidation("1GiG ",3 , 2,cacheLayer,3, 0,10, 104857600, 1000, false, modelTime);

		// test to run multiple processes on the client nodes
		for(int i=2; i <= 6 ; i++){
			runMPIIOLevelValidation("multiple ", 5, 5, cacheLayer,i*5,0,10, 100 * MiB, 1000, false, modelTime);
		}

		// overlapping test
		runMPIIOLevelValidation("overlapping ", 8,8,cacheLayer, 8, 8, 10, 100 * MiB, 2000, false, modelTime);

		modelTime.close();

		System.out.println("Completed");
	}

	@Test public void MPIIOLevelValidation100KByteBlocks() throws Exception{
		ServerCacheLayer cacheLayers [] = new ServerCacheLayer[]{IOC.SimpleWriteBehindCache(), IOC.AggregationCache(), IOC.AggregationReorderCache()};

		for (ServerCacheLayer cacheLayer : cacheLayers){
			BufferedWriter modelTime = new BufferedWriter(new FileWriter("/tmp/io-modelTime" + cacheLayer.getNiceName() + ".txt"));
			//MPIIOLevelValidationBlocks(100 * KiB, 10240, cacheLayer, modelTime);
			runMPIIOLevelValidation("10000MB ", 5,5, cacheLayer, 5 , 0, 10240, 100*KiB, 10000, true, modelTime);

			modelTime.close();
		}

		System.out.println("Completed");
	}

	@Test public void MPIIOTraceExample() throws Exception{
		BufferedWriter modelTime = new BufferedWriter(new FileWriter("/tmp/io-modelTime.txt"));
		runMPIIOLevelValidationSingle(0, true, "tst ",  1, 1, IOC.SimpleWriteBehindCache(), 1, 0, 10240, 100*KiB, 10000, true, modelTime);
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

	@Test public void myTestTrace() throws Exception{
		ServerCacheLayer cacheLayer = IOC.AggregationCache();
		//runMPIIOLevelValidation("1000 ", 3 , 2,cacheLayer,3, 0,10, 104857600, 1000, true, null);

		//runMPIIOLevelValidation("1000 ", 2 ,2, cacheLayer, 2, 0, 10, 104857600, 1000, true, null);
		//runMPIIOLevelValidation("overlapping ", 8,8,cacheLayer, 8, 8, 10, 100 * MiB, 2000, true, null);

		runMPIIOLevelValidationSingle(2, true, "10000MB ", 2, 1, IOC.SimpleWriteBehindCache(), 2, 0, 13000, 50*KiB, 10000,false, null);
	}


	@Test public void MPIIORunTest() throws Exception{
		setupWrCluster(1, 1, 1, 1, IOC.AggregationCache(), 1000);

		parameters.setTraceFile("/tmp/ios");
		parameters.setTraceEnabled(false);

		SimpleStripe dist = new SimpleStripe();
		dist.setChunkSize(64 * KiB);
		FileMetadata file =  aB.createFile("test", GiB, dist );

		FileDescriptor fd = pb.addFileOpen(file, world, false);
		pb.addWriteSequential(0, fd, 0, 100 * MiB);
		pb.addFileClose(fd);

		runSimulationAllExpectedToFinish();


		setupWrCluster(1, 1, 1, 2, IOC.AggregationCache(), 1000);

		parameters.setTraceFile("/tmp/ios");
		parameters.setTraceEnabled(true);
		file =  aB.createFile("test", GiB, dist );
		fd = pb.addFileOpen(file, world, false);
		pb.addWriteSequential(0, fd, 0, 100 * MiB);
		pb.addFileClose(fd);

		runSimulationAllExpectedToFinish();
	}

	@Test public void validationRuns() throws Exception{
		BufferedWriter outputFile = new BufferedWriter(new FileWriter("/tmp/validationRuns.txt"));
		outputFile.write("#Proc\tEvents\tRuntime\tSysModelT\tProgramMT\n");


		BufferedWriter modelTime = new BufferedWriter(new FileWriter("/tmp/validationRuns-modelTime.txt"));
		modelTime.write("# Experiment configuration & times \n");

		ValidationExperiment [] experiments = new ValidationExperiment[]{
				new ValidationExperiment() {

					@Override
					String getName() {
						return "Broadcast100M";
					}

					@Override
					void addOperation(ProgramBuilder p) {
						mb.getGlobalSettings().setClientFunctionImplementation(	new CommandType("Bcast"), "de.hd.pvs.piosim.simulator.program.Bcast.BinaryTreeNotMultiplexed");
						pb.addBroadcast(world, 0, 100 * MiB);
					}
				}
		};


		// test cases run on the WR cluster

		for(ValidationExperiment e: experiments){
			modelTime.write(e.getName() + " ");
			for(String config : configs){
				final String name = e.getName();
				final int nodes = Integer.parseInt(config.split("-")[0]);
				final int processes = Integer.parseInt(config.split("-")[1]);

				long sTime, setupSystemTime, setupProgramTime;
				// dual socket configuration.
				sTime = new Date().getTime();

				setupWrCluster(nodes, processes);

				setupSystemTime = (new Date().getTime() - sTime);

				parameters.setTraceEnabled(e.createTrace());
				parameters.setTraceFile("/tmp/" + name + "_" +  config);

				sTime = new Date().getTime();

				e.addOperation(pb);

				setupProgramTime = (new Date().getTime() - sTime);
				runSimulationWithoutOutput();

				outputFile.write(name + "\t" + config + "\t" + simRes.getEventCount() + "\t" + simRes.getWallClockTime() + "\t" + setupSystemTime  / 1000.0 + "\t" + setupProgramTime  / 1000.0 + "\n");
				outputFile.flush();

				modelTime.write(simRes.getVirtualTime().getDouble() + " ");
				modelTime.flush();
			}
			modelTime.write("\n");
		}
		outputFile.close();
	}


	@Test public void allComputeMPIValidationTests() throws Exception{
		BufferedWriter modelTime = new BufferedWriter(new FileWriter("/tmp/collectives-modelTime.txt"));

		modelTime.write("# Experiment ");
		for(String config: configs){
			modelTime.write(config + " ");
		}
		modelTime.write("\n");

		validationRunCollectiveWithSendRecv(modelTime);
		sendRecvRingRightLeftNeighbor(modelTime);
		sendRecvPaired(modelTime);
		sendRootWhichReceives(modelTime);
		sendRecvRoot(modelTime);

		System.out.println("All Completed!");
		modelTime.write("\nCompleted!\n");
		modelTime.close();
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

				for(int rank=0; rank < processes ; rank++){
				    int dest = rank % 2 == 0 ? rank + 1 : rank -1;

					pb.addSendRecv(world, rank, dest, dest, size, 4711, 4711);
				}

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

				for(int rank=1; rank < processes ; rank++){
					pb.addSendAndRecv(world, rank, 0, size, 4711);
				}

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

				for(int rank=1; rank < processes ; rank++){
					pb.addSendRecv(world, rank, 0, 0, size, 4711, 4711);
					pb.addSendRecv(world, 0, rank, rank, size, 4711, 4711);
				}

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

				for(int rank=0; rank < processes ; rank++){
				    int dest = (rank == processes - 1) ? 0 : rank + 1;
				    int src = (rank == 0) ? processes - 1 : rank - 1;

					pb.addSendRecv(world, rank, src, dest, size, 4711, 4711);
				}

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

		final String prefix = "/home/kunkel/Dokumente/Dissertation/Trace/results-git/compute-only/extracted-communication-patterns/";


		// test cases run on the WR cluster


		String [] experiments = new String[]{ "Reduce", "Allreduce", "Bcast", "Barrier",  "Allgather", "Gather", "Scatter"};

		FilenameFilter projFilter = new FilenameFilter() {
	           public boolean accept(File dir, String name) {
	                return name.endsWith(".proj");
	            }
	    };

		final ApplicationXMLReader axml = new ApplicationXMLReader();

		for(String experiment: experiments){
			for(int size: sizes){

				String sizeStr = "" + size;
				if(experiments.equals("Barrier")){
					if( size > sizes[0]){
						continue;
					}
					sizeStr = "";
				}

				modelTime.write(experiment + sizeStr + " ");

				for(String config: configs){

					final int nodes = Integer.parseInt(config.split("-")[0]);
					final int processes = Integer.parseInt(config.split("-")[1]);

					long sTime, setupSystemTime, setupProgramTime;
					// dual socket configuration.
					sTime = new Date().getTime();

					System.out.println(config);

					setupWrCluster(nodes, processes);

					setupSystemTime = (new Date().getTime() - sTime);

					sTime = new Date().getTime();

					// load traces
					final String folder = prefix + config + "/" + experiment + "/" + sizeStr;
					File f = new File(folder);
					String files[] = f.list(projFilter);
					if (files == null || files.length != 1){
						System.err.println("Invalid configuration: " + folder);
						outputFile.write("Invalid configuration: " + folder);
						// we know the time is 0.0 for the configuration 1-1
						modelTime.write("0.0 ");

						continue;
					}
					String proj=folder + "/" + files[0];

					System.out.println(proj);
					outputFile.write(proj);
					outputFile.flush();

					// don't do any computation ...
					// TODO run both tests at the same time
					//mb.getGlobalSettings().setClientFunctionImplementation(	new CommandType("Compute"), "de.hd.pvs.piosim.simulator.program.Global.NoOperation");


					// load program:
					final Application app = axml.parseApplication(proj, true);
					mb.setApplication("Validate", app);


					setupProgramTime = (new Date().getTime() - sTime);
					runSimulationWithoutOutput();

					outputFile.write("\t" + config + "\t" + simRes.getEventCount() + "\t" + simRes.getWallClockTime() + "\t" + setupSystemTime  / 1000.0 + "\t" + setupProgramTime  / 1000.0 + "\n");
					outputFile.flush();

					modelTime.write(simRes.getVirtualTime().getDouble() + " ");
					modelTime.flush();
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

	@Test public void timingLargeData() throws Exception{
		BufferedOutputStream outputFile = new BufferedOutputStream(new FileOutputStream(new File("/tmp/timing")));

		boolean asserts = false;

		// set the value:
//		assert( asserts = true );

		outputFile.write(("Assertions enabled: " + asserts).getBytes());

		for(int i=0; i < 3 ; i++){
			setupSMP(2, 1);
			mb.getGlobalSettings().setMaxEagerSendSize(100 * KiB);
			pb.addSendAndRecv(world, 0, 1, 100000* MiB, 4711);
			parameters.setTraceEnabled(false);

			runSimulationAllExpectedToFinish();

			final SimulationResultSerializer serializer = new SimulationResultSerializer();
			outputFile.write(serializer.serializeResults(simRes).toString().getBytes());
			outputFile.flush();
		}

		outputFile.close();
	}

	@Test public void broadcastTreeAnalytical() throws Exception{
		setupAnalytical(8, 1);
		mb.getGlobalSettings().setMaxEagerSendSize(100 * KiB);
		mb.getGlobalSettings().setClientFunctionImplementation(	new CommandType("Bcast"), "de.hd.pvs.piosim.simulator.program.Bcast.BinaryTreeNotMultiplexed");

		parameters.setTraceEnabled(false);
		parameters.setTraceFile("/tmp/bcast");


		pb.addBroadcast(world, 3, 100 * MiB);

		runSimulationWithoutOutput();
	}


	@Test public void broadcastTreeAnalyticalIterative() throws Exception{
		int count = 1;

		BufferedOutputStream outputFile = new BufferedOutputStream(new FileOutputStream(new File("/tmp/treeAnalyticalIterative.txt")));
		outputFile.write(("#Proc\tEvents\tRuntime\tSysModelT\tProgramMT\n").getBytes());

		for(int i=0; i < 11;i++){
			count = count*2;
			long sTime, setupSystemTime, setupProgramTime;

			sTime = new Date().getTime();
			setupAnalytical(count, 1);
			setupSystemTime = (new Date().getTime() - sTime);


			mb.getGlobalSettings().setMaxEagerSendSize(100 * KiB);
			mb.getGlobalSettings().setClientFunctionImplementation(	new CommandType("Bcast"), "de.hd.pvs.piosim.simulator.program.Bcast.BinaryTreeNotMultiplexed");

			parameters.setTraceEnabled(false);
			parameters.setTraceFile("/tmp/bcast");


			sTime = new Date().getTime();
			pb.addBroadcast(world, 0, 100 * MiB);
			setupProgramTime = (new Date().getTime() - sTime);


			runSimulationWithoutOutput();

			outputFile.write((count + "\t" + simRes.getEventCount() + "\t" + simRes.getWallClockTime() + "\t" + setupSystemTime  / 1000.0 + "\t" + setupProgramTime  / 1000.0 + "\n").getBytes());
			outputFile.flush();
		}
		outputFile.close();
	}


	@Test public void broadcastSimpleBlockwise() throws Exception{
		setup(5, 1);
		mb.getGlobalSettings().setMaxEagerSendSize(100 * KiB);
		mb.getGlobalSettings().setClientFunctionImplementation(	new CommandType("Bcast"), "de.hd.pvs.piosim.simulator.program.Bcast.BinaryTreeSimpleBlockwise");
		parameters.setTraceFile("/tmp/bcast");

		parameters.setTraceEnabled(true);

		pb.addBroadcast(world, 3, 100 * MiB);

		runSimulationAllExpectedToFinish();
	}




	@Test public void test() throws Exception{
		setupSMP(2);
		mb.getGlobalSettings().setMaxEagerSendSize(100 * KiB);
		pb.addSendAndRecv(world, 0, 1, 100 * KiB, 1);

		parameters.setTraceFile("/tmp/out");
		parameters.setTraceEnabled(true);

		runSimulationAllExpectedToFinish();
	}

	@Test public void broadcastBroadcastScatterGatherall() throws Exception{
		setup(8, 1);
		mb.getGlobalSettings().setMaxEagerSendSize(100 * KiB);
		mb.getGlobalSettings().setClientFunctionImplementation(	new CommandType("Bcast"), "de.hd.pvs.piosim.simulator.program.Bcast.BroadcastScatterGatherall");

		parameters.setTraceFile("/tmp/bcast");
		parameters.setTraceEnabled(true);

		pb.addBroadcast(world, 0,100 * MiB);

		runSimulationAllExpectedToFinish();
	}


	@Test public void broadcastBroadcastPipedBlockwise() throws Exception{
		//setup(3, 1);
		setupSMP(3);
		mb.getGlobalSettings().setMaxEagerSendSize(100 * KiB);
		mb.getGlobalSettings().setClientFunctionImplementation(	new CommandType("Bcast"), "de.hd.pvs.piosim.simulator.program.Bcast.PipedBlockwise");

		parameters.setTraceFile("/tmp/bcast");
		parameters.setTraceEnabled(true);
		parameters.setTraceInternals(true);

		pb.addBroadcast(world, 0, 10 * MiB);

		runSimulationAllExpectedToFinish();
	}

	@Test public void broadcastBroadcastScatterBarrierGatherall() throws Exception{
		setupWrCluster(2, 7);
		mb.getGlobalSettings().setMaxEagerSendSize(100 * KiB);
		mb.getGlobalSettings().setClientFunctionImplementation(	new CommandType("Bcast"), "de.hd.pvs.piosim.simulator.program.Bcast.BroadcastScatterBarrierGatherall");

		parameters.setTraceFile("/tmp/bcast");
		parameters.setTraceEnabled(true);
		parameters.setTraceInternals(true);

		pb.addBroadcast(world, 0,100 * MiB);

		runSimulationAllExpectedToFinish();
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

	@Test public void bcastTest() throws Exception{
		double [] times = new double[11];

		for(int i=1; i <= 10; i++){
			setup(i,1);

			pb.addBroadcast(world,  (i - 2 >= 0 ? i -2 : 0), 100 * MiB);
			runSimulationAllExpectedToFinish();
			times[i] = sim.getVirtualTime().getDouble();
		}

		printTiming("Broadcast", times);
	}

	@Test public void sendAndRecvEagerTestSMP() throws Exception{
		setupSMP(2);
		mb.getGlobalSettings().setMaxEagerSendSize(100 * KiB);
		mb.getGlobalSettings().setClientFunctionImplementation(
				new CommandType("Bcast"), "de.hd.pvs.piosim.simulator.program.Bcast.BinaryTree");  //andere Implementation
		//CommandToSimulationMapper Eintrag, Standard letzter

		parameters.setTraceFile("/tmp/sendRecvTest");

		parameters.setTraceEnabled(true);

		pb.addSendAndRecv(world, 0, 1, 10 * MiB, 1);

		runSimulationAllExpectedToFinish();
	}



	@Test public void MPICH2Reduce() throws Exception{
		setup(2, 1);

		mb.getGlobalSettings().setMaxEagerSendSize(100 * KiB);
		mb.getGlobalSettings().setClientFunctionImplementation(
				new CommandType("Bcast"), "de.hd.pvs.piosim.simulator.program.Reduce.ReduceScatterGatherMPICH2");  //andere Implementation
		//CommandToSimulationMapper Eintrag, Standard letzter

		parameters.setTraceFile("/tmp/mpich2reduce");

		parameters.setTraceEnabled(true);

		pb.addReduce(world, 0, 10 * KiB);

		runSimulationAllExpectedToFinish();
	}



	@Test public void TestScatterHierachicalTwoLevels() throws Exception{
		setup(5, 1);

		mb.getGlobalSettings().setMaxEagerSendSize(100 * KiB);
		mb.getGlobalSettings().setClientFunctionImplementation(
				new CommandType("Scatter"), "de.hd.pvs.piosim.simulator.program.Scatter.ScatterHierachicalTwoLevels");  //andere Implementation
		//CommandToSimulationMapper Eintrag, Standard letzter

		parameters.setTraceFile("/tmp/scatter");

		parameters.setTraceEnabled(true);

		pb.addScatter(world, 0, 10 * KiB);

		runSimulationAllExpectedToFinish();
	}

	@Test public void TestScatterMPICH2() throws Exception{
		setup(2, 3);

		mb.getGlobalSettings().setMaxEagerSendSize(100 * KiB);
		mb.getGlobalSettings().setClientFunctionImplementation(	new CommandType("Scatter"), "de.hd.pvs.piosim.simulator.program.Scatter.ScatterMPICH2");

		parameters.setTraceFile("/tmp/scatter");

	//	parameters.setTraceInternals(true);
		parameters.setTraceEnabled(true);

		pb.addScatter(world, 0, 100* MiB);

		runSimulationAllExpectedToFinish();
	}

	@Test public void TestGatherDirect() throws Exception{
		setupSMP(5);

		mb.getGlobalSettings().setClientFunctionImplementation(	new CommandType("Gather"), "de.hd.pvs.piosim.simulator.program.Gather.Direct");

		parameters.setTraceFile("/tmp/gather");
		parameters.setTraceEnabled(true);

		pb.addGather(world, 0, 100* MiB);

		runSimulationAllExpectedToFinish();
	}


	@Test public void TestGatherWorld() throws Exception{
		setupSMP(15);


		mb.getGlobalSettings().setClientFunctionImplementation(	new CommandType("Gather"), "de.hd.pvs.piosim.simulator.program.Gather.GatherBinaryTreeMPICH2");

		parameters.setTraceFile("/tmp/gather");
		parameters.setTraceEnabled(true);

		pb.addGather(world, 0, 100* MiB);

		runSimulationAllExpectedToFinish();
	}


	@Test public void TestScatter3World() throws Exception{
		setupSMP(5);


		mb.getGlobalSettings().setClientFunctionImplementation(	new CommandType("Scatter"), "de.hd.pvs.piosim.simulator.program.Scatter.ScatterMPICH2");

		parameters.setTraceFile("/tmp/scatter");
		parameters.setTraceEnabled(true);

		pb.addScatter(world, 4, 100* MiB);

		runSimulationAllExpectedToFinish();
	}

	@Test public void TestScatter2() throws Exception{
		setupSMP(2);


		mb.getGlobalSettings().setClientFunctionImplementation(	new CommandType("Scatter"), "de.hd.pvs.piosim.simulator.program.Scatter.ScatterMPICH2");

		parameters.setTraceFile("/tmp/scatter");
		parameters.setTraceEnabled(true);

		Communicator comm = new Communicator("world");
		comm.addRank(0, 1, 0);
		comm.addRank(1, 0, 0);

		pb.addScatter(comm, 1, 100* MiB);

		runSimulationAllExpectedToFinish();
	}



	@Test public void TestScatterMPICH5ProcessesOnThreeNodes() throws Exception{
		setup(2, 3);


		mb.getGlobalSettings().setClientFunctionImplementation(	new CommandType("Scatter"), "de.hd.pvs.piosim.simulator.program.Scatter.ScatterMPICH2");

		parameters.setTraceFile("/tmp/scatter");
		parameters.setTraceEnabled(true);

		Communicator comm = new Communicator("world");
		comm.addRank(0, 0, 0);
		comm.addRank(1, 2, 0);
		comm.addRank(2, 4, 0);
		comm.addRank(3, 1, 0);
		comm.addRank(4, 3, 0);

		pb.addScatter(comm, 0, 100* MiB);

		runSimulationAllExpectedToFinish();
	}



	@Test public void TestScatterFCFS() throws Exception{
		setup(3, 1);

		mb.getGlobalSettings().setMaxEagerSendSize(100 * KiB);
		mb.getGlobalSettings().setClientFunctionImplementation(
				new CommandType("Scatter"), "de.hd.pvs.piosim.simulator.program.Scatter.FCFS");
		parameters.setTraceFile("/tmp/scatterFCFS");
		parameters.setTraceEnabled(true);
		pb.addScatter(world, 0, 10 * MiB);
		runSimulationAllExpectedToFinish();
	}

	@Test public void TestGatherFCFS() throws Exception{
		setup(3, 1);

		mb.getGlobalSettings().setMaxEagerSendSize(100 * KiB);
		mb.getGlobalSettings().setClientFunctionImplementation(
				new CommandType("Scatter"), "de.hd.pvs.piosim.simulator.program.Gather.FCFS");
		parameters.setTraceFile("/tmp/gatherFCFS");
		parameters.setTraceEnabled(true);
		pb.addGather(world, 0, 10 * MiB);
		runSimulationAllExpectedToFinish();
	}



	@Test public void TestScatterFCFSOrdered123() throws Exception{
			setup(4,1);

			parameters.setTraceFile("/tmp/scatter");
			parameters.setTraceEnabled(true);

			mb.getGlobalSettings().setMaxEagerSendSize(100 * KiB);
			mb.getGlobalSettings().setClientFunctionImplementation(
					new CommandType("Scatter"), "de.hd.pvs.piosim.simulator.program.Scatter.FCFS");

			pb.addCompute(1, 10000000);
			pb.addCompute(2, 20000000);
			pb.addCompute(3, 30000000);

			pb.addScatter(world, 0, 10 * MiB);
			runSimulationAllExpectedToFinish();
	}

	@Test public void TestScatterFCFSLate() throws Exception{
		setup(4,1);

		parameters.setTraceFile("/tmp/scatter");
		parameters.setTraceEnabled(true);

		mb.getGlobalSettings().setMaxEagerSendSize(100 * KiB);
		mb.getGlobalSettings().setClientFunctionImplementation(
				new CommandType("Scatter"), "de.hd.pvs.piosim.simulator.program.Scatter.FCFS");

		pb.addCompute(0, 10000000);

		pb.addScatter(world, 0, 10 * MiB);
		runSimulationAllExpectedToFinish();
}

	@Test public void TestScatterFCFSEqual() throws Exception{
		setup(4,1);

		parameters.setTraceFile("/tmp/scatter");
		parameters.setTraceEnabled(true);

		mb.getGlobalSettings().setMaxEagerSendSize(100 * KiB);
		mb.getGlobalSettings().setClientFunctionImplementation(
				new CommandType("Scatter"), "de.hd.pvs.piosim.simulator.program.Scatter.FCFS");

		pb.addScatter(world, 0, 10 * MiB);
		runSimulationAllExpectedToFinish();
}




	public void runJacobi_1C1S() throws Exception{
		final String which =
			"/home/julian/Dokumente/Geschäft/Dissertation/Simulation-Results/paper/trace/partdiff-par.proj";

		AggregationCache cache = new AggregationCache();
		cache.setName("PVS-CACHE");
		cache.setMaxNumberOfConcurrentIOOps(1);

		//setupDisjointIO(1, 1, 1, 12000, cache);

		parameters.setTraceFile("/tmp/jacobi1C1S");
		parameters.setTraceEnabled(true);

		final ApplicationXMLReader axml = new ApplicationXMLReader();
		final Application app = axml.parseApplication(which, true);

		mb.setApplication("Jacobi", app);

		final ClientProcess p = mb.getModel().getClientProcesses().get(0);
		p.setApplication("Jacobi");
		p.setRank(0);

		runSimulationAllExpectedToFinish();
	}

	public static void main(String[] args) throws Exception{
		Validation v = new Validation();
		v.broadcastTreeAnalyticalIterative();
	}
}
