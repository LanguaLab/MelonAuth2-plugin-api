pipeline {
    agent any
    stages {
        stage('Build') {
            steps {
                sh 'mvn package'
            }
        }
    }

    post {
        always {
            archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
            cleanWs()
        }
    }
}