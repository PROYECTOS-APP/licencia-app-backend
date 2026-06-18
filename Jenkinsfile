pipeline {
    agent any

    tools {
        maven 'Maven-3.9'
    }

    stages {
        stage('Checkout') {
            steps {
                echo ' Clonando repositorio...'
                checkout scm
            }
        }

        stage('Build') {
            steps {
                echo ' Compilando y empaquetando...'
                bat 'mvn clean package -DskipTests'
                archiveArtifacts artifacts: 'target/*.jar'
            }
        }

        stage('Test') {
            steps {
                echo ' Ejecutando tests...'
                bat 'mvn test'
            }
            post {
                always {
                    junit 'target/surefire-reports/*.xml'
                }
            }
        }

        stage('Deploy') {
            steps {
                echo 'Desplegando...'
                script {
                    // Detener proceso anterior
                    bat '''
                        echo " Deteniendo servidor anterior..."
                        for /f "tokens=5" %%a in ('netstat -ano ^| findstr :8081 ^| findstr LISTENING') do (
                            taskkill /F /PID %%a 2>nul
                        )
                    '''

                    // Iniciar nuevo servidor
                    bat '''
                        echo " Iniciando nuevo servidor..."
                        cd target
                        start /MIN cmd /c java -jar *.jar > backend.log 2>&1
                    '''

                    // Esperar a que inicie (sin timeout)
                    bat 'timeout /t 5 /nobreak > nul'

                    echo ' Backend desplegado en http://localhost:8081'
                }
            }
        }
    }

    post {
        success {
            echo ' PIPELINE EXITOSO'
        }
        failure {
            echo ' PIPELINE FALLÓ'
        }
    }
}