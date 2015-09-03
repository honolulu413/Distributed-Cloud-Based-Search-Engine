package servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;

import model.YelpAPIParam;
import webService.YelpAPI;


/**
 * Servlet implementation class YelpServlet
 */

public class YelpServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public YelpServlet() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		//System.out.println("doGet called");
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		String searchTerm = request.getParameter("term");
	//	System.out.println("term:"+searchTerm);
		YelpAPI yelpAPI = new YelpAPI(YelpAPIParam.CONSUMER_KEY,
				YelpAPIParam.CONSUMER_SECRET, YelpAPIParam.TOKEN,
				YelpAPIParam.TOKEN_SECRET);
		JSONArray array = yelpAPI.queryAPI(yelpAPI, searchTerm,"San Francisco, CA");
		//System.out.println(array);
		PrintWriter out = response.getWriter();
		//out.print(array);
		out.write(array.toString());
		out.flush();	
		out.close();
	}

}
