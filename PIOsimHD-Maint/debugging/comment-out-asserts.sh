#!/bin/bash
#remove asserts
source `dirname $0`/../path.rc || exit 1

for FOLDER in $SIMF $MODELF ; do
cd $FOLDER

for I in `grep -ri 'assert(' src/ | cut -d ":" -f1|uniq` ;
do
sed -i 's/\(^[ \t]*[^ /]*assert(.*\)/\/\/\1/g' $I 
done

done