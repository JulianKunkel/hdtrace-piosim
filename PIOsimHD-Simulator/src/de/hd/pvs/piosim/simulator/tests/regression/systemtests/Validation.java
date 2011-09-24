package de.hd.pvs.piosim.simulator.tests.regression.systemtests;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Date;

import org.junit.Test;

import de.hd.pvs.piosim.model.ModelBuilder;
import de.hd.pvs.piosim.model.components.ClientProcess.ClientProcess;
import de.hd.pvs.piosim.model.components.ServerCacheLayer.AggregationCache;
import de.hd.pvs.piosim.model.dynamicMapper.CommandType;
import de.hd.pvs.piosim.model.networkTopology.RoutingAlgorithm.PaketFirstRoute;
import de.hd.pvs.piosim.model.networkTopology.RoutingAlgorithm.PaketRoutingAlgorithm;
import de.hd.pvs.piosim.model.program.Application;
import de.hd.pvs.piosim.model.program.ApplicationBuilder;
import de.hd.pvs.piosim.model.program.ApplicationXMLReader;
import de.hd.pvs.piosim.model.program.Communicator;
import de.hd.pvs.piosim.model.program.ProgramBuilder;
import de.hd.pvs.piosim.simulator.SimulationResultSerializer;
import de.hd.pvs.piosim.simulator.tests.regression.systemtests.hardwareConfigurations.NICC;
import de.hd.pvs.piosim.simulator.tests.regression.systemtests.hardwareConfigurations.NetworkEdgesC;
import de.hd.pvs.piosim.simulator.tests.regression.systemtests.hardwareConfigurations.NetworkNodesC;
import de.hd.pvs.piosim.simulator.tests.regression.systemtests.hardwareConfigurations.NodesC;
import de.hd.pvs.piosim.simulator.tests.regression.systemtests.topologies.ClusterT;
import de.hd.pvs.piosim.simulator.tests.regression.systemtests.topologies.HardwareConfiguration;
import de.hd.pvs.piosim.simulator.tests.regression.systemtests.topologies.NodeT;
import de.hd.pvs.piosim.simulator.tests.regression.systemtests.topologies.SMTNodeT;
import de.hd.pvs.piosim.simulator.tests.regression.systemtests.topologies.SMTSocketNodeT;

public class Validation  extends ModelTest {

	String [] configs = new String[]{"1-1","1-2","1-3","1-4","1-5","1-6","1-7","1-8","1-9","1-10","1-11","1-12",
			"2-2","2-3","2-4","2-5","2-7","2-9", "2-11",
			"3-3","3-6","4-4","4-8","5-5","5-10","6-6","6-12","7-7","7-14","8-8","8-16","9-9","9-18","10-10","10-20"};

	int [] sizes = {10240, 1048576, 10485760, 104857600};

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
	 * @param nodeCount
	 * @param processes
	 * @throws Exception
	 */

	protected void setupWrCluster(int nodeCount, int processes, int servers) throws Exception {

	}


