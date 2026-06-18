pipeline {
    agent any

    tools {
        maven 'Maven-3.9'
    }

    environment {
        APP_NAME = 'licencia-backend-app'
        JAR_FILE = 'target/*.jar'
        DEPLOY_DIR = 'deploy'
        LOG_DIR = 'logs'
    }

    stages {
        stage('Checkout') {
            steps {
                echo ' Clonando repositorio...'
                git branch: 'main', url: 'https://github.com/PROYECTOS-APP/licencia-app-backend.git'
            }
            post {
                success { echo ' Repositorio clonado exitosamente' }
                failure { echo ' Error al clonar el repositorio' }
            }
        }

        stage('Build') {
            steps {
                echo '🔨 Compilando y empaquetando...'
                bat 'mvn clean package -DskipTests'
            }
            post {
                success {
                    echo ' Build exitoso'
                    archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
                }
                failure {
                    echo ' Build falló'
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
                echo ' Desplegando aplicación...'
                script {
                    // Crear directorios necesarios
                    bat """
                        if not exist ${DEPLOY_DIR} mkdir ${DEPLOY_DIR}
                        if not exist ${LOG_DIR} mkdir ${LOG_DIR}
                    """

                    // Detener proceso anterior en puerto 8081
                    bat """
                        echo " Buscando proceso en puerto 8081..."
                        for /f "tokens=5" %%a in ('netstat -ano ^| findstr :8081 ^| findstr LISTENING') do (
                            echo " Deteniendo proceso PID: %%a"
                            taskkill /F /PID %%a 2>nul
                        )
                    """

                    // Copiar JAR
                    bat """
                        echo " Copiando JAR..."
                        copy target\\*.jar ${DEPLOY_DIR}\\${APP_NAME}.jar
                    """

                    // Iniciar aplicación
                    bat """
                        echo " Iniciando aplicación..."
                        cd ${DEPLOY_DIR}
                        start /MIN cmd /c "java -jar ${APP_NAME}.jar > ..\\${LOG_DIR}\\backend.log 2>&1"
                        echo "Esperando 5 segundos para inicialización..."
                        ping 127.0.0.1 -n 5 > nul
                    """

                    echo ' Backend desplegado en http://localhost:8081'
                }
            }
            post {
                success {
                    echo ' Despliegue exitoso!'

                failure {
                    echo ' Falló el despliegue'
                    script {
                        def logContent = bat(script: "type ${LOG_DIR}\\backend.log 2>nul", returnStdout: true)
                        if (logContent) {
                            echo " Logs de error:"
                            echo logContent.take(500)
                        }
                    }
                }
            }
        }
    }

    post {
        success {
            echo """
                =================================================================================
                 PIPELINE COMPLETADO CON ÉXITO!
                 ${APP_NAME}
                 Build #${BUILD_NUMBER}
                =================================================================================
            """
        }
        failure {
            echo """
                =================================================================================
                 PIPELINE FALLÓ!
                 ${APP_NAME}
                 Build #${BUILD_NUMBER}
                =================================================================================
            """
        }
        always {
            echo ' Pipeline finalizado'
        }
    }
}