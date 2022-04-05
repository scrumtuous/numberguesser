package com.mcnz.servlet;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
To build an executable JAR file, run the following command in the same folder as the POM:

mvn clean install tomcat7:exec-war-only

NOTE: Before you run the executable jar, make sure Jenkins isn't running on port 8080. 
Also, make sure any other Tomcat instances you have started are stopped.
If port 8080 is in use, the JAR file will not start.

To run the executable file, run the following command in the same folder as the executable.jar file, which should be the \target subfolder:

java -jar executable.jar

The URL to get to the page after the JAR is run is:

http://localhost:8080/playthegame

NOTE: in eclipse the URL will likely be:

http://localhost:8080/numberguesser/playthegame

 */
@WebServlet(name = "playthegame", urlPatterns = { "/playthegame" })
public class NumberGuesserGame extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		System.out.println("In play the game doGet");
		CoreLogic.implementLogic(request, response);
	}
	
	public static String getWebpage() {
		return WebpageFactory.landingPage();
	}
}