	/**
	 * Setup a configuration equal to the WR cluster configuration.
	 * @param nodeCount
	 * @param processes
	 * @throws Exception
	 */
	protected void setupWrCluster(int nodeCount, int processes) throws Exception {
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
				NodesC.PVSSMPNode(procsPerSocket * socketCount),
				NetworkNodesC.SocketLocalNode(),
				NetworkEdgesC.SocketLocalEdge(),
				NetworkNodesC.QPI(),
				NetworkEdgesC.QPI() );
		}else if(true){
			smtNodeT = new SMTSocketNodeT(procsPerSocket,
					socketCount,
					NICC.NICAnalytical(),
					NodesC.PVSSMPNode(procsPerSocket * socketCount),
					NetworkNodesC.SocketLocalNode(),
					NetworkEdgesC.SocketLocalEdge(),
					NetworkNodesC.QPI(),
					NetworkEdgesC.QPI() );
		}

		HardwareConfiguration config;

		if(true){ // real cluster value vs. GiGE throughput
			config = new ClusterT(nodeCount, NetworkEdgesC.GIGEPVS(),NetworkNodesC.GIGSwitch(), smtNodeT);
		}else{ // faster network
			config = new ClusterT(nodeCount, NetworkEdgesC.GIGE(),NetworkNodesC.GIGSwitch(), smtNodeT);
		}

		if(true){ // real vs. latency bound.
		}else{
			smtNodeT = new SMTSocketNodeT(procsPerSocket,
					socketCount,
					NICC.PVSNIC(),
					NodesC.PVSSMPNode(procsPerSocket * socketCount),
					NetworkNodesC.SocketLocalNode(),
					NetworkEdgesC.SocketLocalNoLatencyEdge(),
					NetworkNodesC.QPI(),
					NetworkEdgesC.QPINoLatency() );
			config = new ClusterT(nodeCount, NetworkEdgesC.GIGEPVSNoLatency(),NetworkNodesC.GIGSwitch(), smtNodeT);
		}

		parameters.setLoggerDefinitionFile("loggerDefinitionFiles/example");
		parameters.setTraceEnabled(false);
		parameters.setTraceInternals(false);
		parameters.setTraceClientSteps(true);
		parameters.setTraceServers(true);

		PaketRoutingAlgorithm routingAlgorithm = new PaketFirstRoute();
		mb = new ModelBuilder();
		topology = mb.createTopology("LAN");
		topology.setRoutingAlgorithm(routingAlgorithm);
		config.createModel("", mb, topology);

		aB = new ApplicationBuilder("Validate", "Validation runs", processes, 1);
		app = aB.getApplication();

		// build a dummy app for all nodes
		ApplicationBuilder dummy = new ApplicationBuilder("Test", "Test", procsPerSocket * socketCount * nodeCount, 1);
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
		if(false){ //  => nodes, sockets, PEs

			for(int rank = 0; rank < processes; rank++){

				int physicalCPU = curNode * procsPerNode + curSocket * procsPerSocket + curProc;
				//System.out.println("rank: " + rank + " node: " + curNode + " socket: " + curSocket + " proc: " + curProc + " physicalCPU: " + physicalCPU);

				curNode++;
				if (curNode >= nodeCount){
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
		}else{ // => PEs on one node, one socket
			int numberOfProcsPerNode = processes / nodeCount;
			int restProcs = processes % nodeCount;
			int rank = 0;
			for(int node = 0; node < nodeCount; node++){
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
				ClientProcess c = clients[procsPerNode * (nodeCount-1) + numberOfProcsPerNode + rest];
				c.setApplication("Validate");
				c.setRank(rank);
				c.setName("" + rank);

				rank++;
			}
		}

		mb.setApplication("Validate", app);


		world = aB.getWorldCommunicator();
		model = mb.getModel();
		mb.getGlobalSettings().setMaxEagerSendSize(100 * KBYTE);
	}



	@Test public void sendRecvData() throws Exception{
		final int pairs = 2;
		setupSMP(pairs * 2, 1);
		mb.getGlobalSettings().setMaxEagerSendSize(100 * KBYTE);
		for(int i=0; i < pairs ; i++){
			pb.addSendAndRecv(world, i, i+pairs, 1000* MBYTE, 4711);
		}

		runSimulationAllExpectedToFinish();
	}


	@Test public void sendRecvIntersocketData() throws Exception{
		BufferedWriter modelTime = new BufferedWriter(new FileWriter("/tmp/validationRuns-modelTime-sendRecv.txt"));
		modelTime.write("# Experiment configuration & times \n");
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

				final int pairs = processes / 2;
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



	@Test public void sendRootIntersocketData() throws Exception{
		BufferedWriter modelTime = new BufferedWriter(new FileWriter("/tmp/validationRuns-modelTime-sendRoot.txt"));
		modelTime.write("# Experiment configuration & times \n");
		// test cases run on the WR cluster

		for(int size: sizes){
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


	@Test public void sendRecvRootIntersocketData() throws Exception{
		BufferedWriter modelTime = new BufferedWriter(new FileWriter("/tmp/validationRuns-modelTime-sendRecvRoot.txt"));
		modelTime.write("# Experiment configuration & times \n");
		// test cases run on the WR cluster

		for(int size: sizes){
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

	@Test public void sendRecvRightNeighborIntersocketData() throws Exception{
		BufferedWriter modelTime = new BufferedWriter(new FileWriter("/tmp/validationRuns-modelTime-sendRecvRN.txt"));
		modelTime.write("# Experiment configuration & times \n");
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

	@Test public void sendRecvIntersocketValidate() throws Exception{
		System.out.println("Single socket");

		setupSMP(2, 1);
		mb.getGlobalSettings().setMaxEagerSendSize(100 * KBYTE);
		pb.addSendRecv(world, 0, 1, 1, 0, 100, 101);
		pb.addSendRecv(world, 1, 0, 0, 0, 101, 100);
		runSimulationAllExpectedToFinish();


		System.out.println("Across two sockets");
		setupSMP(1, 2);
		mb.getGlobalSettings().setMaxEagerSendSize(100 * KBYTE);
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
		mb.getGlobalSettings().setMaxEagerSendSize(100 * KBYTE);

		parameters.setTraceFile("/tmp/recv1MB");   //Ausgabedatei

		parameters.setTraceEnabled(true); 	//Trace An

		pb.addReduce(world, 0, 1* MBYTE);  	// (-,-,Größe)

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
		mb.getGlobalSettings().setMaxEagerSendSize(100 * KBYTE);
		mb.getGlobalSettings().setClientFunctionImplementation(	new CommandType("Allreduce"), "de.hd.pvs.piosim.simulator.program.Allreduce.RootComputes");
		parameters.setTraceFile("/tmp/allreduce");

		parameters.setTraceEnabled(true);

		pb.addAllreduce(world, 10 * MBYTE);

		runSimulationAllExpectedToFinish();
	}


	@Test public void barrierTree() throws Exception{
		setup(8, 1);
		mb.getGlobalSettings().setMaxEagerSendSize(100 * KBYTE);
		mb.getGlobalSettings().setClientFunctionImplementation(	new CommandType("Barrier"), "de.hd.pvs.piosim.simulator.program.Barrier.BinaryTree");
		parameters.setTraceFile("/tmp/barrier");

		parameters.setTraceEnabled(true);

		pb.addBarrier(world);

		runSimulationAllExpectedToFinish();
	}


	@Test public void broadcastMultiplex() throws Exception{
		setup(5, 1);
		mb.getGlobalSettings().setMaxEagerSendSize(100 * KBYTE);
		mb.getGlobalSettings().setClientFunctionImplementation(	new CommandType("Bcast"), "de.hd.pvs.piosim.simulator.program.Bcast.BinaryTreeMultiplex");
		parameters.setTraceFile("/tmp/bcast");

		parameters.setTraceEnabled(true);

		pb.addBroadcast(world, 3, 100 * MBYTE);

		runSimulationAllExpectedToFinish();
	}

	@Test public void broadcastSimple() throws Exception{
		setup(5, 1);
		mb.getGlobalSettings().setMaxEagerSendSize(100 * KBYTE);
		mb.getGlobalSettings().setClientFunctionImplementation(	new CommandType("Bcast"), "de.hd.pvs.piosim.simulator.program.Bcast.BinaryTreeSimple");
		parameters.setTraceFile("/tmp/bcast");

		parameters.setTraceEnabled(true);

		pb.addBroadcast(world, 3, 100 * MBYTE);

		runSimulationAllExpectedToFinish();
	}

	abstract class ValidationExperiment{
		abstract String getName();
		abstract void addOperation(ProgramBuilder p);
		boolean createTrace(){
			return true;
		}
	}

	@Test public void MPIIORuns() throws Exception{

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
						pb.addBroadcast(world, 0, 100 * MBYTE);
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




	@Test public void validationRunCollectiveWithSendRecv() throws Exception{
		BufferedWriter outputFile = new BufferedWriter(new FileWriter("/tmp/collectives-runTime.txt"));
		outputFile.write("#Proc\tEvents\tRuntime\tSysModelT\tProgramMT\n");

		final String prefix = "/home/kunkel/Dokumente/Dissertation/Trace/results-git/compute-only/extracted-communication-patterns/";

		BufferedWriter modelTime = new BufferedWriter(new FileWriter("/tmp/collectives-modelTime.txt"));
		modelTime.write("# Experiment ");
		for(String config: configs){
			modelTime.write(config + " ");
		}
		modelTime.write("\n");


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

				modelTime.write(experiment + size + " ");

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
					final String folder = prefix + config + "/" + experiment + "/" + size;
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
			}
		}
		outputFile.close();

		System.out.println("Completed!");
	}

	@Test public void timingLargeData() throws Exception{
		BufferedOutputStream outputFile = new BufferedOutputStream(new FileOutputStream(new File("/tmp/timing")));

		boolean asserts = false;

		// set the value:
//		assert( asserts = true );

		outputFile.write(("Assertions enabled: " + asserts).getBytes());

		for(int i=0; i < 3 ; i++){
			setupSMP(2, 1);
			mb.getGlobalSettings().setMaxEagerSendSize(100 * KBYTE);
			pb.addSendAndRecv(world, 0, 1, 100000* MBYTE, 4711);
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
		mb.getGlobalSettings().setMaxEagerSendSize(100 * KBYTE);
		mb.getGlobalSettings().setClientFunctionImplementation(	new CommandType("Bcast"), "de.hd.pvs.piosim.simulator.program.Bcast.BinaryTreeNotMultiplexed");

		parameters.setTraceEnabled(false);
		parameters.setTraceFile("/tmp/bcast");


		pb.addBroadcast(world, 3, 100 * MBYTE);

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


			mb.getGlobalSettings().setMaxEagerSendSize(100 * KBYTE);
			mb.getGlobalSettings().setClientFunctionImplementation(	new CommandType("Bcast"), "de.hd.pvs.piosim.simulator.program.Bcast.BinaryTreeNotMultiplexed");

			parameters.setTraceEnabled(false);
			parameters.setTraceFile("/tmp/bcast");


			sTime = new Date().getTime();
			pb.addBroadcast(world, 0, 100 * MBYTE);
			setupProgramTime = (new Date().getTime() - sTime);


			runSimulationWithoutOutput();

			outputFile.write((count + "\t" + simRes.getEventCount() + "\t" + simRes.getWallClockTime() + "\t" + setupSystemTime  / 1000.0 + "\t" + setupProgramTime  / 1000.0 + "\n").getBytes());
			outputFile.flush();
		}
		outputFile.close();
	}


	@Test public void broadcastSimpleBlockwise() throws Exception{
		setup(5, 1);
		mb.getGlobalSettings().setMaxEagerSendSize(100 * KBYTE);
		mb.getGlobalSettings().setClientFunctionImplementation(	new CommandType("Bcast"), "de.hd.pvs.piosim.simulator.program.Bcast.BinaryTreeSimpleBlockwise");
		parameters.setTraceFile("/tmp/bcast");

		parameters.setTraceEnabled(true);

		pb.addBroadcast(world, 3, 100 * MBYTE);

		runSimulationAllExpectedToFinish();
	}




	@Test public void test() throws Exception{
		setupSMP(2);
		mb.getGlobalSettings().setMaxEagerSendSize(100 * KBYTE);
		pb.addSendAndRecv(world, 0, 1, 100 * KBYTE, 1);

		parameters.setTraceFile("/tmp/out");
		parameters.setTraceEnabled(true);

		runSimulationAllExpectedToFinish();
	}

	@Test public void broadcastBroadcastScatterGatherall() throws Exception{
		setup(8, 1);
		mb.getGlobalSettings().setMaxEagerSendSize(100 * KBYTE);
		mb.getGlobalSettings().setClientFunctionImplementation(	new CommandType("Bcast"), "de.hd.pvs.piosim.simulator.program.Bcast.BroadcastScatterGatherall");

		parameters.setTraceFile("/tmp/bcast");
		parameters.setTraceEnabled(true);

		pb.addBroadcast(world, 0,100 * MBYTE);

		runSimulationAllExpectedToFinish();
	}


	@Test public void broadcastBroadcastPipedBlockwise() throws Exception{
		//setup(3, 1);
		setupSMP(3);
		mb.getGlobalSettings().setMaxEagerSendSize(100 * KBYTE);
		mb.getGlobalSettings().setClientFunctionImplementation(	new CommandType("Bcast"), "de.hd.pvs.piosim.simulator.program.Bcast.PipedBlockwise");

		parameters.setTraceFile("/tmp/bcast");
		parameters.setTraceEnabled(true);
		parameters.setTraceInternals(true);

		pb.addBroadcast(world, 0, 10 * MBYTE);

		runSimulationAllExpectedToFinish();
	}

	@Test public void broadcastBroadcastScatterBarrierGatherall() throws Exception{
		setup(10, 1);
		mb.getGlobalSettings().setMaxEagerSendSize(100 * KBYTE);
		mb.getGlobalSettings().setClientFunctionImplementation(	new CommandType("Bcast"), "de.hd.pvs.piosim.simulator.program.Bcast.BroadcastScatterBarrierGatherall");

		parameters.setTraceFile("/tmp/bcast");
		parameters.setTraceEnabled(true);
		parameters.setTraceInternals(true);

		pb.addBroadcast(world, 0,100 * MBYTE);

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

			pb.addBroadcast(world,  (i - 2 >= 0 ? i -2 : 0), 100 * MBYTE);
			runSimulationAllExpectedToFinish();
			times[i] = sim.getVirtualTime().getDouble();
		}

		printTiming("Broadcast", times);
	}

	@Test public void sendAndRecvEagerTestSMP() throws Exception{
		setupSMP(2);
		mb.getGlobalSettings().setMaxEagerSendSize(100 * KBYTE);
		mb.getGlobalSettings().setClientFunctionImplementation(
				new CommandType("Bcast"), "de.hd.pvs.piosim.simulator.program.Bcast.BinaryTree");  //andere Implementation
		//CommandToSimulationMapper Eintrag, Standard letzter

		parameters.setTraceFile("/tmp/sendRecvTest");

		parameters.setTraceEnabled(true);

		pb.addSendAndRecv(world, 0, 1, 10 * MBYTE, 1);

		runSimulationAllExpectedToFinish();
	}



	@Test public void MPICH2Reduce() throws Exception{
		setup(2, 1);

		mb.getGlobalSettings().setMaxEagerSendSize(100 * KBYTE);
		mb.getGlobalSettings().setClientFunctionImplementation(
				new CommandType("Bcast"), "de.hd.pvs.piosim.simulator.program.Reduce.ReduceScatterGatherMPICH2");  //andere Implementation
		//CommandToSimulationMapper Eintrag, Standard letzter

		parameters.setTraceFile("/tmp/mpich2reduce");

		parameters.setTraceEnabled(true);

		pb.addReduce(world, 0, 10 * KBYTE);

		runSimulationAllExpectedToFinish();
	}



	@Test public void TestScatterHierachicalTwoLevels() throws Exception{
		setup(5, 1);

		mb.getGlobalSettings().setMaxEagerSendSize(100 * KBYTE);
		mb.getGlobalSettings().setClientFunctionImplementation(
				new CommandType("Scatter"), "de.hd.pvs.piosim.simulator.program.Scatter.ScatterHierachicalTwoLevels");  //andere Implementation
		//CommandToSimulationMapper Eintrag, Standard letzter

		parameters.setTraceFile("/tmp/scatter");

		parameters.setTraceEnabled(true);

		pb.addScatter(world, 0, 10 * KBYTE);

		runSimulationAllExpectedToFinish();
	}

	@Test public void TestScatterMPICH2() throws Exception{
		setup(2, 3);

		mb.getGlobalSettings().setMaxEagerSendSize(100 * KBYTE);
		mb.getGlobalSettings().setClientFunctionImplementation(	new CommandType("Scatter"), "de.hd.pvs.piosim.simulator.program.Scatter.ScatterMPICH2");

		parameters.setTraceFile("/tmp/scatter");

	//	parameters.setTraceInternals(true);
		parameters.setTraceEnabled(true);

		pb.addScatter(world, 0, 100* MBYTE);

		runSimulationAllExpectedToFinish();
	}

	@Test public void TestGatherDirect() throws Exception{
		setupSMP(5);

		mb.getGlobalSettings().setClientFunctionImplementation(	new CommandType("Gather"), "de.hd.pvs.piosim.simulator.program.Gather.Direct");

		parameters.setTraceFile("/tmp/gather");
		parameters.setTraceEnabled(true);

		pb.addGather(world, 0, 100* MBYTE);

		runSimulationAllExpectedToFinish();
	}


	@Test public void TestGatherWorld() throws Exception{
		setupSMP(15);


		mb.getGlobalSettings().setClientFunctionImplementation(	new CommandType("Gather"), "de.hd.pvs.piosim.simulator.program.Gather.GatherBinaryTreeMPICH2");

		parameters.setTraceFile("/tmp/gather");
		parameters.setTraceEnabled(true);

		pb.addGather(world, 0, 100* MBYTE);

		runSimulationAllExpectedToFinish();
	}


	@Test public void TestScatter3World() throws Exception{
		setupSMP(5);


		mb.getGlobalSettings().setClientFunctionImplementation(	new CommandType("Scatter"), "de.hd.pvs.piosim.simulator.program.Scatter.ScatterMPICH2");

		parameters.setTraceFile("/tmp/scatter");
		parameters.setTraceEnabled(true);

		pb.addScatter(world, 4, 100* MBYTE);

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

		pb.addScatter(comm, 1, 100* MBYTE);

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

		pb.addScatter(comm, 0, 100* MBYTE);

		runSimulationAllExpectedToFinish();
	}



	@Test public void TestScatterFCFS() throws Exception{
		setup(3, 1);

		mb.getGlobalSettings().setMaxEagerSendSize(100 * KBYTE);
		mb.getGlobalSettings().setClientFunctionImplementation(
				new CommandType("Scatter"), "de.hd.pvs.piosim.simulator.program.Scatter.FCFS");
		parameters.setTraceFile("/tmp/scatterFCFS");
		parameters.setTraceEnabled(true);
		pb.addScatter(world, 0, 10 * MBYTE);
		runSimulationAllExpectedToFinish();
	}

	@Test public void TestGatherFCFS() throws Exception{
		setup(3, 1);

		mb.getGlobalSettings().setMaxEagerSendSize(100 * KBYTE);
		mb.getGlobalSettings().setClientFunctionImplementation(
				new CommandType("Scatter"), "de.hd.pvs.piosim.simulator.program.Gather.FCFS");
		parameters.setTraceFile("/tmp/gatherFCFS");
		parameters.setTraceEnabled(true);
		pb.addGather(world, 0, 10 * MBYTE);
		runSimulationAllExpectedToFinish();
	}



	@Test public void TestScatterFCFSOrdered123() throws Exception{
			setup(4,1);

			parameters.setTraceFile("/tmp/scatter");
			parameters.setTraceEnabled(true);

			mb.getGlobalSettings().setMaxEagerSendSize(100 * KBYTE);
			mb.getGlobalSettings().setClientFunctionImplementation(
					new CommandType("Scatter"), "de.hd.pvs.piosim.simulator.program.Scatter.FCFS");

			pb.addCompute(1, 10000000);
			pb.addCompute(2, 20000000);
			pb.addCompute(3, 30000000);

			pb.addScatter(world, 0, 10 * MBYTE);
			runSimulationAllExpectedToFinish();
	}

	@Test public void TestScatterFCFSLate() throws Exception{
		setup(4,1);

		parameters.setTraceFile("/tmp/scatter");
		parameters.setTraceEnabled(true);

		mb.getGlobalSettings().setMaxEagerSendSize(100 * KBYTE);
		mb.getGlobalSettings().setClientFunctionImplementation(
				new CommandType("Scatter"), "de.hd.pvs.piosim.simulator.program.Scatter.FCFS");

		pb.addCompute(0, 10000000);

		pb.addScatter(world, 0, 10 * MBYTE);
		runSimulationAllExpectedToFinish();
}

	@Test public void TestScatterFCFSEqual() throws Exception{
		setup(4,1);

		parameters.setTraceFile("/tmp/scatter");
		parameters.setTraceEnabled(true);

		mb.getGlobalSettings().setMaxEagerSendSize(100 * KBYTE);
		mb.getGlobalSettings().setClientFunctionImplementation(
				new CommandType("Scatter"), "de.hd.pvs.piosim.simulator.program.Scatter.FCFS");

		pb.addScatter(world, 0, 10 * MBYTE);
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
