FROM sonarqube:7.9.1-community

COPY --chown=sonarqube:sonarqube config_sonar.sh /opt/sonarqube/
ENTRYPOINT ["/opt/sonarqube/config_sonar.sh"]