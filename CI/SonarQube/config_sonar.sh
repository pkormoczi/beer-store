#!/bin/bash
./bin/run.sh &

BASE_URL=http://127.0.0.1:9000

function isUp {
  curl -s -u admin:admin -f "$BASE_URL/api/system/info"
}

# Wait for server to be up
PING=`isUp`
while [[ -z "$PING" ]]
do
  sleep 5
  PING=`isUp`
done

# Create webhook for jenkins
curl -v -u admin:admin "$BASE_URL/api/webhooks/create" -X POST -d "name=Jenkins&url=http://172.17.0.3:8080/sonarqube-webhook/"
wait