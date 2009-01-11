#!/bin/bash

# see http://www.umlgraph.org/doc/index.html
source `dirname $0`/../path.rc || exit 1

cd `dirname $0`/../
mkdir diagrams

javadoc javadoc  -doclet org.umlgraph.doclet.UmlGraph -private -docletpath $MAINTF/maint/umlgraph/UmlGraph.jar  -sourcepath $SIMF/src/:$MODELF/src/:umlgraph/views/ -views -subpackages  de.hd

for DOT in `ls *.dot | cut -d "." -f 1`
do
echo "Processing $DOT"
dot -Tpng -o $DOT.png $DOT.dot 1>/tmp/out-$DOT 2>&1|grep -v ignor
done

rm *.dot


#                        <param name="-link" value="http://java.sun.com/j2se/1.5.0/docs/guide/javadoc/doclet/spec"/>
#                      <param name="-link" value="http://java.sun.com/j2se/1.5/docs/api"/>

