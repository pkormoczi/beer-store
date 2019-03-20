FROM jenkins/jenkins:2.164.1

# Skip initial setup
ENV JAVA_OPTS -Djenkins.install.runSetupWizard=false
ENV JENKINS_USER admin
ENV JENKINS_PASS admin
ENV SONAR_SERVER_URL http://172.17.0.2:9000
#ENV SONAR_PLUGIN_VERSION
#ENV SONAR_ADDITIONAL_PROPS
#ENV SONAR_RUNNER_VERSION
# Install plugins
COPY plugins.txt /usr/share/jenkins/ref/plugins.txt
RUN /usr/local/bin/install-plugins.sh < /usr/share/jenkins/ref/plugins.txt

# Adding scipts
COPY Scripts/*.groovy /usr/share/jenkins/ref/init.groovy.d/
COPY Scripts/generate_sonar_auth_token.sh /usr/share/jenkins/
COPY Scripts/init.sh /init.sh

# Reprotect
USER root
RUN chmod +x /init.sh

ENTRYPOINT ["/init.sh"]