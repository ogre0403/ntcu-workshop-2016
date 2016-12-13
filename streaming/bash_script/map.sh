#! /bin/bash
while read LINE; do
  for word in $LINE
  do
    echo -e "$word\t1"
  done
done

