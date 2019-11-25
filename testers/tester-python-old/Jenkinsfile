
node('build_tester') {
  try {
    stage("Checkout repositories") {
      checkout scm
    }

    stage("Build docker container") {
      sh(returnStdout: false, script: 'docker build -t hodor_python .')
    }

    stage("Deployment") {
      if (env.BRANCH_NAME == 'master') {
        name = 'hodor_python'
      } else {
        name = 'hodor_python_' + env.BRANCH_NAME
      }
      sh(returnStdout: false, script: 'docker save -o hodor_python.tar ' + name)
      withCredentials([[$class: 'FileBinding', credentialsId: '98ba0434-af5e-4432-8fa1-14b2b2633208', variable: 'SSHKEYFILE']]) {
        sh('eval `ssh-agent -s`;'
         + 'ssh-agent bash -c \'ssh-add "' + env.SSHKEYFILE + '";'
         + 'scp hodor_python.tar jenkins@193.40.252.119: '
         + '&& ssh jenkins@193.40.252.119 "docker load -i hodor_python.tar"\'')
      }
    }
  } catch(err) {
    currentBuild.result = 'FAILURE'
    throw err
  }

}