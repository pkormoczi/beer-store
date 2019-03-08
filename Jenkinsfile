#!groovy
pipeline {
    agent any
    tools {
        maven 'Maven 3.5.4'
        jdk 'JDK 1.8u181'
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

        stage('Checkout from GitHub') {
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

        stage('Unit Testing with Coverage Report') {
            steps {
                sh 'mvn clean clover:setup test clover:aggregate clover:clover'
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

        stage('Build Package') {
            steps {
                sh 'mvn -Dmaven.test.skip=true clean package'
            }
        }
    }
}