sleep 30
for _ in $(seq 1); do
  curl -fs $url > /dev/null
  if [ "$?" = "0" ]; then
    echo 'smoke test passed'
    
    
  else
    echo 'its failed performing rollback of deployment'
    kubectl rollout undo deployment/test -n $environment
    error('Aborting the build.')
  fi
done
