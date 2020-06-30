pipeline {
    agent{node('master')}
    stages {
        stage('clear workspace and download project') {
            steps {
                script {
			cleanWs()
			withCredentials([
				usernamePassword(credentialsId: 'srv_sudo',
                        	usernameVariable: 'username',
                        	passwordVariable: 'password')
				]) {
					try{
						sh "echo '${password}' | sudo -S docker stop nginx_alhassan"
                            			sh "echo '${password}' | sudo -S docker container rm nginx_alhassan"
					} catch (Exception e) {
                            			print 'no previous container, operation was failed'
						currentBuild.result = 'FAILURE'
                        		}	
				}
                }
                script {
                    echo 'update project from git'
                    checkout([$class                           : 'GitSCM',
                              branches                         : [[name: '*/master']],
                              doGenerateSubmoduleConfigurations: false,
                              extensions                       : [[$class           : 'RelativeTargetDirectory',
                                                                   relativeTargetDir: 'auto']],
                              submoduleCfg                     : [],
                              userRemoteConfigs                : [[credentialsId: 'AlhassanHaGit', url: 'https://github.com/alhassanha/jenkins_practical.git']]])
                }
            }
        }
        stage ('Create docker image'){
            steps{
                script{
                    withCredentials([
                        usernamePassword(credentialsId: 'srv_sudo',
                        usernameVariable: 'username',
                        passwordVariable: 'password')
                    ]) {

                        sh "echo '${password}' | sudo -S docker build ${WORKSPACE}/auto -t docker_img_alhassan"
                        sh "echo '${password}' | sudo -S docker run -d -p 8642:80 --name nginx_alhassan -v /home/adminci/alhassan:/stat docker_img_alhassan"
                    }
                }
            }
        }
		
		stage ('write stats to file'){
            steps{
                script{
                    withCredentials([
                        usernamePassword(credentialsId: 'srv_sudo',
                        usernameVariable: 'username',
                        passwordVariable: 'password')
                    ]) {
                        
                        sh "echo '${password}' | sudo -S docker exec -t nginx_alhassan bash -c 'df -h > /stat/stats.txt'"
			sh "echo '${password}' | sudo -S docker exec -t nginx_alhassan bash -c 'top -n 1 -b >> /stat/stats.txt'"
                    }
                }
            }
        }
	    stage ('stop docker container'){
            steps{
                script{
                    withCredentials([
                        usernamePassword(credentialsId: 'srv_sudo',
                        usernameVariable: 'username',
                        passwordVariable: 'password')
                    ]) {
                        
                        sh "echo '${password}' | sudo -S docker stop nginx_alhassan"
                    }
                }
            }
        }
        
    }

    
}
