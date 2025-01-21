pipeline {
    agent any
    tools {
        maven 'mvn'
    }
    environment {
        DOCKER_HUB_USER = "smitsoni004"
        DOCKER_HUB_PASS = "Smitsoni@004"
        DOCKER_IMAGE_TAG = "azguards_application:latest"
//         SERVER_IP = "172.31.38.158"  // Replace with your server's IP address
        SERVER_IP = "13.235.12.7"  // Replace with your server's IP address
        SERVER_USER = "ubuntu"       // Replace with your server's username
        CONTAINER_NAME = "azguards-application" // Name for the Docker container
    }
    stages {
        stage('Code Checkout') {
            steps {
                git branch: 'Jenkins', changelog: false, poll: false, url: 'https://github.com/Azguards-Technolabs/azguards-application', credentialsId: 'github-token'
            }
        }

        stage('Clean & Package') {
            steps {
                sh "mvn clean package -DskipTests"
            }
        }

        stage('Docker Build') {
            steps {
                script {
                    withDockerRegistry(credentialsId: 'Docker-hub-key', toolName: 'docker') {
                        def buildTag = "${CONTAINER_NAME}"
                        def latestTag = "${CONTAINER_NAME}:latest"  // Define latest tag
                        sh "docker build -t ${DOCKER_HUB_USER}/${DOCKER_IMAGE_TAG} -f Dockerfile ."
                    }
                }
            }
        }

        stage('Push Docker Image') {
            steps {
                sh """
                    docker login -u ${DOCKER_HUB_USER} -p ${DOCKER_HUB_PASS}
                    docker push ${DOCKER_HUB_USER}/${DOCKER_IMAGE_TAG}
                """
            }
        }

        stage('Trivy Vulnerability Scan') {
            steps {
                sh """
                    # Run Trivy scan
                    trivy image \
                        --severity HIGH,CRITICAL \
                        --exit-code 1 \
                        --no-progress \
                        --clear-cache \
                        ${DOCKER_HUB_USER}/${DOCKER_IMAGE_TAG}
                """
            }
        }

        stage('Deploy to Server') {
            steps {
                script {
                    sshagent(['server-ssh-cred']) { // Use the ID of the SSH credential you added in Jenkins
                        sh """
                        ssh -o StrictHostKeyChecking=no ${SERVER_USER}@${SERVER_IP}
                            docker pull ${DOCKER_HUB_USER}/${DOCKER_IMAGE_TAG}
                            docker stop ${CONTAINER_NAME} || true
                            docker rm ${CONTAINER_NAME} || true
                            docker run -d --name ${CONTAINER_NAME} ${DOCKER_HUB_USER}/${DOCKER_IMAGE_TAG}
                        """
                    }
                }
            }
        }
    }
    post {
        success {
            echo 'Build, Push, and Deployment Successful'
        }

        failure {
            echo 'Build, Push, or Deployment Failed'
        }
    }
}
