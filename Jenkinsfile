pipeline {
  agent {
    docker 'maven:3.3.9'
  }
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