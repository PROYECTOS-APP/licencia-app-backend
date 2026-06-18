pipeline {
    agent any

    tools {
        maven 'Maven-3.9'
    }

    environment {
        APP_NAME = 'licencia-backend-app'
        DEPLOY_DIR = 'deploy'
        LOG_DIR = 'logs'
    }

    stages {
        stage('Checkout') {
            steps {
                echo ' Clonando repositorio...'
                git branch: 'main', url: 'https://github.com/PROYECTOS-APP/licencia-app-backend.git'
            }
        }

        stage('Build') {
            steps {
                echo 'Compilando y empaquetando...'
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
                    bat """
                        if not exist ${DEPLOY_DIR} mkdir ${DEPLOY_DIR}
                        if not exist ${LOG_DIR} mkdir ${LOG_DIR}
                    """

                    bat """
                        echo " Deteniendo servidor anterior en puerto 8081..."
                        for /f "tokens=5" %%a in ('netstat -ano ^| findstr :8081 ^| findstr LISTENING') do (
                            echo " Deteniendo PID: %%a"
                            taskkill /F /PID %%a 2>nul
                        )
                    """

                    bat """
                        echo " Copiando JAR..."
                        copy target\\*.jar ${DEPLOY_DIR}\\${APP_NAME}.jar
                    """

                    bat """
                        echo " Iniciando servidor..."
                        cd ${DEPLOY_DIR}
                        start /MIN cmd /c "java -jar ${APP_NAME}.jar > ..\\${LOG_DIR}\\backend.log 2>&1"
                        echo " Esperando 5 segundos..."
                        ping 127.0.0.1 -n 5 > nul
                    """

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
        always {
            echo ' Pipeline finalizado'
        }
    }
}