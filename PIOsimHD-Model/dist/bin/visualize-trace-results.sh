# This file creates a slog2 tracefile from the simulation result and 
# visualizes it with jumpshot.

./jumpshot/bin/tau2slog2 -tcc run-trace.trc run-trace.edf -o result.slog2 2>&1

FILE="$1"


if [[ "$FILE" == "" ]] ; then
	FILE="result.slog2"
else
	mv result.slog2 "$FILE"
fi

./jumpshot/bin/jumpshot "$FILE"

