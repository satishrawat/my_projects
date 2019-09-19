#!/bin/bash
code=$(curl -Is http://35.239.92.56:80 | head -n 1)
echo 'this is my code' $code
if [ "$code" = "HTTP/1.1 200 OK" ]; then
  echo "Website $url is online."
else
  echo "Website $url seems to be offline."
fi
