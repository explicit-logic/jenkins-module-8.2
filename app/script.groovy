def testApp() {
    echo 'testing the application...'
    sh 'mvn test'
}

def buildJar() {
    echo 'building the application...'
    sh 'mvn package'
}

def buildImage(String dockerRepo, String tag = 'latest') {
    echo "building the docker image..."
    withCredentials([usernamePassword(credentialsId: 'docker', passwordVariable: 'PASSWORD', usernameVariable: 'USERNAME')]) {
        sh "docker build -t ${dockerRepo}:${tag} ."
        sh 'echo $PASSWORD | docker login -u $USERNAME --password-stdin'
        sh "docker push ${dockerRepo}:${tag}"
    }
}

def deployApp() {
    echo 'deploying the application...'
}

return this
