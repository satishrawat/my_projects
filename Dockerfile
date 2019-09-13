FROM java:8
WORKDIR /
ADD kaniko/*.jar HelloWorld.jar
EXPOSE 8080
CMD java -jar HelloWorld.jar
