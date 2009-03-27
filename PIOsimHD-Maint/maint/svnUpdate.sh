#!/bin/bash

# this script updates files in SVN (for manual checkout) and adds keywords.

svn status | grep "^\?" |grep -v "/bin$" |grep -v "/Debug$" | cut -b 3- | xargs svn add
svn status | grep "^\!" | cut -b 3- | xargs svn remove

svn propset svn:keywords "Id Date LastChangedBy Revision" ` find -name "*.java"`
