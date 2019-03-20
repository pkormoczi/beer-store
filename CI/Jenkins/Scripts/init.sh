#!/bin/bash

echo "Generate Sonar authentication token"
source /usr/share/jenkins/generate_sonar_auth_token.sh

echo "start JENKINS"

chown -R 1000:1000 /var/jenkins_home
su jenkins -c /usr/local/bin/jenkins.sh