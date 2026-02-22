def gv

pipeline {
  agent any
  tools {
    maven 'maven-3.9'
  }
  parameters {
    string(name: 'DOCKER_REPO', defaultValue: 'explicitlogic/app:pipeline')
  }
  stages {
    stage("init") {
      steps {
        dir('app') {
          script {
            gv = load "script.groovy"
          }
        }
      }
    }

    stage("build jar") {
      steps {
        dir('app') {
          script {
            gv.buildJar()
          }
        }
      }
    }

    stage("build image") {
      steps {
        dir('app') {
          script {
            gv.buildImage(params.DOCKER_REPO)
          }
        }
      }
    }

    stage("deploy") {
      steps {
        dir('app') {
          script {
            gv.deployApp()
          }
        }
      }
    }
  }
} 
