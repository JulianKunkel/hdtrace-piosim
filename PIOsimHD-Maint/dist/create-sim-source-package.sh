#!/bin/bash
source `dirname $0`/../path.rc || exit 1

cd `dirname $0`/../

if [[ ! -d "javadoc" ]] ; then
	echo "Please create first javadoc!"
	exit 1
fi

VERSION=$(head -n 1 $MAINTF/dist/roottxt/VERSION)
TARGET=$MAINTF/PIOsimHD-Source-$VERSION

mkdir $TARGET || rm -rf $TARGET/*

echo "Copy basic files"
cp -a $MAINTF/dist/roottxt/* $TARGET/

echo "Copy javadoc"

cp -a javadoc $TARGET/$(basename $DOCF) || exit 1

echo "Copy everything in SVN"
for I in $MODELF $SIMF $DOCF $MAINTF  ; do
 	mkdir $TARGET/$(basename $I)/

	for OBJ in $(svn ls -r HEAD  $I) ; do
	cp -a $I/$OBJ $TARGET/$(basename $I)/ || exit 1
    done 
done

echo "Removing SVN entries"
for I in `find $TARGET|grep "/.svn$"`; do
        rm -rf $I
done

echo "Packing archive"
tar -czf $TARGET.tgz $(basename $TARGET) || exit 1

echo Complete
