#!/bin/bash
source `dirname $0`/../path.rc || exit 1

cd `dirname $0`/../

if [[ ! -d "javadoc" ]] ; then
	echo "Please create first javadoc!"
	exit 1
fi

TARGET=$MAINTF/PIOsimHD-Source

mkdir -p $TARGET || rm -rf $TARGET/*

echo "Copy javadoc"

cp -a javadoc $TARGET/$(basename $DOCF) || exit 1

echo "Copy everything in SVN"
for I in $MODELF $SIMF $DOCF $MAINTF  ; do
 	mkdir $TARGET/$(basename $I)/

	for OBJ in $(svn ls $I) ; do
	cp -a $I/$OBJ $TARGET/$(basename $I)/ || exit 1
    done 
done

echo "Removing SVN entries"
for I in `find $TARGET|grep "/.svn$"`; do
        rm -rf $I
	echo $I
done

echo "Packing archive"
tar -czf $TARGET.tgz $(basename $TARGET) || exit 1

echo Complete
