pipeline {
    agent any

    tools {
        maven 'Maven-3.9'
    }

    environment {
        APP_NAME = 'licencia-backend-app'
        JAR_FILE = 'target/*.jar'
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
            }
            post {
                success {
                    archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
                }
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
                echo ' Desplegando...'
                script {
                    // Detener proceso anterior en puerto 8081
                    bat '''
                        echo "Deteniendo servidor anterior en puerto 8081..."
                        for /f "tokens=5" %%a in ('netstat -ano ^| findstr :8081 ^| findstr LISTENING') do (
                            echo " Deteniendo PID: %%a"
                            taskkill /F /PID %%a 2>nul
                        )
                    '''

                    // Verificar que el JAR existe
                    bat '''
                        echo " Verificando archivo JAR..."
                        dir target\\*.jar
                    '''

                    // Iniciar nuevo servidor
                    bat '''
                        echo " Iniciando nuevo servidor..."
                        cd target
                        start /MIN cmd /c "java -jar *.jar > backend.log 2>&1"
                        echo " Servidor iniciado en http://localhost:8081"
                    '''
                }
                echo 'Despliegue completado!'
            }
        }
    }

    post {
        success {
            echo ' PIPELINE EXITOSO'
        }
        failure {
            echo ' PIPELINE FALLÓ'
            // Mostrar logs si falla
            script {
                def logContent = bat(script: "type target\\backend.log 2>nul", returnStdout: true)
                if (logContent) {
                    echo " Logs del servidor:"
                    echo logContent.take(500)
                }
            }
        }
        always {
            echo ' Pipeline finalizado'
        }
    }
}