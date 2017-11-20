#!/bin/bash

set -e

RP=`realpath $0`
SDIR=`dirname $RP`

echo "Installation directory: $SDIR"

java -Xmx4g -Xms4g -classpath $SDIR/classes:$SDIR/jcommander-1.48.jar dsi.DSIEventAnalyzer $*
