package com.mcnz.servlet;

public class WebpageFactory {

	public static String landingPage() {
		String page = "<html>\r\n"
				+ "   <head>\r\n"
				+ "      <title>The Number Guesser Game</title>\r\n"
				+ "      <link href=\"https://cdn.jsdelivr.net/npm/bootstrap@5.0.2/dist/css/bootstrap.min.css\" rel=\"stylesheet\" integrity=\"sha384-EVSTQN3/azprG1Anm3QDgpJLIm9Nao0Yz1ztcQTwFspd3yD65VohhpuuCOmLASjC\" crossorigin=\"anonymous\">"
				+ "   </head>\r\n"
				+ "   <body style=\"padding:50\">\r\n"
				+ "      <h3><font color=\"navy\">Would you like to play a game?</font></h3>\r\n"
				+ "      <p>Pick a number between <b>0</b> and <b>10</b>?\r\n"
				+ "      <p/>\r\n"
				+ "         <a href=\"playthegame?guess=1\" class=\"btn btn-primary\"> 1 </a> &nbsp;\r\n"
				+ "         <a href=\"playthegame?guess=2\" class=\"btn btn-danger\"> 2 </a> &nbsp;\r\n"
				+ "         <a href=\"playthegame?guess=3\" class=\"btn btn-success\"> 3 </a> &nbsp;\r\n"
				+ "         <a href=\"playthegame?guess=4\" class=\"btn btn-warning\"> 4 </a> &nbsp;\r\n"
				+ "         <a href=\"playthegame?guess=5\" class=\"btn btn-info\"> 5 </a> &nbsp;\r\n"
				+ "         <a href=\"playthegame?guess=6\" class=\"btn btn-dark\"> 6 </a> &nbsp;\r\n"
				+ "         <a href=\"playthegame?guess=7\" class=\"btn btn-warning\"> 7 </a> &nbsp;\r\n"
				+ "         <a href=\"playthegame?guess=8\" class=\"btn btn-light\"> 8 </a> &nbsp;\r\n"
				+ "         <a href=\"playthegame?guess=9\" class=\"btn btn-primary\"> 9 </a> &nbsp;\r\n"
				+ "         <a href=\"playthegame?guess=10\" class=\"btn btn-danger\">10</a> &nbsp;\r\n"
				+ "      <div id=\"results\">The gameplay results will appear here.</div>\r\n"
				+ "   </body>\r\n"
				+ "</html>";
		return page;
	}
	

}
