#!/bin/bash
CP=bin:../piosim-shared/bin/:lib/:../piosim-shared/lib/log4j-1.2.15.jar:lib/TAU_tf.jar
java -cp $CP de.hd.pvs.piosim.simulator.CommandLineInterface  $@
