SUM=0 
for I in `grep Comput ~/workspace/PIOsim-CVS/Examples/Jacobi-2C-long-application.xml|cut -d "\"" -f 2` 
	do 
	SUM=$(($SUM + $I))  
done 
echo $SUM  / 1000 / 1000 | bc -l
