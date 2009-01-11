#!/bin/bash
#insert asserts around debug
source `dirname $0`/../path.rc || exit 1

for FOLDER in $SIMF $MODELF ; do
cd $FOLDER

for I in `grep -ri 'debug(' src/ | cut -d ":" -f1|uniq` ; do sed -i 's/\(^[ \t]*\)\([^ ]*debug(.*\);/\1assert(\2);/g' $I ; done
for I in `grep -ri 'debugFollowUpLine(' src/ | cut -d ":" -f1|uniq` ; do sed -i 's/\(^[ \t]\)\(*[^ ]*debugFollowUpLine(.*\);/\1assert(\2);/g' $I ; done

done