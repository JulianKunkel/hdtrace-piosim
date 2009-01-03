#!/bin/bash

# see http://www.umlgraph.org/doc/index.html

cd `dirname $0`

javadoc javadoc  -doclet org.umlgraph.doclet.UmlGraph -private -docletpath ./umlgraph/UmlGraph.jar  -sourcepath /media/home/julianlokal/workspace/PIOsimHD-Simulator/src/:/media/home/julianlokal/workspace/PIOsimHD-Model/src/:umlgraph/views/ -views -subpackages  de.hd

for DOT in `ls *.dot | cut -d "." -f 1`
do
echo "Processing $DOT"
dot -Tpng -o $DOT.png $DOT.dot 1>/tmp/out-$DOT 2>&1|grep -v ignor
done

rm *.dot
