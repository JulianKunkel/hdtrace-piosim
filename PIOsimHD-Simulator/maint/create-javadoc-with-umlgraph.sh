#!/bin/bash

mkdir javadoc
cd javadoc

CP="-classpath ../../lib/TAU_tf.jar:../../../PIOsimHD-Model/lib/junit-4.5.jar"

#create std. javadoc
#javadoc -d . -private $CP -sourcepath ../../../PIOsimHD-Simulator/src/:../../../PIOsimHD-Model/src/ -subpackages de.hd

# automatically generate javadoc:
javadoc -d "."  -doclet org.umlgraph.doclet.UmlGraphDoc -private $CP -docletpath ../umlgraph/UmlGraph.jar  -sourcepath ../../../PIOsimHD-Simulator/src/:../../../PIOsimHD-Model/src/ -subpackages de.hd -inferrel -inferdep -hide java.* -collpackages "java.util.*" -qualify -postfixpackage -nodefontsize 9 -nodefontpackagesize 7
