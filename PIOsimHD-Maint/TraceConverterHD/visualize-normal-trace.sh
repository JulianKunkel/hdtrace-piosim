#!/bin/bash

# This file creates a slog2 tracefile from the simulation result and 
# visualizes it with jumpshot.

source `dirname $0`/../path.rc || exit 1

$MAINTF/jumpshot/bin/tau2slog2 -tcc $1.trc $1.edf -o result.slog2 2>&1 || exit 1

FILE="$1"

mv result.slog2 "$FILE" || exit 1

./ProcessToGradient -g ".*:.*" "$FILE" || exit 1

$MAINTF/jumpshot/bin/jumpshot "$FILE.slog2" || exit 1

