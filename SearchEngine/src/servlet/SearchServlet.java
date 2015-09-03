package servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import model.DB;
import model.SearchResult;
import model.SpellingCorrector;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * Servlet implementation class SearchServlet
 */

public class SearchServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
//	String hostName = "localhost:8080/Master";
	String[] hostNameFinal = {"52.5.43.209/Master", "52.5.43.209/ImageNode"};
	

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public SearchServlet() {
		super();
		// TODO Auto-generated constructor stub
	}

	private String spellHelper(String[] originalString) throws IOException {
		HashMap<String, Integer> map = new HashMap<String, Integer>();
		ServletContext cntxt = this.getServletContext();
		String fName = "/wordlist.txt";
		int n = 0;
		InputStream ins = cntxt.getResourceAsStream(fName);
		try {
			if (ins != null) {
				InputStreamReader isr = new InputStreamReader(ins);
				BufferedReader reader = new BufferedReader(isr);
				String word = "";
				while ((word = reader.readLine()) != null) {
					n++;
					map.put(word, 1);
				}
			}
		} finally {

		}
		// System.out.println("n:"+n);

		SpellingCorrector checker = new SpellingCorrector();
		checker.dictionary = map;
		StringBuffer buffer = new StringBuffer();
		// String[] words ={"worry", "appl", "expariment"};

		int wordLength = originalString.length;
		String[] correctWords = new String[wordLength];
		for (int i = 0; i < wordLength - 1; i++) {
			correctWords[i] = checker.correct(originalString[i]);
			// System.out.println(correctWords[i]);
			buffer.append(correctWords[i]);
			buffer.append(" ");
		}
		correctWords[wordLength - 1] = checker
				.correct(originalString[wordLength - 1]);
		buffer.append(correctWords[wordLength - 1]);
		String correctString = buffer.toString();
		return correctString;
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		long startTime = System.currentTimeMillis();

		String searchTerm = request.getParameter("searchTerm");
		searchTerm = searchTerm.toLowerCase();
		System.out.println("searchTerm " + searchTerm);
		String[] duplicated = searchTerm.split("[^0-9A-Za-z]+");

		String searchType = request.getParameter("searchtype");

		int type = 0;
		if (searchType.equals("word")) {
			type = 0;
		} else {
			type = 1;
		}

		StringBuilder originalBuf = new StringBuilder();
		for (int i = 0; i < duplicated.length - 1; i++) {
			originalBuf.append(duplicated[i]);
			originalBuf.append(" ");
		}
		originalBuf.append(duplicated[duplicated.length - 1]);
		String original = originalBuf.toString();

		String corrected = null;
		try {
			corrected = spellHelper(duplicated);
		} catch (Exception e) {
		}

		HashSet<String> set = new HashSet<String>();
		for (String word : duplicated) {
			System.out.println("word is " + word);
			if (!"".equals(word.trim()))
				set.add(word);
		}

		String noDuplicatedTerm = "";
		for (String word : set) {
			noDuplicatedTerm += word + "+";
		}

		noDuplicatedTerm = noDuplicatedTerm.substring(0,
				noDuplicatedTerm.length() - 1);

		System.out.println("noDuplicatedTerm " + noDuplicatedTerm);

		String url = "http://" + hostNameFinal[type] + "/?word=" + noDuplicatedTerm;
		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();

		// optional default is GET
		con.setRequestMethod("GET");
		con.setDoOutput(true);
		con.setDoInput(true);
		// Get Response
		InputStream is = con.getInputStream();

		BufferedReader rd = new BufferedReader(new InputStreamReader(is));
		String res = "";
		String line;
		while ((line = rd.readLine()) != null) {
			res += line;
		}
		rd.close();

		
		
		
		String numFiles = res.split("\t")[0];
		String output = res.split("\t")[1];

		double timeElapsed = (double) (System.currentTimeMillis() - startTime) / 1000.0;
		String time = timeElapsed + " second";
		output = output.replaceAll("\\[|\\]", "");
		System.out.println("timeElapsed " + timeElapsed);

		// output = output.replaceAll("\\[|\\]", "");

		ArrayList<String> urlList = new ArrayList<String>();
		String[] arr = output.split(",");
		for (int i = 0; i < arr.length; i++) {
			arr[i] = arr[i].trim();
			String[] temp = arr[i].split(" ");
			String decoded = java.net.URLDecoder.decode(temp[0], "UTF-8");

			urlList.add(decoded);
		}
		System.out.println(urlList);

		/*
		 * for (String urlInfo : ) { urlList.add(urlInfo.split("\\s")[0]);
		 * System.out.println(urlInfo.split("\\s")[0]); }
		 */

		JSONObject jObject = new JSONObject();
		JSONArray jArray = null;

		// int size = 60;
		// for(int i=0;i<size;i++){
		// String url = "url"+i;
		// urlList.add(url);
		// }
		DB db = new DB();
		ArrayList<SearchResult> resultList;
		if (type == 0) {
			resultList = db.getResultWithTitlte(urlList);
		} else {
			resultList = db.getImgWithTitlte(urlList);
		}

		/*
		 * ArrayList<SearchResult> resultList = new ArrayList<SearchResult>();
		 * for(int i=0;i<urlList.size();i++){ String title = "title"+i; String
		 * urlStr = urlList.get(i); SearchResult sr = new
		 * SearchResult(title,urlStr); resultList.add(sr); }
		 */
		System.out.println(resultList);

		try {
			jArray = new JSONArray();
			for (int i = 0; i < resultList.size(); i++) {
				JSONObject valJSON = new JSONObject();
				valJSON.put("name", resultList.get(i).getTitle());
				valJSON.put("url", resultList.get(i).getUrl());
				jArray.add(valJSON);
			}
			// jObject.put("result", jArray);
			// System.out.println(jObject);
			// System.out.println(jArray);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		request.setCharacterEncoding("utf-8");
		request.setAttribute("googleResult", jArray.toString());
		request.setAttribute("term", searchTerm);// 为request对象添加参数
		request.setAttribute("time", time);
		request.setAttribute("numFiles", numFiles);
		request.setAttribute("original", original);
		request.setAttribute("corrected", corrected);

		RequestDispatcher dispatcher = request
				.getRequestDispatcher("result.jsp"); // 使用req对象获取RequestDispatcher对象
		dispatcher.forward(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		// System.out.println("doPost()");
	}

}
