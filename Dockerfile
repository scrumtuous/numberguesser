FROM tomcat:9.0
COPY /target/numberguesser.war /usr/local/tomcat/webapps/
