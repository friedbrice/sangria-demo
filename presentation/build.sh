#!/bin/bash

set -e
NAME=$1
HERE=$(basename `pwd`)
which pandoc || { echo "could not find pandoc, exiting"; exit 1; }
echo "building pdf from latex source"
pandoc -t beamer -o presentation.pdf --template ./custom.beamer presentation.md
echo "pdf written to ${HERE}/presentation.pdf"
