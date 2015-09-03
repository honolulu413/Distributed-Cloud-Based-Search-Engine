package imageNode;

import imageDB.ImageDBWrapper;
import imageDB.imageOccurence;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ImageNodeServlet extends HttpServlet {
	String DBdirec = "/home/bitnami/ImageDB";
	ImageDBWrapper db;

	@Override
	public void init(ServletConfig config) {
		db = new ImageDBWrapper(DBdirec);
		db.setup();
	}

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		response.setContentType("text/html");
		HashSet<String> urlCollection = new HashSet<String>();
		if (request.getParameter("word") == null) return;
		String[] queryWords = request.getParameter("word").split("\\s");
		for (String word: queryWords) {
			imageOccurence tmp = db.getImageIndex(word);
			if (tmp != null) {
				ArrayList<String> urls = tmp.getUrls();
				for (String url: urls) {
					if (!urlCollection.contains(url)) {
						urlCollection.add(url);
					}
				}
			}
		}
		PrintWriter out = response.getWriter();
		ArrayList<String> res = new ArrayList<String>();
		for (String url: urlCollection) {
			res.add(url);
			if (res.size() == 60) break;
		}
		out.println(urlCollection.size() + "\t" + res);
	}

	@Override
	public void destroy() {
		db.close();
	}
}
