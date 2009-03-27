#!/bin/bash

### ADD SVN Keywords to all JAVA files.

source `dirname $0`/../path.rc || exit 1

cd $PFAD

for FILE in ` find -name *.java` ; do
	if grep "Version Control Information" $FILE >/dev/null; then
        	continue; // KEYWORDS already added
	fi
	echo "Adding keywords to $FILE"	

(
echo '
 /** Version Control Information $Id$
  * @lastmodified    $Date$
  * @modifiedby      $LastChangedBy$
  * @version         $Revision$ 
  */
'
cat $FILE
) > /dev/shm/tmp
mv /dev/shm/tmp "$FILE"

done
