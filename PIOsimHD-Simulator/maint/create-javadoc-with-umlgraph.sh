#!/bin/bash

mkdir javadoc
cd javadoc
# automatically generate javadoc:
javadoc javadoc  -doclet org.umlgraph.doclet.UmlGraphDoc -private -docletpath ../umlgraph/UmlGraph.jar  -sourcepath ../../../PIOsimHD-Simulator/src/:../../../PIOsimHD-Model/src/ -subpackages de.hd -inferrel -inferdep -hide java.* -collpackages "java.util.*" -qualify -postfixpackage -nodefontsize 9 -nodefontpackagesize 7
