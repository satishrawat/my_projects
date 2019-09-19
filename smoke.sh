for _ in $(seq 10); do
  curl -fs http://35.239.92.56:80 > /dev/null
  if [ "$?" = "0" ]; then
    echo 'smoke test passed'
  else
    kubectl rollout history deployment/test -n ${environment}
  fi
done
