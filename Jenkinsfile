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
                sh 'mvn -Dmaven.test.skip=true clean install'
            }
        }

        stage('Unit test') {
            steps {
                sh 'mvn test'
                junit '**/target/surefire-reports/*.xml'
            }
        }

        stage('SonarQube analysis') {
            steps {
                withSonarQubeEnv('SonarQube') {
                    sh 'mvn sonar:sonar'
                }
            }
        }
        stage("Check SonarQube Quality Gate") {
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
                sh 'mvn -Dmaven.test.skip=true package'
            }
        }
    }
}