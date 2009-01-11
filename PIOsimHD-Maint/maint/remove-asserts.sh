#!/bin/bash
#remove asserts

for I in `grep -ri 'assert(' src/ | cut -d ":" -f1|uniq` ;
do
sed -i 's/\(^[ \t]*[^ ]*assert(.*\)/\/\/\1/g' $I 
done
