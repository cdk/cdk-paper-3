# Systematic CDK Benchmark

This benchmark compares CDK v1.4.19 to v2.0 over a set of common tasks. Each 
task can be run with input from either an SDfile or SMILES.

## Prerequisites

 * ``java`` Java v1.7+
 * ``mvn`` Maven build tool
 * ``curl`` to download data sets
 * ``perl`` (to extract ChEBI SMILES)

## The ``cdk`` Executable

A ``cdk`` executable is provided for both v1.4.19 and v2.0. To build the executable:

```
$ cd cdk-2.0
$ make
```

You should now be able to run the command

```
$ ./cdk
```

## Runnable Modules

There are several tasks that can be run:

- ``./cdk countheavy`` counts heavy atoms in a record
- ``./cdk rings`` reports ring info, requires one of the following
  - ``.cdk rings --mark`` report number of ring bonds
  - ``.cdk rings --sssr`` report number of SSSR rings (circuit rank)
  - ``.cdk rings --all`` report number of rings (all) size ≤ 12
- ``./cdk cansmi`` create a unique SMILES
- ``./cdk fpgen`` generate fingerprints in [chemfp FPS1 format](https://www.ncbi.nlm.nih.gov/pmc/articles/PMC3606241/)
	- ``./cdk fpgen -type=maccs`` generate CDK's MACCS 166 keys (MDL)
	- ``./cdk fpgen -type=path`` generate path fingerprint (Daylight)
	- ``./cdk fpgen -type=circ`` generate circular fingerprint (ECFP4)
- ``./cdk convert`` convert one format to another (or to it's self)
  - ``./cdk -ofmt=smi`` convert to non-canonical SMILES
  - ``./cdk -ofmt=sdf`` convert to SDfile
  - ``./cdk -gen2d -ofmt=sdf`` convert to SDfile and generate 2D coordinates

### General Options

The input format is detected from the file extension, or if specified the ``-ifmt`` option overrides this. Processing STDIN requires the ``-ifmt`` is specified:

```
$ echo "CCO" | ./cdk cansmi -ifmt=smi
```

The record title is taken from the title field in an SDfile Ctab records. Some databases do not honour this and instead use a non-structural data field to 
store the title of the record. In these situations the the ``--title-tag`` option specifies where to get the title from:

``` 
$ ./cdk countheavy ../data/chebi_149.sdf  | head -n 5
21
11
10
21
10
$ ./cdk countheavy ../data/chebi_149.sdf -title-tag='ChEBI ID' | head -n 5
21 CHEBI:90
11 CHEBI:165
10 CHEBI:598
21 CHEBI:776
10 CHEBI:943
```

The output location is specified by ``-o`` or ``--out``. Only the ``convert`` module uses the name of the output to determine the format. As with the ``-ifmt`` option the ``-ofmt`` for this module overrides the output type.

```
$ echo "CCO" | ./cdk convert -ifmt=smi -out=ethanol.sdf
$ echo "CCO" | ./cdk convert -ifmt=smi -ofmt=sdf
```

You can display the progress with the ``-p`` or ``--progress`` option:

```
$ ./cdk cansmi ../data/chembl_22_1.smi -o chembl_22_1.csmi -p
[INFO] 6.5% [==                       ] 111640 (19445 per second)
```

## Data Sets

The ``data/`` directory includes a make file to download and create SDF and SMI
input for two data sets **ChEBI 149** and **ChEMBL 22.1**. In the directory
a Makefile generates all input for the benchmark.

```
$ cd data
$ make
```

If you just want to make one of the data sets:

```
$ make chembl_22_1.smi
```

## Running the Benchmark

To run all the tasks in the benchmark the utility script ``run-benchmark`` is
provided. You can modify the script to only test one (or a different) dataset.
The script generates a ``results/`` directory that can be inspected

```
# ensure everything is built
$ cd data
$ make
$ cd ../cdk-2.0
$ make

$ nohup ../util/run-benchmark &
```

The ``tabulate.pl`` perl script is provided to summarize the results:

```
$ perl ../util/tabulate.pl results/*.err | sort
cansmi  	sdf	chebi_149	26	0m10.87s
cansmi  	smi	chebi_149	9	0m3.96s
convert -ofmt sdf  	smi	chebi_149	9	0m17.97s
convert -ofmt smi  	sdf	chebi_149	29	0m10.95s
convert -ofmt smi  	smi	chebi_149	11	0m3.32s
countheavy  	sdf	chebi_149	25	0m6.52s
countheavy  	smi	chebi_149	9	0m1.89s
rings -all  	sdf	chebi_149	40	0m8.10s
rings -all  	smi	chebi_149	26	0m2.79s
rings -mark  	sdf	chebi_149	25	0m7.33s
rings -mark  	smi	chebi_149	9	0m2.60s
rings -sssr  	sdf	chebi_149	25	0m8.32s
rings -sssr  	smi	chebi_149	9	0m3.13s
```
