stage('Build') {
  node {
    echo "Hello $USER, we are building"
    sh(returnStdout: true, script: 'echo "BUILD NO: ${BUILD_NUMBER}" > output.json').trim()
  }
}

stage('Test') {
  node ("debian-jessie"){
    echo "Hello $USER, we are now testing"
    sh(returnStdout: true, script: 'ls -algh').trim()
  }
}

stage('Deploy') { 
  node {
    echo "Hello $USER, we are now deploying"
    def server = Artifactory.server 'Artifactory'
    def uploadSpec = """{
      "files": [
      {
        "pattern": "output.json",
        "target": "libs-release-local/test_files/"
      }
    ] 
    }"""    
    server.upload(uploadSpec)
  } 
}
