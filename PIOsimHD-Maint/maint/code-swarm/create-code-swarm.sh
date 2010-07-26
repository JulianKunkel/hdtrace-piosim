echo download via "git clone git://github.com/rictic/code_swarm.git"

pushd SVN
git log --pretty=format:'%n------------------------------------------------------------------------%nr%h | %an | %ai (%aD) | x lines%nChanged paths:' --name-status > /tmp/log

popd

rm frames/*
mkdir frames

pushd code_swarm
cd bin
 ./convert_logs.py -g /tmp/log -o ../../activity.xml
cd ..
 java -Xmx1000m -classpath "dist/code_swarm.jar:lib/gluegen-rt.jar:lib/jogl.jar:lib/jogl-natives-macosx-universal.jar:lib/core.jar:lib/opengl.jar:lib/xml.jar:lib/vecmath.jar:." -Djava.library.path=lib/ code_swarm ../julian.config
popd

ffmpeg -r 12 -i ./frames/code_swarm-%05d.png -sameq ./out.avi -pass 2
