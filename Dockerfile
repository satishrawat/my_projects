FROM java:8
WORKDIR /
ADD kaniko/*.war HelloWorld.war
CMD java -jar HelloWorld.war
