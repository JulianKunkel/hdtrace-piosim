#!/bin/bash

# This file creates a slog2 tracefile from the simulation result and 
# visualizes it with jumpshot.


./bin/trace-converter-hd -i $1 -o /tmp/test


./bin/tau2slog2 -tcc /tmp/test.trc /tmp/test.edf -o /tmp/result.slog2 2>&1 || exit 1

./bin/ProcessToGradient -g ".*:.*" "/tmp/result.slog2" || exit 1

./bin/jumpshot "/tmp/result.slog2.slog2" || exit 1

