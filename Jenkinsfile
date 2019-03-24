#!groovy
pipeline {
    agent any
    tools {
        maven 'Maven-3.6.0'
        jdk 'JDK-8u202'
    }
    stages {
        stage('Initialize') {
            steps {
                sh '''
                    echo "PATH = ${PATH}"
                    echo "M2_HOME = ${M2_HOME}"
                '''
                sh 'java -version'
                sh 'mvn -version'
            }
        }

        stage('Checkout') {
            steps {
                git 'https://github.com/pkormoczi/beer-store.git'
            }
        }

        stage('Preparation') {
            steps {
                dir('beer-store-contract') {
                    sh 'mvn clean install'
                }
            }
        }

        stage('Build') {
            steps {
                withMaven() {
                    sh 'mvn -Dmaven.test.skip=true clean install'
                }
            }
        }

        stage("Test") {
            parallel {
                stage('Unit test') {
                    steps {
                        withMaven() {
                            sh 'mvn test'
                        }
                    }
                }
                stage('Unit test') {
                    steps {
                        withMaven() {
                            sh 'mvn org.apache.maven.plugins:maven-compiler-plugin:testCompile org.apache.maven.plugins:maven-failsafe-plugin:integration-test'
                        }
                    }
                }
            }
        }

        stage('SonarQube analysis') {
            steps {
                withSonarQubeEnv('SonarQube') {
                    sh 'mvn sonar:sonar'
                }
            }
        }
        stage("SonarQube Quality Gate") {
            steps {
                script {
                    timeout(time: 30, unit: 'MINUTES') {
                        def qg = waitForQualityGate()
                        if (qg.status != 'OK') {
                            error "Pipeline aborted due to quality gate failure: ${qg.status}"
                        }
                    }
                }
            }
        }

        stage('Package') {
            steps {
                withMaven() {
                    sh 'mvn -Dmaven.test.skip=true package'
                }
            }
        }
    }
}