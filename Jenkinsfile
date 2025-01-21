pipeline {
    agent any
    tools {
        maven 'mvn'
    }
    environment {
        DOCKER_HUB_USER = "smitsoni004"
        DOCKER_HUB_PASS = "Smitsoni@004"
        DOCKER_IMAGE_TAG = "azguards_application:latest"
        SERVER_IP = "172.31.38.158"
        SERVER_USER = "ubuntu"
        CONTAINER_NAME = "azguards-application"
        DB_HOST = "13.235.12.7"
        DB_PORT = "3306"
        DB_NAME = "azguards_application"
        DB_USER = "devUser"
        DB_PASSWORD = "DevTEst@Kit123!@456"
    }
    stages {
        stage('Code Checkout') {
            steps {
                git branch: 'Jenkins',
                    changelog: false,
                    poll: false,
                    url: 'https://github.com/Azguards-Technolabs/azguards-application',
                    credentialsId: 'github-token'
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
                    sh "docker build -t ${DOCKER_HUB_USER}/${DOCKER_IMAGE_TAG} -f Dockerfile ."
                }
            }
        }

        stage('Push Docker Image') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'Docker-hub-key',
                                              usernameVariable: 'DOCKER_USERNAME',
                                              passwordVariable: 'DOCKER_PASSWORD')]) {
                    sh """
                        echo '${DOCKER_PASSWORD}' | docker login -u '${DOCKER_USERNAME}' --password-stdin
                        docker push ${DOCKER_HUB_USER}/${DOCKER_IMAGE_TAG}
                    """
                }
            }
        }

        stage('Trivy Vulnerability Scan') {
            steps {
                sh """
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
                    sshagent(['server-ssh-cred']) {
                        sh """
                            ssh -o StrictHostKeyChecking=no ${SERVER_USER}@${SERVER_IP} "
                                # Create network if it doesn't exist
                                docker network inspect azguards-whatsapp >/dev/null 2>&1 || docker network create azguards-whatsapp

                                # Stop and remove existing container
                                docker stop ${CONTAINER_NAME} || true
                                docker rm ${CONTAINER_NAME} || true

                                # Pull the latest image
                                docker pull ${DOCKER_HUB_USER}/${DOCKER_IMAGE_TAG}

                                # Run the new container
                                docker run -d \\
                                    --name ${CONTAINER_NAME} \\
                                    --network azguards-whatsapp \\
                                    -p 8090:8090 \\
                                    -e SPRING_DATASOURCE_URL=jdbc:mariadb://${DB_HOST}:${DB_PORT}/${DB_NAME}?allowMultiQueries=true \\
                                    -e SPRING_DATASOURCE_USERNAME=${DB_USER} \\
                                    -e SPRING_DATASOURCE_PASSWORD=${DB_PASSWORD} \\
                                    -e SPRING_KAFKA_BOOTSTRAP_SERVERS=kafka:9092 \\
                                    ${DOCKER_HUB_USER}/${DOCKER_IMAGE_TAG}"
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
        always {
            sh "docker rmi ${DOCKER_HUB_USER}/${DOCKER_IMAGE_TAG} || true"
        }
    }
}
