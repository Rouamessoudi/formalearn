pipeline {
  agent any

  options {
    disableConcurrentBuilds()
  }

  parameters {
    booleanParam(name: 'SKIP_SONAR', defaultValue: false, description: 'true uniquement si SonarQube n’est pas encore joignable. Défaut : analyse + Quality Gate obligatoires.')
    booleanParam(name: 'RUN_FRONTEND_TESTS', defaultValue: true, description: 'Karma/ChromeHeadless.')
    booleanParam(name: 'PUSH_IMAGES', defaultValue: false, description: 'Pousser les images si DOCKER_REGISTRY est défini (credentials docker-registry).')
    booleanParam(name: 'DEPLOY_K8S', defaultValue: false, description: 'kubectl apply -k k8s/ seulement si un cluster sain est configuré sur l’agent.')
    booleanParam(name: 'FORCE_FAIL', defaultValue: false, description: 'Échec volontaire après les tests (démo).')
  }

  environment {
    CI = 'true'
  }

  stages {
    stage('Checkout') {
      steps {
        script {
          try {
            checkout scm
          } catch (Exception e) {
            echo "Pas de SCM GitHub sur ce job (${e.class.simpleName}). Copie du monorepo monté dans /workspace."
            if (isUnix()) {
              sh '''
                set -e
                if [ -d /workspace/backend ]; then
                  tar -C /workspace -cf - \
                    --exclude='frontend/node_modules' \
                    --exclude='frontend/dist' \
                    --exclude='backend/target' \
                    --exclude='ml/.venv' \
                    --exclude='ml/.venv-ci' \
                    --exclude='ml/__pycache__' \
                    --exclude='.git' \
                    Jenkinsfile docker-compose.yml .env.example backend frontend ml docs scripts \
                    | tar -xf -
                else
                  echo "Workspace Jenkins déjà peuplé par Git."
                  ls -la
                fi
              '''
            } else {
              bat 'echo Checkout SCM Jenkins requis sur agent Windows (Pipeline script from SCM).'
            }
          }
        }
      }
    }

    stage('Backend Build') {
      steps {
        dir('backend') {
          script {
            if (isUnix()) {
              sh 'chmod +x mvnw && ./mvnw -B clean compile'
            } else {
              bat 'mvnw.cmd -B clean compile'
            }
          }
        }
      }
    }

    stage('Backend Tests') {
      steps {
        dir('backend') {
          script {
            if (isUnix()) {
              sh './mvnw -B test'
            } else {
              bat 'mvnw.cmd -B test'
            }
          }
        }
      }
    }

    stage('Frontend Install') {
      steps {
        dir('frontend') {
          script {
            if (isUnix()) {
              sh 'npm ci'
            } else {
              bat 'npm ci'
            }
          }
        }
      }
    }

    stage('Frontend Build') {
      steps {
        dir('frontend') {
          script {
            if (isUnix()) {
              sh 'npm run build'
            } else {
              bat 'npm run build'
            }
          }
        }
      }
    }

    stage('Frontend Tests') {
      when {
        expression { return params.RUN_FRONTEND_TESTS }
      }
      steps {
        dir('frontend') {
          script {
            if (isUnix()) {
              sh 'npx ng test --watch=false --browsers=ChromeHeadlessNoSandbox'
            } else {
              bat 'npx ng test --watch=false --browsers=ChromeHeadlessNoSandbox'
            }
          }
        }
      }
    }

    stage('ML Tests') {
      steps {
        dir('ml') {
          script {
            if (isUnix()) {
              sh '''
                set -e
                python3 -m venv .venv-ci
                . .venv-ci/bin/activate
                pip install -q -r requirements.txt
                python -m pytest -q
              '''
            } else {
              bat '''
                python -m venv .venv-ci
                .venv-ci\\Scripts\\python.exe -m pip install -q -r requirements.txt
                .venv-ci\\Scripts\\python.exe -m pytest -q
              '''
            }
          }
        }
      }
    }

    stage('ML Training') {
      steps {
        dir('ml') {
          script {
            if (isUnix()) {
              sh '''
                set -e
                . .venv-ci/bin/activate
                python data/generate_dataset.py
                python train.py
                test -f models/recommender_model.pkl
              '''
            } else {
              bat '''
                .venv-ci\\Scripts\\python.exe data\\generate_dataset.py
                .venv-ci\\Scripts\\python.exe train.py
                if not exist models\\recommender_model.pkl exit /b 1
              '''
            }
          }
        }
      }
    }

    stage('SonarQube') {
      when {
        expression { return !params.SKIP_SONAR }
      }
      steps {
        withCredentials([string(credentialsId: 'sonar-token', variable: 'SONAR_TOKEN')]) {
          dir('backend') {
            script {
              if (isUnix()) {
                sh './mvnw -B -DskipTests sonar:sonar -Dsonar.host.url=$SONAR_HOST_URL -Dsonar.token=$SONAR_TOKEN'
              } else {
                bat 'mvnw.cmd -B -DskipTests sonar:sonar -Dsonar.host.url=%SONAR_HOST_URL% -Dsonar.token=%SONAR_TOKEN%'
              }
            }
          }
        }
      }
    }

    stage('Quality Gate') {
      when {
        expression { return !params.SKIP_SONAR }
      }
      steps {
        timeout(time: 10, unit: 'MINUTES') {
          waitForQualityGate abortPipeline: true
        }
      }
    }

    stage('Package') {
      steps {
        dir('backend') {
          script {
            if (isUnix()) {
              sh './mvnw -B -DskipTests package && ls target/*.jar'
            } else {
              bat 'mvnw.cmd -B -DskipTests package'
            }
          }
        }
        script {
          if (isUnix()) {
            sh 'test -f frontend/dist/frontend/browser/index.html'
            sh 'test -f ml/models/recommender_model.pkl'
          } else {
            bat 'if not exist frontend\\dist\\frontend\\browser\\index.html exit /b 1'
            bat 'if not exist ml\\models\\recommender_model.pkl exit /b 1'
          }
        }
      }
    }

    stage('Docker Build') {
      steps {
        script {
          if (isUnix()) {
            sh '''
              set -e
              docker build -t formalearn-backend:${BUILD_NUMBER} ./backend
              docker build -t formalearn-frontend:${BUILD_NUMBER} ./frontend
              docker build -t formalearn-ml:${BUILD_NUMBER} ./ml
            '''
          } else {
            bat '''
              docker build -t formalearn-backend:%BUILD_NUMBER% ./backend
              docker build -t formalearn-frontend:%BUILD_NUMBER% ./frontend
              docker build -t formalearn-ml:%BUILD_NUMBER% ./ml
            '''
          }
        }
      }
    }

    stage('Docker Push') {
      when {
        expression { return params.PUSH_IMAGES }
      }
      steps {
        withCredentials([usernamePassword(credentialsId: 'docker-registry', usernameVariable: 'REG_USER', passwordVariable: 'REG_PASS')]) {
          script {
            if (!env.DOCKER_REGISTRY?.trim()) {
              error('PUSH_IMAGES=true mais DOCKER_REGISTRY n’est pas défini sur Jenkins.')
            }
            if (isUnix()) {
              sh '''
                set -e
                echo "$REG_PASS" | docker login "$DOCKER_REGISTRY" -u "$REG_USER" --password-stdin
                docker tag formalearn-backend:${BUILD_NUMBER} ${DOCKER_REGISTRY}/formalearn-backend:${BUILD_NUMBER}
                docker tag formalearn-frontend:${BUILD_NUMBER} ${DOCKER_REGISTRY}/formalearn-frontend:${BUILD_NUMBER}
                docker tag formalearn-ml:${BUILD_NUMBER} ${DOCKER_REGISTRY}/formalearn-ml:${BUILD_NUMBER}
                docker push ${DOCKER_REGISTRY}/formalearn-backend:${BUILD_NUMBER}
                docker push ${DOCKER_REGISTRY}/formalearn-frontend:${BUILD_NUMBER}
                docker push ${DOCKER_REGISTRY}/formalearn-ml:${BUILD_NUMBER}
              '''
            } else {
              bat 'echo Docker push Windows : définir DOCKER_REGISTRY et credential docker-registry'
            }
          }
        }
      }
    }

    stage('Kubernetes') {
      when {
        expression { return params.DEPLOY_K8S }
      }
      steps {
        script {
          if (isUnix()) {
            sh '''
              set -e
              kubectl cluster-info
              kubectl apply -k k8s/
              kubectl -n formalearn rollout status deploy/backend --timeout=180s
            '''
          } else {
            bat 'kubectl apply -k k8s/'
          }
        }
      }
    }

    stage('CI quality gate (demo)') {
      when {
        expression { return params.FORCE_FAIL }
      }
      steps {
        error('FORCE_FAIL=true : échec volontaire pour la démonstration de soutenance. La CI a bien bloqué le pipeline.')
      }
    }
  }

  post {
    success {
      echo 'Pipeline SUCCESS : compile, tests, package et images Docker OK.'
    }
    failure {
      echo 'Pipeline FAILED : un stage a renvoyé un code d’erreur. Corriger puis relancer. Ne jamais ignorer un test rouge.'
    }
  }
}
