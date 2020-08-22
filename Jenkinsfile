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
            archiveArtifacts artifacts: 'target/MelonAuth2-plugin-api-*.jar', fingerprint: true
            cleanWs()
        }
    }
}