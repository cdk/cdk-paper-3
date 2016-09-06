#!/bin/bash

./measure_runtime.sh small_masses.txt 0.001
./measure_runtime.sh small_masses.txt 0.01

./measure_runtime.sh large_masses.txt 0.001
./measure_runtime.sh large_masses.txt 0.01

