#!/bin/bash

...

if [ "$code" = "HTTP/1.1 200 OK" ]; then
  echo "Website $url is online."
else
  echo "Website $url seems to be offline."
fi
