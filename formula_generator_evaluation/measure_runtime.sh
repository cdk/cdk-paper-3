#!/bin/bash

MASS=$1
TOLERANCE=$2
MIN=0
MAX=10000
ELEMENTS=C${MIN}-${MAX}H${MIN}-${MAX}N${MIN}-${MAX}O${MIN}-${MAX}S${MIN}-${MAX}P${MIN}-${MAX}

echo Running CDK on mass ${MASS}+-${TOLERANCE}
time -p java -cp "CDK/*:CDK" CDKFormulaGeneratorCLI $MASS $TOLERANCE $ELEMENTS > outCDK.txt
echo

echo Running PFG on mass ${MASS}+-${TOLERANCE}
time -p PFG/PFG eval -m ${MASS} -u Da -t ${TOLERANCE} --C ${MIN}-${MAX} --H ${MIN}-${MAX} --N ${MIN}-${MAX} --O ${MIN}-${MAX} --S ${MIN}-${MAX} --P ${MIN}-${MAX} > outPFG.txt
echo

echo Running HR2 on mass ${MASS}+-${TOLERANCE}
time -p HR2/HR2 -m ${MASS} -t ${TOLERANCE} -C ${MIN}-${MAX} -H ${MIN}-${MAX} -N ${MIN}-${MAX} -O ${MIN}-${MAX} -S ${MIN}-${MAX} -P ${MIN}-${MAX} > outHR2.txt
echo

# Clean up results
cat outPFG.txt | cut -f 1 | grep -v formula | sort > outPFG.sorted
cat outCDK.txt | sort > outCDK.sorted
cat outHR2.txt | cut -f 1 | tail -n +6 | head -n -3 | sort > outHR2.sorted

echo Comparing number of results
wc -l *.sorted | grep -v total

