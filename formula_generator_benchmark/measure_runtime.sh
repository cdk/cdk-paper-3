#!/bin/bash

MASS_FILE=$1
TOLERANCE=$2
MIN=0
MAX=10000

echo Running CDK on file $MASS_FILE +-${TOLERANCE} Da
time -p java -cp "CDK/*:CDK" CDKFormulaGeneratorCLI $MASS_FILE $TOLERANCE C${MIN}-${MAX}H${MIN}-${MAX}N${MIN}-${MAX}O${MIN}-${MAX}S${MIN}-${MAX}P${MIN}-${MAX} > outCDK.txt
echo

echo Running PFG on file $MASS_FILE +-${TOLERANCE} Da
time -p while read MASS ; do PFG/PFG eval -m ${MASS} -u Da -t ${TOLERANCE} --C ${MIN}-${MAX} --H ${MIN}-${MAX} --N ${MIN}-${MAX} --O ${MIN}-${MAX} --S ${MIN}-${MAX} --P ${MIN}-${MAX} ; done < $MASS_FILE > outPFG.txt
echo

echo Running HR2 on file $MASS_FILE +-${TOLERANCE} Da
time -p while read MASS ; do HR2/HR2 -m ${MASS} -t ${TOLERANCE} -C ${MIN}-${MAX} -H ${MIN}-${MAX} -N ${MIN}-${MAX} -O ${MIN}-${MAX} -S ${MIN}-${MAX} -P ${MIN}-${MAX} ; done < $MASS_FILE > outHR2.txt
echo

# Clean up results
cat outPFG.txt | cut -f 1 | grep -v formula | sort > outPFG.sorted
cat outCDK.txt | sort > outCDK.sorted
cat outHR2.txt | cut -f 1 | sort > outHR2.sorted

echo Comparing the numbers of results
wc -l *.sorted | grep -v total

