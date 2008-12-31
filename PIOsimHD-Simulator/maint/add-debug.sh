
#!/bin/bash
#remove debug lines.

for I in `grep -ri 'debug(' src/ | cut -d ":" -f1|uniq` ; do sed -i 's/^\/\/\(.*debug(.*[;+].*\)/\1/g' $I ; done
for I in `grep -ri 'debugFollowUpLine(' src/ | cut -d ":" -f1|uniq` ; do sed -i 's/^\/\/\(.*debugFollowUpLine(.*[;+].*\)/\1/g' $I ; done
