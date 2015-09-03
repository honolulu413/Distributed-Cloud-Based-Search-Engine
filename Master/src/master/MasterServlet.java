package master;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import pageRankDB.PageRankWrapper;

public class MasterServlet extends HttpServlet {
	JSONParser parser;
	String[] hostNames = { "localhost:8081/SingleNode",
			"localhost:8082/SingleNode" };
	HashMap<String, ArrayList<WordInURL>> docInfo;
	HashMap<String, Double> wordInfo;
	 String DBdirec = "/home/bitnami/PageDB";
//	String DBdirec = "/home/joseph/Desktop/pageRank";
	PageRankWrapper db;

	HashMap<Integer, ArrayList<String>> hostNameMap;
	String[] workerIP = { "52.5.89.92", "52.7.68.51", "52.1.190.45",
			"52.5.236.146", "52.5.87.99", "52.5.203.4" };

	static int numberOfNodes = 3;
	static BigInteger total = new BigInteger(
			"ffffffffffffffffffffffffffffffffffffffff", 16);
	static BigInteger unit = total.divide(BigInteger.valueOf(numberOfNodes));

	@Override
	public void init(ServletConfig config) {
		hostNameMap = new HashMap<Integer, ArrayList<String>>();
		parser = new JSONParser();
		// docInfo = new HashMap<String, ArrayList<WordInURL>>();
		// wordInfo = new HashMap<String, Double>();
		db = new PageRankWrapper(DBdirec);
		db.setup();
		ArrayList<String> ip = new ArrayList<String>();
		ip.add(workerIP[0]);
		ip.add(workerIP[1]);
		hostNameMap.put(0, ip);

		ip = new ArrayList<String>();
		ip.add(workerIP[2]);
		ip.add(workerIP[3]);
		hostNameMap.put(1, ip);

		ip = new ArrayList<String>();
		ip.add(workerIP[4]);
		ip.add(workerIP[5]);
		hostNameMap.put(2, ip);

	}

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) {
		docInfo = new HashMap<String, ArrayList<WordInURL>>();
		wordInfo = new HashMap<String, Double>();
		response.setContentType("text/html");
		PrintWriter searchEngineout = null;
		try {
			searchEngineout = response.getWriter();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		System.out.println(request.getParameter("word"));

		String[] queryWords = request.getParameter("word").split("\\s");

		for (int i = 0; i < queryWords.length; i++) {
			String query = queryWords[i];
			int partition = shuffleHelper(query);

			int index = 0;
//			try {
//				masterToWorker(query, hostNames[i]);
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
			while (true) {
				try {
					masterToWorker(query, hostNameMap.get(partition).get(index));
					break;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					index++;
					if (index == 2) {
						break;
					}
				}
			}

		}

		ArrayList<FinalDocument> docRank = new ArrayList<FinalDocument>();

		for (String key : docInfo.keySet()) {

			ArrayList<WordInURL> wordList = docInfo.get(key);

			if (wordList.size() == wordInfo.size()
					|| wordList.size() == wordInfo.size() - 1) {

				double rank = 0.15;

				if (db.getPage(key) != null) {
					rank = db.getPage(key).getPageRank();
				}
				double scoreSum = 0;

				for (WordInURL info : wordList) {
					scoreSum += info.getScore();
				}
				// System.out.println(scoreSum + " " + rank + " " + key);

				if (wordList.size() == wordInfo.size()) {
					ArrayList<ArrayList<Integer>> phraseList = new ArrayList<ArrayList<Integer>>();
					for (String word : queryWords) {
						for (WordInURL info : wordList) {
							if (word.equals(info.getWord())) {
								phraseList.add(info.getPositions());
								break;
							}
						}
					}
					if (isMatch(phraseList))
						scoreSum *= wordList.size();

				}

				docRank.add(new FinalDocument(key, scoreSum * rank));
			}

		}
		Collections.sort(docRank, new DocComparator());
		int numFiles = docRank.size();

		if (docRank.size() > 60) {
			List<FinalDocument> docRankTemp = (List<FinalDocument>) docRank
					.subList(0, 60);
			searchEngineout.println(numFiles + "\t" + docRankTemp);
			searchEngineout.flush();
		} else {
			searchEngineout.println(numFiles + "\t" + docRank);
			searchEngineout.flush();

		}

	}

	public static boolean isMatch(ArrayList<ArrayList<Integer>> data) {

		int match = 1;
		for (int i = 0; i < data.get(0).size(); i++) {
			int start = data.get(0).get(i);
			for (int j = 0; j < data.size(); j++) {
				if (!data.get(j).contains(start + j))
					break;
				match++;
				if (match == data.size() + 1)
					return true;
			}
			match = 0;
		}
		return false;
	}

	private void masterToWorker(String query, String hostName)
			throws IOException {

		// int partition = shuffleHelper(query);
		String url = "http://" + hostName + "/SingleNode/?word=" + query;
		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();

		// optional default is GET
		con.setRequestMethod("GET");
		con.setDoOutput(true);

		con.setDoInput(true);

		// OutputStream os = con.getOutputStream();
		// Get Response

		int resCode = con.getResponseCode();
		System.out.println("statusCode " + resCode);

		InputStream is = con.getInputStream();

		BufferedReader rd = new BufferedReader(new InputStreamReader(is));
		String output = "";
		String line;
		while ((line = rd.readLine()) != null) {
			output += line;
		}
		rd.close();
		// out.println(output);

		JSONObject jsonObject = null;
		try {
			jsonObject = (JSONObject) parser.parse(output);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		String word = jsonObject.get("word").toString();

		if (!wordInfo.containsKey(word)) {

			if (jsonObject.get("idf") != null
					&& jsonObject.get("doclist") != null) {

				Double idf = Double.parseDouble(jsonObject.get("idf")
						.toString());
				wordInfo.put(word, idf);

				String docListTemp = jsonObject.get("doclist").toString()
						.replaceAll("\"|\\[|\\]", "");
				String[] docList = docListTemp.split(",");

				for (String doc : docList) {
					String[] docSplit = doc.split("\\s", 3);
					String urlInfo = docSplit[0];
					double tf = Double.parseDouble(docSplit[1]);
					String positions = docSplit[2];

					WordInURL wordInURL = new WordInURL(word, tf, idf,
							positions);

					if (!docInfo.containsKey(urlInfo)) {
						ArrayList<WordInURL> temp = new ArrayList<WordInURL>();
						temp.add(wordInURL);
						docInfo.put(urlInfo, temp);
					} else {
						docInfo.get(urlInfo).add(wordInURL);
					}

				}

			}
		}

	}

	private static int shuffleHelper(String key) {
		String s = "";
		String realKey = key.toString();
		try {
			MessageDigest crypt = MessageDigest.getInstance("SHA-1");
			crypt.reset();
			crypt.update(realKey.getBytes("UTF-8"));
			s = byteArrayToHexString(crypt.digest());
		} catch (Exception e) {
			e.printStackTrace();
		}

		BigInteger keyValue = new BigInteger(s, 16);
		int n = (keyValue.divide(unit)).intValue();
		return n;
	}

	private static String byteArrayToHexString(byte[] b) {
		String result = "";
		for (int i = 0; i < b.length; i++) {
			result += Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1);
		}
		return result;
	}

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		response.setContentType("text/html");

		PrintWriter out = response.getWriter();

		String res = request.getParameter("word");
		out.println(res);
	}

	@Override
	public void destroy() {
		db.close();
	}
}
