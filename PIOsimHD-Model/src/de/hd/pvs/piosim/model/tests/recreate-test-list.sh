#!/bin/bash
# Simple bash skript, updates test list in RunAllTests.java.
FILE=RunAllTests.java

TEXT=
for I in `grep @Test \`find -name '*.java'\`| cut -f 1 -d ":"|uniq|sed "s/\//./g"|sed "s/.java$/.class/"|sed "s/\.\././g" ` ; do
  	TEXT="de.hd.pvs.piosim.tests.regression$I,$TEXT"
done

sed -i "s/^[ \t]*de\.hd\.pvs.*$/$TEXT/" $FILE