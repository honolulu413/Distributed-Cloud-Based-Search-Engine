package mergeFile;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class MergeFile {
	public static void main(String[] args) throws IOException {
		String direc = args[0];
		String outputName = args[1];
		File files = new File(direc);
		if (!files.isDirectory()) {
			System.out.println("directory not valid");
			return;
		}
		
		FileWriter fw = new FileWriter(outputName, true);		
		for (File file: files.listFiles()) {
			HashMap<String, ArrayList<Occurence>> wordOccurence = new HashMap<String, ArrayList<Occurence>>();

			String url = file.getName();
			String hostName = new URL(URLDecoder.decode(url, "UTF-8")).getHost();
			String html = new String(Files.readAllBytes(file.toPath()));
			
			Document doc = Jsoup.parse(html, url);

			int position = 1;

			Elements metaData = doc.select("meta[name]");
			if (metaData != null) {
				String metaContent = "";
				for (Element data : metaData) {
					if ("keywords".equals(data.attr("name"))
							|| "description".equals(data.attr("name")))
						metaContent += data.attr("content") + " ";
				}

				if (!"".equals(metaContent)) {
					String[] metaTokens = metaContent.split("[^a-zA-Z0-9]+");
					for (String word : metaTokens) {
						word = word.trim();
						if (!"".equals(word)) {
							helper(url, wordOccurence, word, 2, position);
							position++;
						}
					}
				}

			}

			// for title
			String title = doc.title();

			if (!"".equals(title)) {
				String[] titleTokens = title.split("[^a-zA-Z0-9]+");
				for (String word : titleTokens) {
					word = word.trim();
					if (!"".equals(word)) {
						helper(url, wordOccurence, word, 1, position);
						position++;
					}
				}
			}

			// for body
			Element bodyData = doc.body();
			if (bodyData != null) {
				String body = bodyData.text();

				if (!"".equals(body)) {
					String[] bodyTokens = body.split("[^a-zA-Z0-9]+");
					for (String word : bodyTokens) {
						word = word.trim();
						if (!"".equals(word)) {
							helper(url, wordOccurence, word, 3, position);
							position++;
						}
					}

				}

			}

			// for anchor
			Elements links = doc.select("a[href]");
			if (links != null) {
				int numOutLinks = 0;
				ArrayList<String> outLinks = new ArrayList<String>();
				for (Element link : links) {
					String anchor = link.text().trim();
					String outLink = link.attr("abs:href").trim();
					if (outLink.endsWith("/")) {
						outLink = outLink.substring(0, outLink.length() - 1);
					}

					boolean ifEncoded = false;

					if (!"".equals(outLink)) {
						try {
							String outHostName = new URL(outLink).getHost();
							if (!hostName.equals(outHostName)) {
								numOutLinks++;
								outLink = URLEncoder.encode(outLink, "UTF-8");
								ifEncoded = true;
								outLinks.add(outLink);
							}
						} catch (Exception e) {
						}

						if (!"".equals(anchor)) {
							// for page rank
							String[] anchorTokens = anchor
									.split("[^a-zA-Z0-9]+");
							if (!ifEncoded)
								outLink = URLEncoder.encode(outLink, "UTF-8");

							for (String word : anchorTokens) {
								word = word.trim();
								if (!"".equals(word)) {
									helper(outLink, wordOccurence, word, 0, 0);
									position++;
								}
							}

						}
					}

				}

				for (String outLink : outLinks) {
					fw.write("Link" + url + " " + numOutLinks);
					fw.write("\t");
					fw.write(outLink + "\n");
				}
			}

			for (String word : wordOccurence.keySet()) {
				for (Occurence ocr : wordOccurence.get(word)) {
					fw.write("Url" + ocr.url);
					String output = word + "," + ocr.importance + ","
							+ ocr.position;
					fw.write("\t");
					fw.write(output + "\n");
				}

			}
			
		}
		fw.close();
		
	}
	
	private static boolean isAllCapital(String s) {
		for (int i = 0; i < s.length(); i++) {
			if (!Character.isUpperCase(s.charAt(i)))
				return false;
		}
		return true;
	}

	private static boolean isAllLetter(String s) {
		for (int i = 0; i < s.length(); i++) {
			if (!Character.isLetter(s.charAt(i)))
				return false;
		}
		return true;
	}

	private static void helper(String url,
			HashMap<String, ArrayList<Occurence>> wordOccurence, String word,
			int type, int position) {

		int isCapital = 0;
		word = word.replaceAll("[^A-Za-z0-9]*$|^[^A-Za-z0-9]*", "");
		if (isAllLetter(word)) {
			if (isAllCapital(word)) {
				isCapital = 1;
			}
			// word = Stemmer.getString(word);
		}

		word = word.toLowerCase().toString();

		if (!Utility.stopList.contains(word)) {
			if (isAllLetter(word)) {
				word = Stemmer.getString(word);
			}
			if (!wordOccurence.containsKey(word)) {
				ArrayList<Occurence> tempList = new ArrayList<Occurence>();
				tempList.add(new Occurence(url, isCapital, type, position));
				wordOccurence.put(word, tempList);
			} else {
				wordOccurence.get(word).add(
						new Occurence(url, isCapital, type, position));
			}
		}

	}
}
