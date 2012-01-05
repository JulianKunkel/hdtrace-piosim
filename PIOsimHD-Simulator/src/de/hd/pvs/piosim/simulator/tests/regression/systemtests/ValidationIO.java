package de.hd.pvs.piosim.simulator.tests.regression.systemtests;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Random;

import org.junit.Test;

import de.hd.pvs.piosim.model.components.ServerCacheLayer.ServerCacheLayer;
import de.hd.pvs.piosim.model.inputOutput.FileDescriptor;
import de.hd.pvs.piosim.model.inputOutput.FileMetadata;
import de.hd.pvs.piosim.model.inputOutput.ListIO;
import de.hd.pvs.piosim.model.inputOutput.distribution.SimpleStripe;
import de.hd.pvs.piosim.simulator.SimulationResultSerializer;
import de.hd.pvs.piosim.simulator.tests.regression.systemtests.hardwareConfigurations.IOC;

public class ValidationIO extends Validation {

	long randomComputeCyclesMax = procSpeed / 100000; // speed of the proc max 10 ns.
	Random random = new Random(1);

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

	private void levelXOperation(boolean isWrite, int level, FileDescriptor fd, int clientProcesses, int repeats, long size, boolean addCompute){

		switch(level){
		case 0: {
			long pos = 0;

			// level 0
			for(long c=0; c < repeats; c++){

				for(int i=0 ; i < clientProcesses ; i++){
					if (isWrite){
						pb.addWriteSequential(i, fd, size * pos, size);
					}else{
						pb.addReadSequential(i, fd,  size * pos, size);
					}
					if(addCompute)
						addComputeOp(i);
					pos++;
				}
			}
			break;
		}
		case 1: {
			long pos = 0;
			for(long c=0; c < repeats; c++){

				LinkedList<ListIO> ios = new LinkedList<ListIO>();

				for(long i=0 ; i < clientProcesses ; i++){
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

				if(addCompute){
					for(int i=0 ; i < clientProcesses ; i++){
						addComputeOp(i);
					}
				}
			}

			break;
		}

		case 2: {
			//level2:
			for(int i=0 ; i < clientProcesses ; i++){
				ListIO listio = new ListIO();
				for(long c=0; c < repeats; c++){
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
			for(long i=0 ; i < clientProcesses ; i++){
				ListIO listio = new ListIO();
				for(long c=0; c < repeats; c++){
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

	void runMPIIOLevelValidationSingleSimple(int level, boolean write, int clients, int servers, ServerCacheLayer cacheLayer, int processes, int overlapping, int repeats, long size, long ramSize, boolean tracing, BufferedWriter modelTime) throws Exception{
		setupWrCluster(2, false , false, false, true, clients, processes,
				overlapping, servers, cacheLayer, ramSize);

		//model.getGlobalSettings().setTransferGranularity(10 * KiB);


		parameters.setTraceFile("/tmp/io-level" + level + (write ? "WRITE" : "READ"));
		parameters.setTraceEnabled(tracing);

		SimpleStripe dist = new SimpleStripe();
		dist.setChunkSize(64 * KiB);
		FileMetadata file =  aB.createFile("test", Long.MAX_VALUE, dist );

		FileDescriptor fd = pb.addFileOpen(file, world, false);

		if( write ){
			levelXOperation(true, level, fd, processes, repeats, size, false);
		}else{
			levelXOperation(false, level, fd, processes, repeats, size, false);
		}
		pb.addFileClose(fd);

		runSimulationAllExpectedToFinish();

		if (modelTime == null){
			modelTime = new BufferedWriter(new FileWriter("/tmp/mpi-iolevelUnnamed-modelTime.txt"));

			final SimulationResultSerializer serializer = new SimulationResultSerializer();
			modelTime.write(serializer.serializeResults(simRes).toString());
			modelTime.flush();

			modelTime.close();
		}else{
			final SimulationResultSerializer serializer = new SimulationResultSerializer();
			modelTime.write(serializer.serializeResults(simRes).toString());
			modelTime.flush();
		}
	}

	void runMPIIOLevelValidationSingleThroughput(int level, boolean write, int clients, int servers, ServerCacheLayer cacheLayer, int processes, int overlapping, int repeats, long size, long ramSize, boolean tracing, BufferedWriter modelTime) throws Exception{

		runMPIIOLevelValidationSingleSimple(level, write, clients, servers, cacheLayer, processes, overlapping, repeats, size, ramSize, tracing, null);
		double totalSizeInMiB = (double) (processes) * repeats * size / 1024.0 / 1024.0;
		double tp = totalSizeInMiB / simRes.getVirtualTime().getDouble();
		modelTime.write(" " + tp);
		modelTime.flush();
	}

	void startExperiment(String name, BufferedWriter modelTime) throws IOException{
		modelTime.write("\n" + name);
		modelTime.flush();
	}



	void runMPIIOLevelValidationSingle(int level, boolean write, String prefix, int clients, int servers, ServerCacheLayer cacheLayer, int processes, int overlapping, int repeats, long size, long ramSize, boolean tracing, BufferedWriter modelTime) throws Exception{
		runMPIIOLevelValidationSingleSimple(level, write, clients, servers, cacheLayer, processes, overlapping, repeats, size, ramSize, tracing, modelTime);

		String configStr = prefix + " N" + (clients + servers - overlapping) + "-P1-C" + clients + "-P" + processes + "-S" + servers + "-RAM" + ramSize + "-Size" + size + "-rep" + repeats + " " + (write ? "WRITE" : "READ") + "-lvl" + level;
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
		runMPIIOLevelValidationSingle(3, true, "tst ",  2, 2, IOC.AggregationReorderCache(), 2, 0, 10, 100*MiB, 1000, true, modelTime);
	}


	@Test public void TenGiGFileAccessTrace() throws Exception{
		BufferedWriter modelTime = new BufferedWriter(new FileWriter("/tmp/io-modelTime.txt"));
		//runMPIIOLevelValidationSingleThroughput(0, true, 5,5, IOC.SimpleWriteBehindCache(),	15, 0, 100, 100 * MiB, 10000, true, modelTime);
		runMPIIOLevelValidationSingleThroughput(0, true, 5,5, IOC.AggregationReorderCache(),	15, 0, 100, 100 * MiB, 1000, true, modelTime);

	}


	@Test public void TenGiGFileAccessTrace1() throws Exception{
		// Similar behavior to TenGiGFileAccessTrace
		BufferedWriter modelTime = new BufferedWriter(new FileWriter("/tmp/io-modelTime.txt"));
		runMPIIOLevelValidationSingleThroughput(0, true, 1, 1, IOC.AggregationReorderCache(),	3, 0, 100, 100 * MiB, 10000, true, modelTime);
	}

	@Test public void OneGiGFileAccessTrace1() throws Exception{
		// Similar behavior to TenGiGFileAccessTrace but with smaller amounts of data.
		BufferedWriter modelTime = new BufferedWriter(new FileWriter("/tmp/io-modelTime.txt"));
		runMPIIOLevelValidationSingleThroughput(0, true, 1, 1, IOC.AggregationReorderCache(),	3, 0, 1, 100 * MiB, 10000, true, modelTime);
	}


	@Test public void test3() throws Exception{
		setupWrCluster(2, false , false, false, true, 1, 1,	0, 1, IOC.AggregationReorderCache(), 100);

		parameters.setTraceFile("/tmp/dump");
		parameters.setTraceEnabled(true);
		parameters.setTraceInternals(true);

		SimpleStripe dist = new SimpleStripe();
		dist.setChunkSize(64 * KiB);
		FileMetadata file =  aB.createFile("test", 100 * GiB, dist );

		FileDescriptor fd = pb.addFileOpen(file, world, false);

		levelXOperation(true, 0, fd, 1, 1, 100*MiB, false);
		pb.addFileClose(fd);

		runSimulationAllExpectedToFinish();

		System.out.println((100*MiB/ simRes.getVirtualTime().getDouble()/1024/1024.0) + " MiB/s");
	}


	/**
	 * Comparision of measured parabench results for the different levels.
	 * @throws Exception
	 */
	@Test public void MPIIOLevelValidation() throws Exception{
		ServerCacheLayer cacheLayer = IOC.AggregationReorderCache();

		BufferedWriter modelTime = new BufferedWriter(new FileWriter("/tmp/io-modelTime.txt"));

		modelTime.write("Cache settings: " + cacheLayer.toString() + "\n");

		for(int level = 0; level < 4 ; level ++){
			for(int i = 0 ; i < 2; i++){
				boolean isWrite = i == 0 ? true : false;

				// test with 1000 MiB main memory
				startExperiment("1GiGRAM/100.txt " + level, modelTime);
				runMPIIOLevelValidationSingleThroughput(level, isWrite,	 1,1, cacheLayer,	1, 0, 10, 100 * MiB, 1000, false, modelTime);
				runMPIIOLevelValidationSingleThroughput(level, isWrite,	 2,2, cacheLayer,	2, 0, 10, 100 * MiB, 1000, false, modelTime);
				runMPIIOLevelValidationSingleThroughput(level, isWrite,	 3,2, cacheLayer,	3, 0, 10, 100 * MiB, 1000, false, modelTime);
				runMPIIOLevelValidationSingleThroughput(level, isWrite,	 3,3, cacheLayer,	3, 0, 10, 100 * MiB, 1000, false, modelTime);
				runMPIIOLevelValidationSingleThroughput(level, isWrite,	 4,4, cacheLayer,	4, 0, 10, 100 * MiB, 1000, false, modelTime);
				runMPIIOLevelValidationSingleThroughput(level, isWrite,	 5,5, cacheLayer,	5, 0, 10, 100 * MiB, 1000, false, modelTime);
			}
		}

		for(int level = 0; level < 4 ; level ++){
			for(int i = 0 ; i < 2; i++){
				boolean isWrite = i == 0 ? true : false;

				startExperiment("100KByteGranularity/100.txt " + level, modelTime);
				runMPIIOLevelValidationSingleThroughput(level, isWrite,	 1,1, cacheLayer,	1, 0, 10240, 100 * KiB, 1000, false, modelTime);
				runMPIIOLevelValidationSingleThroughput(level, isWrite,	 2,2, cacheLayer,	2, 0, 10240, 100 * KiB, 1000, false, modelTime);
				runMPIIOLevelValidationSingleThroughput(level, isWrite,  3,2, cacheLayer,	3, 0, 10240, 100 * KiB, 1000, false, modelTime);
				runMPIIOLevelValidationSingleThroughput(level, isWrite,  3,3, cacheLayer,	3, 0, 10240, 100 * KiB, 1000, false, modelTime);
				runMPIIOLevelValidationSingleThroughput(level, isWrite,	 4,4, cacheLayer,	4, 0, 10240, 100 * KiB, 1000, false, modelTime);
				runMPIIOLevelValidationSingleThroughput(level, isWrite,	 5,5, cacheLayer,	5, 0, 10240, 100 * KiB, 1000, false, modelTime);
			}
		}

		for(int level = 0; level < 4 ; level ++){
			for(int i = 0 ; i < 2; i++){
				boolean isWrite = i == 0 ? true : false;

				startExperiment("100KByteGranularity/10000RAM.txt " + level, modelTime);
				runMPIIOLevelValidationSingleThroughput(level, isWrite,	 1,1, cacheLayer,	1, 0, 10240, 100 * KiB, 10000, false, modelTime);
				runMPIIOLevelValidationSingleThroughput(level, isWrite,	 2,2, cacheLayer,	2, 0, 10240, 100 * KiB, 10000, false, modelTime);
				runMPIIOLevelValidationSingleThroughput(level, isWrite,  3,2, cacheLayer,	3, 0, 10240, 100 * KiB, 10000, false, modelTime);
				runMPIIOLevelValidationSingleThroughput(level, isWrite,  3,3, cacheLayer,	3, 0, 10240, 100 * KiB, 10000, false, modelTime);
				runMPIIOLevelValidationSingleThroughput(level, isWrite,	 4,4, cacheLayer,	4, 0, 10240, 100 * KiB, 10000, false, modelTime);
				runMPIIOLevelValidationSingleThroughput(level, isWrite,	 5,5, cacheLayer,	5, 0, 10240, 100 * KiB, 10000, false, modelTime);
			}
		}

		for(int level = 0; level < 4 ; level ++){
			for(int i = 0 ; i < 2; i++){
				boolean isWrite = i == 0 ? true : false;

				// test to run multiple p100rocesses on the client nodes
				startExperiment("1GiGRAM/100-multiple.txt " + level, modelTime);
				for(int p=1; p <= 6 ; p++){
					runMPIIOLevelValidationSingleThroughput(level, isWrite, 5,5, cacheLayer, p*5, 0, 10, 100 * MiB, 1000, false, modelTime);
				}
			}
		}

		for(int level = 0; level < 4 ; level ++){
			for(int i = 0 ; i < 2; i++){
				boolean isWrite = i == 0 ? true : false;

				startExperiment("100KByteGranularity/100-multiple.txt " + level, modelTime);
				for(int p=1; p <= 6 ; p++){
					runMPIIOLevelValidationSingleThroughput(level, isWrite, 5,5, cacheLayer,	p*5, 0, 10240, 100 * KiB, 1000, false, modelTime);
				}

			}
		}

		for(int level = 0; level < 4 ; level ++){
			for(int i = 0 ; i < 2; i++){
				boolean isWrite = i == 0 ? true : false;

				// overlapping test
				startExperiment("1GiGRAM/100-overlapping.txt " + level, modelTime);
				runMPIIOLevelValidationSingleThroughput(level, isWrite,	8,8,cacheLayer, 8, 8, 10, 100 * MiB, 2000, false, modelTime);
				startExperiment("100KByteGranularity/100-overlapping.txt " + level, modelTime);
				runMPIIOLevelValidationSingleThroughput(level, isWrite,	8,8,cacheLayer, 8, 8, 10240, 100 * KiB, 2000, false, modelTime);
			}
		}

		for(int level = 0; level < 4 ; level ++){
			for(int i = 0 ; i < 2; i++){
				boolean isWrite = i == 0 ? true : false;

				startExperiment("10GiGAccessed/100.txt " + level, modelTime);
				runMPIIOLevelValidationSingleThroughput(level, isWrite,	 1,1, cacheLayer,	1, 0, 100, 100 * MiB, 10000, false, modelTime);
				runMPIIOLevelValidationSingleThroughput(level, isWrite,	 2,2, cacheLayer,	2, 0, 100, 100 * MiB, 10000, false, modelTime);
				runMPIIOLevelValidationSingleThroughput(level, isWrite,	 3,2, cacheLayer,	3, 0, 100, 100 * MiB, 10000, false, modelTime);
				runMPIIOLevelValidationSingleThroughput(level, isWrite,	 3,3, cacheLayer,	3, 0, 100, 100 * MiB, 10000, false, modelTime);
				runMPIIOLevelValidationSingleThroughput(level, isWrite,	 4,4, cacheLayer,	4, 0, 100, 100 * MiB, 10000, false, modelTime);
				runMPIIOLevelValidationSingleThroughput(level, isWrite,	 5,5, cacheLayer,	5, 0, 100, 100 * MiB, 10000, false, modelTime);
			}
		}

		for(int level = 0; level < 4 ; level ++){
			for(int i = 0 ; i < 2; i++){
				boolean isWrite = i == 0 ? true : false;

				startExperiment("10GiGAccessed/10000RAM.txt " + level, modelTime);
				runMPIIOLevelValidationSingleThroughput(level, isWrite,	 1,1, cacheLayer,	1, 0, 100, 100 * MiB, 10000, false, modelTime);
				runMPIIOLevelValidationSingleThroughput(level, isWrite,	 2,2, cacheLayer,	2, 0, 100, 100 * MiB, 10000, false, modelTime);
				runMPIIOLevelValidationSingleThroughput(level, isWrite,	 3,2, cacheLayer,	3, 0, 100, 100 * MiB, 10000, false, modelTime);
				runMPIIOLevelValidationSingleThroughput(level, isWrite,	 3,3, cacheLayer,	3, 0, 100, 100 * MiB, 10000, false, modelTime);
				runMPIIOLevelValidationSingleThroughput(level, isWrite,	 4,4, cacheLayer,	4, 0, 100, 100 * MiB, 10000, false, modelTime);
				runMPIIOLevelValidationSingleThroughput(level, isWrite,	 5,5, cacheLayer,	5, 0, 100, 100 * MiB, 10000, false, modelTime);
			}
		}

		for(int level = 0; level < 4 ; level ++){
			for(int i = 0 ; i < 2; i++){
				boolean isWrite = i == 0 ? true : false;

				startExperiment("10GiGAccessed/100-multiple.txt " + level, modelTime);
				for(int p=1; p <= 6 ; p++){
					runMPIIOLevelValidationSingleThroughput(level, isWrite, 5,5, cacheLayer,	p*5, 0, 100, 100 * MiB, 10000, false, modelTime);
				}
			}
		}

		for(int level = 0; level < 4 ; level ++){
			for(int i = 0 ; i < 2; i++){
				boolean isWrite = i == 0 ? true : false;

				startExperiment("4-levels-of-access/400RAM.txt " + level, modelTime);
				runMPIIOLevelValidationSingleThroughput(level, isWrite,	 1,1, cacheLayer,	1, 0, 10, 100 * MiB, 400, false, modelTime);
				runMPIIOLevelValidationSingleThroughput(level, isWrite,	 2,2, cacheLayer,	2, 0, 10, 100 * MiB, 400, false, modelTime);
				runMPIIOLevelValidationSingleThroughput(level, isWrite,	 3,2, cacheLayer,	3, 0, 10, 100 * MiB, 400, false, modelTime);
				runMPIIOLevelValidationSingleThroughput(level, isWrite,	 3,3, cacheLayer,	3, 0, 10, 100 * MiB, 400, false, modelTime);
				runMPIIOLevelValidationSingleThroughput(level, isWrite,	 4,4, cacheLayer,	4, 0, 10, 100 * MiB, 400, false, modelTime);
				runMPIIOLevelValidationSingleThroughput(level, isWrite,	 5,5, cacheLayer,	5, 0, 10, 100 * MiB, 400, false, modelTime);
			}
		}


		modelTime.write("\n\n Settings: " + mb.getGlobalSettings().toString());

		modelTime.close();

		System.out.println("Completed");
	}

	@Test public void myTestTrace() throws Exception{
		ServerCacheLayer cacheLayer = IOC.AggregationCache();
		//runMPIIOLevelValidation("1000 ", 3 , 2,cacheLayer,3, 0,10, 104857600, 1000, true, null);

		//runMPIIOLevelValidation("1000 ", 2 ,2, cacheLayer, 2, 0, 10, 104857600, 1000, true, null);
		//runMPIIOLevelValidation("overlapping ", 8,8,cacheLayer, 8, 8, 10, 100 * MiB, 2000, true, null);

		runMPIIOLevelValidationSingle(2, true, "10000MB ", 2, 1, IOC.SimpleWriteBehindCache(), 2, 0, 13000, 50*KiB, 10000,false, null);
	}


}
