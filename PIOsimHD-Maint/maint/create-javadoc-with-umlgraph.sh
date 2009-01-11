#!/bin/bash
source `dirname $0`/../path.rc || exit 1

cd `dirname $0`/../

mkdir javadoc
cd javadoc

CP="-classpath $SIMF/lib/TAU_tf.jar:$MODELF/lib/junit-4.5.jar"

#create std. javadoc
#javadoc -d . -private $CP -sourcepath ../../../PIOsimHD-Simulator/src/:../../../PIOsimHD-Model/src/ -subpackages de.hd

# automatically generate javadoc:
javadoc -d "."  -doclet org.umlgraph.doclet.UmlGraphDoc -private $CP -docletpath $MAINTF/maint/umlgraph/UmlGraph.jar  -sourcepath $SIMF/src/:$MODELF/src/ -subpackages de.hd -inferrel -inferdep -hide java.* -collpackages "java.util.*" -qualify -postfixpackage -nodefontsize 9 -nodefontpackagesize 7 2>&1 | egrep -iC10 "error"
