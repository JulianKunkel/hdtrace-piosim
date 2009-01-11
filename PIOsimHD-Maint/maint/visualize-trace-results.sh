#!/bin/bash

# This file creates a slog2 tracefile from the simulation result and 
# visualizes it with jumpshot.

source `dirname $0`/../path.rc || exit 1

cd $SIMF

rm -rf tmp
mkdir tmp 2>&1

rm result.slog2


# adapt result file:
#(
#sed 's/\"\([^ ]*\) \([^ ]*\)\(.*\)\"/"\1\2\3"/g' run-trace.edf | sed 's/\"\([^ ]*\) \([^ ]*\)\(.*\)\"/"\1\2\3"/g'  | sed 's/\([^ ]*\) \([^ ]*\) 
#\([^ ]*\) "\([^"]*\)" \([^ ]*\)/\1 \4 \3 "\2" \5/'
#) >  run-trace.edf.swapped
#./jumpshot/bin/tau2slog2 -tcc run-trace.trc run-trace.edf.swapped -o result.slog2 2>&1 || exit 1

$MAINTF/jumpshot/bin/tau2slog2 -tcc run-trace.trc run-trace.edf -o result.slog2 2>&1 || exit 1
mv run-trace* tmp/


FILE="$1"


if [[ "$FILE" == "" ]] ; then
	FILE="./tmp/result.slog2"
fi

mv result.slog2 "$FILE"

$MAINTF/jumpshot/bin/jumpshot "$FILE"

