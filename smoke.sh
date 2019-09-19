sleep 60
for _ in $(seq 10); do
  curl -fs $url > /dev/null
  if [ "$?" = "0" ]; then
    echo 'smoke test passed'
  else
    kubectl rollout history deployment/test -n $environment
  fi
done
