FROM ubuntu:xenial
MAINTAINER  Naveen <naveen@xyz.com>

#get updates
RUN apt-get update

##instal git
RUN apt-get install git -y

##install wget#
RUN apt install wget -y

#install curl#
RUN apt-get install curl -y

#install jq

RUN apt install jq -y

##Install unzip
RUN apt install unzip -y

##install jdk8
RUN apt-get -y install software-properties-common
RUN add-apt-repository -y ppa:webupd8team/java
RUN echo debconf shared/accepted-oracle-license-v1-1 select true | debconf-set-selections
RUN echo debconf shared/accepted-oracle-license-v1-1 seen true | debconf-set-selections
RUN apt install openjdk-8-jdk -y
ENV JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64


#install maven#
WORKDIR /opt/
RUN wget http://mirror.olnevhost.net/pub/apache/maven/binaries/apache-maven-3.2.2-bin.tar.gz
RUN tar -xvzf apache-maven-3.2.2-bin.tar.gz && rm apache-maven-3.2.2-bin.tar.gz
RUN mv apache-maven-3.2.2 maven
ENV M2_HOME=/opt/maven
ENV PATH=${M2_HOME}/bin:${PATH}
RUN mvn --version


##Install terraform

RUN wget https://releases.hashicorp.com/terraform/0.12.8/terraform_0.12.8_linux_amd64.zip
RUN unzip terraform_0.12.8_linux_amd64.zip && rm terraform_0.12.8_linux_amd64.zip
RUN mv terraform /usr/local/bin/

##Install helm#
RUN wget https://get.helm.sh/helm-v2.14.3-linux-amd64.tar.gz
RUN tar -xvf helm-v2.14.3-linux-amd64.tar.gz && rm helm-v2.14.3-linux-amd64.tar.gz
RUN mv linux-amd64/* /usr/local/bin/



##Install Golang
RUN wget https://dl.google.com/go/go1.13.linux-amd64.tar.gz
RUN tar -xvf go1.13.linux-amd64.tar.gz && rm go1.13.linux-amd64.tar.gz
RUN mv go /usr/local
ENV GOROOT=/usr/local/go
RUN mkdir /opt/goproject
ENV GOPATH=/opt/goproject
ENV PATH=$GOPATH/bin:$GOROOT/bin:$PATH
RUN go version

##install gradle
RUN wget https://downloads.gradle-dn.com/distributions/gradle-5.6.2-all.zip
RUN mkdir -p /opt/gradle
RUN unzip -d /opt/gradle gradle-5.6.2-all.zip
ENV PATH=$PATH:/opt/gradle/gradle-5.6.2/bin
RUN gradle -v

##install kubectl ##
RUN curl -LO https://storage.googleapis.com/kubernetes-release/release/`curl -s https://storage.googleapis.com/kubernetes-release/release/stable.txt`/bin/linux/amd64/kubectl
RUN chmod +x ./kubectl
RUN mv ./kubectl /usr/local/bin/kubectl



##install gcloud
RUN echo "deb [signed-by=/usr/share/keyrings/cloud.google.gpg] http://packages.cloud.google.com/apt cloud-sdk main" | tee -a /etc/apt/sources.list.d/google-cloud-sdk.list && curl https://packages.cloud.google.com/apt/doc/apt-key.gpg | apt-key --keyring /usr/share/keyrings/cloud.google.gpg  add - && apt-get update -y && apt-get install google-cloud-sdk -y



