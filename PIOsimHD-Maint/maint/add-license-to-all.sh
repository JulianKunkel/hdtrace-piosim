#!/bin/bash

source `dirname $0`/../path.rc || exit 1

cd `dirname $0`

for FOLDER in $SIMF $MODELF $MAINTF/TraceConverterHD $TRACEF; do
	./add-license.sh $FOLDER/src
done
  
