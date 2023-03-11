pipeline {
    agent any

    environment {
        imagename = "spring1"
        registryCredential = 'alger426'
        dockerImage = ''
        //KUBECONFIG = credentials('186562b5-1671-45e0-9f70-ae582b1c6fee')
        DATE = 'test'
        NAMESPACE = 'jenkins'
    }

    stages {

        stage('new Kuber Run') {
            steps {
                //이미지 불러오는거 아님
                podTemplate(label: 'builder',
                containers: [
                    containerTemplate(name: 'kubectl', image: 'jenkins/jenkins', command: 'cat', ttyEnabled: true)
                ]){
                    node('builder') {
                        container('kubectl') {
                            sh "echo ${DATE}"
                            sh "kubectl apply -f .deployment.yaml -n ${NAMESPACE}"                         
                        }
                    }
                }
            }
        }
        stage('Prepare') {
          steps {
            echo 'Clonning Repository'
            git url: 'git@github.com:choisaelim/jenkinsbuildtest.git',
              branch: 'main',
              credentialsId: 'github'
            }
            post {
             success { 
               echo 'Successfully Cloned Repository'
             }
           	 failure {
               error 'This pipeline stops here...'
             }
          }
        }

        stage('Bulid Gradle') {
          steps {
            echo 'Bulid Gradle'
            dir('demo'){
                sh './gradlew clean build --no-daemon --stacktrace -Xmx4096m -Xms4096m'
            }
          }
          post {
            success { 
               echo 'dir demo'
            }
            failure {
              error 'This pipeline stops here...'
            }
          }
        }
        
        stage('Bulid Docker') {
          steps {
            echo 'Bulid Docker'
            script {
                dockerImage = docker.build imagename
            }
          }
          post {
            failure {
              error 'This pipeline stops here...'
            }
          }
        }

        stage('Push Docker') {
          steps {
            echo 'Push Docker'
            script {
                docker.withRegistry( '', registryCredential) {
                    dockerImage.push() 
                }
            }
          }
          post {
            failure {
              error 'This pipeline stops here...'
            }
          }
        }
        
        stage('Kubernetes deploy') {
            steps {
                kubernetesDeploy configs: "deployment.yaml", kubeconfigId: 'kubernetes'
                //sh "kubectl --kubeconfig=/root/.jenkins/.kube/config rollout restart deployment/wildfly-deployment"
                sh "kubectl run -f ${imagename}"
            }
        }

        stage('Deploy to Kubernetes') {
            steps {
                script {
                    //def kubeconfig = readFile(KUBECONFIG).trim() // Read the Kubernetes configuration file
                    
                    // Deploy the YAML file using the Kubernetes plugin
                    kubernetesDeploy(
                        kubeconfigId: '186562b5-1671-45e0-9f70-ae582b1c6fee', // ID of the Kubernetes configuration file stored as Jenkins credentials
                        configs: 'develop.yaml', // Path to your Kubernetes YAML file(s)
                        enableConfigSubstitution: true, // Enable substitution of environment variables in the YAML file
                        namespace: 'jenkins', // Namespace in which to deploy the YAML file
                        recreate: true // Recreate the Kubernetes resources if they already exist
                    )
                }
            }
        }
        
        
        

        stage('Kubernetes Run') {
            container('kubectl') {
                sh ("kubectl set image -n ${POD_NAMESPACE} deployment/${K8S_DEPLOYMENT_NAME} ${K8S_DEPLOYMENT_NAME}=imagename:latest")
            }   
        }
        stage('Kuber Run') {
            steps{
                podTemplate(yaml: '''
                    apiVersion: v1
                    kind: Pod
                    spec:
                      containers:
                      - name: maven
                        image: maven:3.8.1-jdk-8
                        command:
                        - sleep
                        args:
                        - 99d
                      - name: golang
                        image: golang:1.16.5
                        command:
                        - sleep
                        args:
                        - 99d
                ''') {
            }
        }
        stage('Docker Run') {
            steps {
                echo 'Pull Docker Image & Docker Image Run'
                sshagent (credentials: ['SSH Credential ID -> ssh']) {
                    sh "ssh -o StrictHostKeyChecking=no [Spring Boot Server username]@[Spring Boot Server IP 주소] 'docker pull [도커이미지 이름]'" 
                    sh "ssh -o StrictHostKeyChecking=no [Spring Boot Server username]@[Spring Boot Server IP 주소] 'docker ps -q --filter name=[컨테이너 이름] | grep -q . && docker rm -f \$(docker ps -aq --filter name=[컨테이너 이름])'"
                    sh "ssh -o StrictHostKeyChecking=no [Spring Boot Server username]@[Spring Boot Server IP 주소] 'docker run -d --name [컨테이너 이름] -p 8080:8080 [도커이미지 이름]'"
                }
            }
        }
    }
}
docker build -t ./DockerFile spring

docker build . -f Dockerfile

sh '''
        kubectl create deployment pl-bulk-prod --image=192.168.1.10:8443/echo-ip
        kubectl expose deployment pl-bulk-prod --type=LoadBalancer --port=8080 \
                                               --target-port=80 --name=pl-bulk-prod-svc
        '''