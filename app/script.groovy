def buildJar() {
    echo 'building the application...'
    sh 'mvn package'
}

def buildImage(String dockerRepo) {
    echo "building the docker image..."
    withCredentials([usernamePassword(credentialsId: 'docker', passwordVariable: 'PASSWORD', usernameVariable: 'USERNAME')]) {
        sh "docker build -t ${dockerRepo} ."
        sh 'echo $PASSWORD | docker login -u $USERNAME --password-stdin'
        sh "docker push ${dockerRepo}"
    }
}

def deployApp() {
    echo 'deploying the application...'
}

return this
