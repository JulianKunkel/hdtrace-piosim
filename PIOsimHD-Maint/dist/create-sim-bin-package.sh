#!/bin/bash

# This script generates a distribution of the simulator.

source `dirname $0`/../path.rc || exit 1

cd `dirname $0`

VERSION=$(head -n 1 $MAINTF/dist/roottxt/VERSION)
TARGET=$MAINTF/PIOsimHD-Bin-$VERSION

SIMF_LIBFILES="CommandToSimulationMapper.txt  ModelToSimulationMapper.txt"

ROOTFILES_LIB=
ROOTFILES_DIST="" 

function JAR(){
	TGT="$1.jar"
	MF="$MAINTF/dist/$2"
	SRC="$3/bin/"

	jar -cfm $TARGET/lib/$TGT $MF  -C  $SRC de $4 $5 $6 $7
}

echo "preparing folders"

mkdir -p $TARGET/lib
mkdir -p $TARGET/bin 

cp -a "$MAINTF"/dist/bin/* $TARGET/bin
cp -r $MODELF/lib/* $TARGET/lib
cp -r $SIMF/lib/* $TARGET/lib
cp -r "$MAINTF"/jumpshot/lib/* $TARGET/lib/

echo "Copy basic files"
cp -a $MAINTF/dist/roottxt/* $TARGET/

for F in $SIMF_LIBFILES ; do
	cp $SIMF/$F $TARGET/lib/
done

for F in $ROOTFILES_LIB ; do
	cp $MODELF/$F $TARGET/lib/
done

for F in $ROOTFILES_DIST ; do
	cp $MODELF/$F $TARGET/
done

echo "Creating model package"
JAR piosimhd-model     model.mf       $MODELF  #"-C ../ images"

echo "Creating simulator package" 
JAR piosimhd-simulator simulator.mf  $SIMF # "-C ../ lib"

cp -a "../javadoc" $TARGET/

echo "Removing SVN entries"
for I in `find $TARGET|grep "/.svn$"`; do
	rm -rf $I
done

echo "Packing archive"
cd ../
tar -czf "$TARGET.tgz" $(basename $TARGET) || exit 1

echo "Complete"
