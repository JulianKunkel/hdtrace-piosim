#!/bin/bash

DIRME="$PWD"

FOLDER="$1"

if [ "$FOLDER" == "" ] ; then
	echo "Syntax $0 <SRC FOLDER> <LICENSE>"
	exit 1
fi

LICENSE="$DIRME/${2-license.txt}"

if [[ ! -e $LICENSE ]] ; then
	echo License file does not exist: $LICENSE
	exit 1;
fi

# this script adds a license to all java files

if [ -e  /dev/shm/tmp ] ; then
	echo "remove  /dev/shm/tmp and restart"
	exit 1
fi

source `dirname $0`/../path.rc || exit 1

cd $FOLDER

for FILE in `find -name "*.java"`; do

if grep "This file is part of" $FILE >/dev/null; then
	continue; // license already added
fi

(
cat $LICENSE
echo ""
cat $FILE
) > /dev/shm/tmp
sed  "s/author.*julian/author Julian M. Kunkel/" /dev/shm/tmp > $FILE

echo "processed: $FILE"

done
  
rm /dev/shm/tmp 2>/dev/null || echo "No file processed!"
