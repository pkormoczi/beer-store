## Commands
start:
`docker-compose up`

force recreate and build:
`docker-compose up --build --force-recreate`

shutdown:
`docker-compose down`

In Nexus create webhook:

`http://{JENKINS_HOST}:{JENKINS_PORT}/sonarqube-webhook/`

## TODO
- [ ] Add maven programmatically
- [ ] Add JDK programmatically
- [ ] Get Jenkins and Sonar IP address from compose ENV