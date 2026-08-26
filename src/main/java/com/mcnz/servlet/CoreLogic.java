package com.mcnz.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class CoreLogic {
	
	protected static void implementLogic(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		System.out.println("In play the game doGet");
	
		String guess = request.getParameter("guess");
		Integer magicNumber = null;
		if (guess == null) {
			response.getWriter().append(WebpageFactory.landingPage());
			request.getSession().invalidate();
			return;
		}
		else {
			String textToDisplay = null;
			Object magicNumberString = request.getSession().getAttribute("magicnumber");
			if (magicNumberString!=null) {
				magicNumber = Integer.parseInt(magicNumberString.toString());
				magicNumber = Math.abs(magicNumber);
				System.out.println("Just calculated number " + magicNumber);
			} else {
				magicNumber = ((new java.util.Random().nextInt() % 10) +1);
				request.getSession().setAttribute("magicnumber", "" + magicNumber);
			}
			
			if (Integer.parseInt(guess)>magicNumber) {
				textToDisplay = WebpageFactory.landingPage().replace("The gameplay results will appear here.", "Guess Lower!");
			}
			if (Integer.parseInt(guess)<magicNumber) {
				textToDisplay = WebpageFactory.landingPage().replace("The gameplay results will appear here.", "Guess Higher!");
			}
			if (Integer.parseInt(guess)==magicNumber) {
				textToDisplay = WebpageFactory.landingPage().replace("The gameplay results will appear here.", "You got it right! It was: " + magicNumber + ".");
			}
			response.getWriter().append(textToDisplay);
		}
		System.out.println("The magic number is: " + magicNumber);
		System.out.println("The guess is: " + guess);
	}

}
