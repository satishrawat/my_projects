#!/bin/bash

...

if [ "$code" = "200" ]; then
  echo "Website $url is online."
else
  echo "Website $url seems to be offline."
fi
