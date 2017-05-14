pipeline {
  
  agent { docker 'maven:3.3.3' }
  
  stages {
    
    stage('build') {
      steps {
        sh 'clean compile'
      }
    }
    
    stage('test') {
      steps {
        sh 'test'
      }
    }
    
  }
}
