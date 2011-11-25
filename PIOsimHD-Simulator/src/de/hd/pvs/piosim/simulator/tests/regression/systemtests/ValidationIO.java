package de.hd.pvs.piosim.simulator.tests.regression.systemtests;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.LinkedList;
import java.util.Random;

import org.junit.Test;

import de.hd.pvs.piosim.model.components.ServerCacheLayer.ServerCacheLayer;
import de.hd.pvs.piosim.model.inputOutput.FileDescriptor;
import de.hd.pvs.piosim.model.inputOutput.FileMetadata;
import de.hd.pvs.piosim.model.inputOutput.ListIO;
import de.hd.pvs.piosim.model.inputOutput.distribution.SimpleStripe;
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
			int pos = 0;

			// level 0
			for(int c=0; c < repeats; c++){

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
			levelXOperation(true, level, fd, processes, repeats, size, false);
		}else{
			levelXOperation(false, level, fd, processes, repeats, size, false);
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
		ServerCacheLayer cacheLayer = IOC.AggregationReorderCache();

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


}
