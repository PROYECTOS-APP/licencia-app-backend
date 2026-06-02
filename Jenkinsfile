pipeline {
    agent any

    tools {
        maven 'Maven-3.9'
    }

    stages {
        stage('Checkout') {
            steps {
                echo '📦 Clonando repositorio...'
                checkout scm
            }
        }

        stage('Compile') {
            steps {
                echo '🔨 Compilando...'
                bat 'mvn clean compile -DskipTests'
            }
        }

        stage('Test') {
            steps {
                echo '🧪 Ejecutando tests...'
                bat 'mvn test'
            }
            post {
                always {
                    junit 'target/surefire-reports/*.xml'
                }
            }
        }

        stage('Package') {
            steps {
                echo '📦 Empaquetando...'
                bat 'mvn package -DskipTests'
            }
        }

        stage('Archive') {
            steps {
                echo '📦 Archivando JAR...'
                archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
            }
        }

        stage('Deploy') {
            steps {
                echo '🚀 Desplegando...'
                bat '''
                    echo "Deteniendo servidor anterior..."
                    for /f "tokens=5" %%a in ('netstat -ano ^| findstr :8081 ^| findstr LISTENING') do (
                        taskkill /F /PID %%a 2>nul
                    )

                    echo "Iniciando nuevo servidor..."
                    cd target
                    start /MIN cmd /c java -jar *.jar > backend.log 2>&1
                    timeout /t 3 /nobreak
                '''
                echo '✅ Backend desplegado en http://localhost:8081'
            }
        }
    }

    post {
        success {
            echo '🎉 PIPELINE COMPLETADO CON ÉXITO'
        }
        failure {
            echo '❌ PIPELINE FALLÓ'
        }
    }
}