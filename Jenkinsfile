pipeline {
    agent any

    tools {
        maven 'Maven-3.9'
    }

    environment {
        APP_NAME = 'licencia-backend-app'
    }

    stages {
        stage('Build') {
            steps {
                echo 'Compilando...'
                bat 'mvn clean package -DskipTests'
            }
            post {
            post {
                success {
                    archiveArtifacts artifacts: 'target/*.jar'
                }
            }
        }

        stage('Deploy') {
            steps {
                echo 'Desplegando...'
                script {
                    // Detener proceso en puerto 8081
                    bat '''
                        for /f "tokens=5" %%a in ('netstat -ano ^| findstr :8081 ^| findstr LISTENING') do (
                            taskkill /F /PID %%a 2>nul
                        )
                    '''
                    // Iniciar app
                    bat 'start /MIN java -jar target/*.jar'
                    // Esperar a que inicie
                    timeout(time: 30, unit: 'SECONDS') {
                        waitUntil {
                            try {
                                def response = bat(script: "curl -s -o nul -w \"%%{http_code}\" http://localhost:8081/actuator/health", returnStdout: true).trim()
                                return response == '200'
                            } catch (Exception e) {
                                return false
                            }
                        }
                    }
                }
                echo 'Desplegado'
            }
        }
    }
}