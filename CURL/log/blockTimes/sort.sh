#!/bin/bash
cat blockTimes.txt | sort -n > b.txt
mv b.txt blockTimes.txt
