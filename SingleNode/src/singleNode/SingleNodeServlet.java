package singleNode;

import java.io.DataOutputStream;
import java.io.IOException;

import indexDB.*;

import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.*;
import org.json.simple.parser.JSONParser;

public class SingleNodeServlet extends HttpServlet {
	String DBdirec = "/home/joseph/Desktop/wordIndex";
//	String DBdirec = "/home/bitnami/InvertedDB";

	IndexDBWrapper db;
	JSONParser parser;
	String hostName = "localhost:8080";

	@Override
	public void init(ServletConfig config) {
		db = new IndexDBWrapper(DBdirec);
		db.setup();
		parser = new JSONParser();
	}

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		response.setContentType("text/html");
		System.out.println(request);
		
		String word = request.getParameter("word");
		if (word != null) {
			WordOccurence wordOccurence = db.getWordIndex(word);
			JSONObject obj = new JSONObject();
			obj.put("word", word);

			if (wordOccurence != null) {
				obj.put("idf", wordOccurence.getIdf());
				JSONArray list = new JSONArray();
				for (UrlOccurence url : wordOccurence.getUrlOccurences()) {
					list.add(url.toString());
				}
				obj.put("doclist", list);
			}

			JSONArray array = new JSONArray();
			array.add(obj);
			array.add(obj);
			 PrintWriter out = response.getWriter();


//			 out.println(array.toString());
			 out.println(obj.toJSONString());
			 out.flush();

		}
	}

	@Override
	public void destroy() {
		db.close();
	}
}
