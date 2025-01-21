pipeline {
    agent any
    tools {
        maven 'mvn'
    }
    environment {
        DOCKER_HUB_USER = "smitsoni004"
        DOCKER_HUB_PASS = "Smitsoni@004"
        DOCKER_IMAGE_TAG = "azguards_application:latest"
        SERVER_IP = "172.31.38.158"  // Replace with your server's IP address
        SERVER_USER = "ubuntu"       // Replace with your server's username
        CONTAINER_NAME = "azguards-application" // Name for the Docker container
    }
    stages {
        stage('Code Checkout') {
            steps {
                git branch: 'Jenkins', changelog: false, poll: false, url: 'https://github.com/Azguards-Technolabs/azguards-application', credentialsId: 'github-token'
            }
        }

stage('Create Docker Compose') {
            steps {
                script {
                    // Create docker-compose.yml file in workspace
                    writeFile file: 'docker-compose.yml', text: '''
services:
  azguards-application:
    image: smitsoni004/azguards_application:latest
    container_name: 'azguards-application'
    environment:
      SPRING_DATASOURCE_URL: jdbc:mariadb://mariadb:3306/azguards_application?allowMultiQueries=true
      SPRING_DATASOURCE_USERNAME: devUser
      SPRING_DATASOURCE_PASSWORD: DevTEst@Kit123!@456
    ports:
      - '8090:8090'
    networks:
      - azguards-whatsapp
    depends_on:
      kafka:
        condition: service_healthy
      mariadb:
        condition: service_healthy
    healthcheck:
      test: ["CMD", "curl", "-fs", "http://localhost:8090/actuator/health"]
      interval: 5s
      retries: 10
      start_period: 10s
      timeout: 3s

  mariadb:
    image: 'mariadb:latest'
    container_name: mariadb
    environment:
      MYSQL_ROOT_PASSWORD: root
      MYSQL_DATABASE: azguards_application
      MYSQL_USER: devUser
      MYSQL_PASSWORD: DevTEst@Kit123!@456
    volumes:
      - dbdata:/var/lib/mysql
    ports:
      - '3306:3306'
    healthcheck:
      test: [ "CMD", "healthcheck.sh", "--connect", "--innodb_initialized" ]
      start_period: 1m
      start_interval: 10s
      interval: 1m
      timeout: 5s
      retries: 3
    networks:
      - azguards-whatsapp

  zookeeper:
    image: confluentinc/cp-zookeeper:latest
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181
      ZOOKEEPER_TICK_TIME: 2000
    ports:
      - 22181:2181
    networks:
      - azguards-whatsapp

  kafka:
    image: confluentinc/cp-kafka:latest
    depends_on:
      - zookeeper
    ports:
      - 9092:9092
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka:9092
      KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: PLAINTEXT:PLAINTEXT,PLAINTEXT_HOST:PLAINTEXT
      KAFKA_INTER_BROKER_LISTENER_NAME: PLAINTEXT
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
    healthcheck:
      test: kafka-topics --bootstrap-server kafka:9092 --list
      interval: 10s
      timeout: 10s
      retries: 3
    networks:
      - azguards-whatsapp
    volumes:
      - kafkadata:/bitnami/kafka

  kafka-ui:
    image: provectuslabs/kafka-ui:latest
    depends_on:
      kafka:
        condition: service_healthy
    ports:
      - 8081:8080
    environment:
      SERVER_PORT: 8080
      DYNAMIC_CONFIG_ENABLED: 'true'
      KAFKA_BROKER: kafka:9092
      KAFKA_CLUSTERS_0_NAME: local
      KAFKA_CLUSTERS_0_BOOTSTRAPSERVERS: kafka:9092
      KAFKA_CLUSTERS_0_ZOOKEEPER: zookeeper:2181
    networks:
      - azguards-whatsapp

networks:
  azguards-whatsapp:
    driver: bridge

volumes:
  dbdata:
  kafkadata:
'''
                }
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

                        // Copy docker-compose file to server
                              sh """
                                   scp -o StrictHostKeyChecking=no docker-compose.yml ${SERVER_USER}@${SERVER_IP}:~/docker-compose.yml
                              """
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
