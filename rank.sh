#!/usr/bin/env sh
# ./rank.sh <queryDocTrainData path> taskType
java -Xmx1024m -cp bin/ edu.stanford.cs276.Rank $1 $2
