FROM jenkins/jenkins:lts

COPY Plugins.txt /usr/share/jenkins/ref/plugins.txt
RUN /usr/local/bin/install-plugins.sh < /usr/share/jenkins/ref/plugins.txt

# Skip initial setup
ENV JAVA_OPTS -Djenkins.install.runSetupWizard=false
ENV JENKINS_USER admin
ENV JENKINS_PASS admin

# Adding scipts
COPY Scripts/*.groovy /usr/share/jenkins/ref/init.groovy.d/

#VOLUME /var/jenkins_home