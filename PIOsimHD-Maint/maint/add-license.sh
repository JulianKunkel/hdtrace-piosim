#!/bin/bash

FOLDER="$1"

if [ "$FOLDER" == "" ] ; then
	echo "Syntax $0 <SRC FOLDER>"
	exit 1
fi

# this script adds a license to all java files

if [ -e  /dev/shm/tmp ] ; then
	echo "remove  /dev/shm/tmp and restart"
	exit 1
fi

source `dirname $0`/../path.rc || exit 1

cd $FOLDER

for FILE in `find -name "*.java"`; do

if grep "This file is part of PIOsimHD" $FILE >/dev/null; then
	continue; // license already added
fi

(
echo "
//	Copyright (C) 2008, 2009 Julian M. Kunkel
//	
//	This file is part of PIOsimHD.
//	
//	PIOsimHD is free software: you can redistribute it and/or modify
//	it under the terms of the GNU General Public License as published by
//	the Free Software Foundation, either version 3 of the License, or
//	(at your option) any later version.
//	
//	PIOsimHD is distributed in the hope that it will be useful,
//	but WITHOUT ANY WARRANTY; without even the implied warranty of
//	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//	GNU General Public License for more details.
//	
//	You should have received a copy of the GNU General Public License
//	along with PIOsimHD.  If not, see <http://www.gnu.org/licenses/>.
"
cat $FILE
) > /dev/shm/tmp
sed  "s/julian/Julian M. Kunkel/" /dev/shm/tmp > $FILE

echo "processed: $FILE"

done
  
rm /dev/shm/tmp 2>/dev/null || echo "No file processed!"
