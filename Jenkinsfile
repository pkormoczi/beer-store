node {
   def mvnHome
   stage('Preparation') {
      // Get some code from a GitHub repository
      git 'https://github.com/pkormoczi/beer-store.git'
      // Get the Maven tool.
      mvnHome = tool 'Maven 3.5.4'
   }
   stage('Install contract') {
       dir('beer-store-contract'){
         sh "'${mvnHome}/bin/mvn' clean install"
       }
   }
   stage('Build') {
      // Run the maven build
         sh "'${mvnHome}/bin/mvn' clean package"
   }
   stage('Clover reports') {
      // Run the maven build
         sh "'${mvnHome}/bin/mvn' clean clover:setup test clover:aggregate clover:clover"
   }
   stage('SonarQube analysis') {
       withSonarQubeEnv('SonarQube') {
         sh "'${mvnHome}/bin/mvn' sonar:sonar -Dsonar.host.url=http://172.17.0.3:9000 -Dsonar.login=c0bee1da6fc598716e80e82011ff6c25ebb75395"
       }
  }
  stage("Quality Gate"){
      timeout(time: 30, unit: 'MINUTES') {
            def qg = waitForQualityGate()
            if (qg.status != 'OK') {
                error "Pipeline aborted due to quality gate failure: ${qg.status}"
            }
      }
  }
  stage('Results') {
    junit '**/target/surefire-reports/TEST-*.xml'
  }
}