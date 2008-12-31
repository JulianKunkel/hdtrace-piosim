for I in `seq 1 9 `; do 
for A in "" "-lat"; do
cd $I$A; 
pwd

rm function.data

/media/home/julianlokal/workspace/PIOsim/mpiwrapper/unifyTraces.py -i trace-desc-partdiff-par.xml -o result.xml || exit 1
rm trace-*

cd .. 
done

done 
