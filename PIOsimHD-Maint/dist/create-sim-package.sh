#!/bin/bash

# This script generates a distribution of the simulator.

SIMF=../../PIOsim

TARGET=final

SIMF_LIBFILES="CommandToSimulationMapper.txt  ModelToSimulationMapper.txt"

ROOTFILES_LIB=
ROOTFILES_DIST="README.txt TODO.txt COPYING Changelog" 

function JAR(){
	TGT="$1.jar"
	MF="../dist/$2"
	SRC="$3/bin/"

	jar -cfm $TARGET/lib/$TGT $MF  -C  $SRC de $4 $5 $6 $7
}

echo "preparing folders"

mkdir -p $TARGET/lib
mkdir -p $TARGET/bin 

cp -r bin/* $TARGET/bin
cp -r ../lib/* $TARGET/lib
cp -r $SIMF/lib/* $TARGET/lib
cp -r $SIMF/jumpshot/lib/* $TARGET/lib/

chmod 755 $TARGET/bin/*

for F in $SIMF_LIBFILES ; do
	cp $SIMF/$F $TARGET/lib/
done

for F in $ROOTFILES_LIB ; do
	cp ../$F $TARGET/lib/
done

for F in $ROOTFILES_DIST ; do
	cp ../$F $TARGET/
done

echo "Creating model package"
JAR piosimhd-model     model.mf       ../  "-C ../ images"

echo "Creating simulator package" 
JAR piosimhd-simulator simulator.mf  $SIMF # "-C ../ lib" 