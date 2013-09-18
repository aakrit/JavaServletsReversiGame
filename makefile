all: Servlet.class Client.class tomcat

Servlet.class:
	javac -cp /usr/local/Cellar/tomcat/7.0.42/libexec/lib/servlet-api.jar hw3/Servlet.java hw3/ReversiGame.java hw3/Move.java hw3/Player.java	

Client.class:
	javac -cp /usr/local/Cellar/tomcat/7.0.42/libexec/lib/servlet-api.jar hw3_client/Client.java

start:
	- catalina stop
	catalina start

tomcat:
	cp hw3/*.class /usr/local/Cellar/tomcat/7.0.42/libexec/webapps/ROOT/WEB-INF/classes/hw3	

clean:
	rm hw3/*.class hw3_client/*.class 
