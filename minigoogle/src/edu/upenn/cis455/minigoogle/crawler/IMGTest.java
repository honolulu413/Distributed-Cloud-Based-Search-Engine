package edu.upenn.cis455.minigoogle.crawler;

import java.io.IOException;
import java.net.URLEncoder;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class IMGTest {
	public static void main(String[] args) throws IOException{
		Document doc = Jsoup.connect("http://www.epfl.ch").get();
		Elements links = doc.getElementsByTag("img");
		for(Element e: links){
			String url =e.attr("abs:src");
			int start = url.lastIndexOf("/")+1;
			int end = url.lastIndexOf("?");
			String fileName ="";
			if(end == -1){
				fileName = url.substring(start);
			}
			else{
				fileName = url.substring(start, end);
			}
			
			String ele = doc.title()+" "+ fileName+" "+e.attr("anchor")+" "+e.attr("alt");
			ele.replaceAll("\t", " ");
			String[] text = ele.split("[^a-zA-Z0-9']");
			
			
			String content = URLEncoder.encode(url)+"\t";
			for(String s: text){
				String tmp = s.trim();
				if(tmp.equals("")) continue;
				content+=tmp+" ";
			}
			System.out.println(content);
			
			/*System.out.println(url);
			System.out.println(doc.title());
			System.out.println(fileName);
			
			String anchor = e.text();
			String alt = e.attr("alt");
			System.out.println("anchor: "+ anchor+ " alt: "+alt);
			System.out.println();
		*/
		}
	}
}
