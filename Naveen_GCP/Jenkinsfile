pipeline {
  agent {
    kubernetes {
      label 'slave'
      yaml """
kind: Pod
metadata:
  name: kaniko
spec:
  containers:
  - name: jnlp
    workingDir: /home/jenkins
    
    
  - name: maven
    workingDir: /home/jenkins
    image: gcr.io/kmsh-250102/maven-slave
    imagePullPolicy: Always
    command:
    - cat
    tty: true   	
  - name: kaniko
    workingDir: /home/jenkins
    image: gcr.io/kaniko-project/executor:debug
    imagePullPolicy: Always
    command:
    - /busybox/cat
    tty: true
    volumeMounts:
      - name: kaniko-secret
        mountPath: /secret
    env:
      - name: GOOGLE_APPLICATION_CREDENTIALS
        value: /secret/kaniko-secret.json		
  volumes:
    - name: kaniko-secret
      secret:
        secretName: kaniko-secret
"""
    }
  }
  stages {
    stage('checkout_code') {      
      steps {
        container(name: 'maven') {                    
          git branch: 'master',
	      credentialsId: 'github',
          url: 'https://github.com/satishrawat/app1.git' 
        }
      }
    }
	stage('Build_code') {      
      steps {
        container(name: 'maven') {                    
                sh """
					mvn -Dmaven.test.failure.ignore=true install
					mkdir kaniko
					cp Dockerfile target/*.war kaniko
					tar -czvf app1.tar.gz kaniko
					gcloud auth activate-service-account pubsub@kmsh-250102.iam.gserviceaccount.com --key-file=service-account.json --project=kmsh-250102
					gsutil cp app1.tar.gz gs://kaniko-bucketsk/
					"""	      
        }
      }
    }
	stage('Build with Kaniko and push to gcr') {
      environment {
        PATH = "/busybox:/kaniko:$PATH"
      }
      steps {
        container(name: 'kaniko', shell: '/busybox/sh') {                    
          sh '''#!/busybox/sh
            /kaniko/executor --context=gs://kaniko-bucketsk/app1.tar.gz --dockerfile=kaniko/Dockerfile --destination=gcr.io/kmsh-250102/app1:$BUILD_NUMBER
          '''
        }
      }
    }
	stage('Deploy on dev') {
	 environment {
        environment = "dev"
		url = "http://35.239.92.56:80/"
      }	
      steps {
        container(name: 'maven', shell: '/bin/bash') {
				git branch: 'master',
				credentialsId: 'github',
				url: 'https://github.com/satishrawat/pipeline.git'
				sh """
					sed -i s/app1/app1:$BUILD_NUMBER/g deployment/app1-deployment.yaml
					gcloud container clusters get-credentials standard-cluster-1 --zone us-central1-a --project kmsh-250102
					kubectl apply -f deployment/app1-deployment.yaml -n $environment
					bash smoke.sh
					"""													      
        }
      }
    }
	stage('Deploy on qa') {
     environment {
        environment = "qa"
		url = "http://34.70.233.225/"
      }	
      steps {
        container(name: 'maven', shell: '/bin/bash') {                    
                sh """					
					kubectl apply -f deployment/app1-deployment.yaml -n $environment
					bash smoke.sh
					"""									      
					
        }
      }
    }
  }
}
