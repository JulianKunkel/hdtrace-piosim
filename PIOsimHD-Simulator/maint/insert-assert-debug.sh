
#!/bin/bash
#insert asserts around debug

for I in `grep -ri 'debug(' src/ | cut -d ":" -f1|uniq` ; do sed -i 's/\(^[ \t]*\)\([^ ]*debug(.*\);/\1assert(\2);/g' $I ; done
for I in `grep -ri 'debugFollowUpLine(' src/ | cut -d ":" -f1|uniq` ; do sed -i 's/\(^[ \t]\)\(*[^ ]*debugFollowUpLine(.*\);/\1assert(\2);/g' $I ; done
