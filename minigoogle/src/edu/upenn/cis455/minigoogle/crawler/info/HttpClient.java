package edu.upenn.cis455.minigoogle.crawler.info;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.prefs.Preferences;

import javax.net.ssl.HttpsURLConnection;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.w3c.tidy.Tidy;
import org.xml.sax.SAXException;


public class HttpClient {
	String url;
	boolean valid;
	PrintWriter out;
	InputStream in;
	int contentLength;
	String contentType;
	String body;
	long lastModified;
	String status;
	HttpsURLConnection con;
	URL myurl;

	public HttpClient(String webUrl) {
		valid = true;
		this.url = webUrl;

		if (url.startsWith("https")) {
			try {
				myurl = new URL(url);
			} catch (MalformedURLException e) {
				valid = false;
			}
			try {
				con = (HttpsURLConnection) myurl.openConnection();
			} catch (Exception e) {
				valid = false;
			}
			return;
		}
		

	}

	public void sendHead() throws IOException  {
		try{
			if (url.startsWith("https")) {
				con.setRequestMethod("HEAD");
				con.setRequestProperty("User-Agent", "cis455crawler");
				status = "" + con.getResponseCode();
				contentType = getValue(con.getContentType());
				contentLength = con.getContentLength();		
				lastModified = con.getLastModified();
				

				if(status==null||contentType==null)
					return;
				if(con.getResponseCode() >= 400 )
					return;
				
				con.disconnect();
			}
			else if(url.startsWith("http")){
				URL hpUrl = new URL(url);
				HttpURLConnection hpCon = (HttpURLConnection) hpUrl.openConnection();
				hpCon.setRequestMethod("HEAD");
				hpCon.setRequestProperty("User-Agent", "cis455crawler");
				if(hpCon.getContentType() == null){
					status ="500";
					return;
				}
				contentType = getValue(hpCon.getContentType());
				contentLength = hpCon.getContentLength();
				status = "" + hpCon.getResponseCode();
				
				if(status==null||contentType==null)
					return;
				if(hpCon.getResponseCode() >= 400 )
					return;
				lastModified = hpCon.getLastModified();
				hpCon.disconnect();	
			}
			return;
		}catch(Exception e){
			return;
		}
	}

	public String getStatus() {
		return status;
	}

	public void sendGet() throws IOException {
		try{
			if (url.startsWith("https")) {
				con = (HttpsURLConnection) myurl.openConnection();
				con.setRequestProperty("User-Agent", "cis455crawler");
				in = con.getInputStream();
			} else {
				URL hpUrl = new URL(url);
				HttpURLConnection hpCon = (HttpURLConnection) hpUrl.openConnection();
				hpCon.setRequestMethod("GET");
				hpCon.setRequestProperty("User-Agent", "cis455crawler");
				in=hpCon.getInputStream();
			}	

			if (contentLength > 0) {
				StringBuilder sb = new StringBuilder();
				for (int i = 0; i < contentLength; i++) {
					sb.append((char) in.read());
				}
				body = sb.toString();
			} else {
				StringBuilder sb = new StringBuilder();
				int a;
				while ((a = in.read()) != -1) {
					contentLength++;
					//System.out.println("aaaaaa");
					sb.append((char) a);
				}
				body = sb.toString();
			}
		}catch(IOException e){
			return;
		}
		
	}

	private String getValue(String s) {
		int start = s.indexOf(":");
		int end = s.indexOf(";");
		if (end == -1)
			return s.substring(start + 1).trim();
		return s.substring(start + 1, end).trim();
	}

	public long getLastModified() {
		return lastModified;
	}

	public String getContentType() {
		return contentType;
	}

	public int getContentLength() {
		return contentLength;
	}

	public boolean isUrlValid() {
		return valid;
	}

	public String getBody() {
		return body;
	}

	public InputStream getBodyInputStream() throws UnsupportedEncodingException {
		/*if (contentType.equals("text/html")) {
			Tidy tidy = new Tidy();
			tidy.setXHTML(true);
			tidy.setDocType("omit");
			ByteArrayInputStream inputStream = new ByteArrayInputStream(
					body.getBytes("UTF-8"));
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			tidy.parseDOM(inputStream, outputStream);
			return new ByteArrayInputStream(outputStream.toString("UTF-8").getBytes());
		} */
		return new ByteArrayInputStream(body.getBytes());
	}

	private String readOneLine() throws IOException {
		StringBuilder sb = new StringBuilder();
		char a;
		while ((a = (char) in.read()) != '\n') {
			sb.append(a);
		}
		return sb.toString().trim();
	}

	public static void main(String args[]) throws IOException {
		Preferences root = Preferences.userRoot();
		root.putBoolean("run", true);
		System.out.println(root.getBoolean("run", false));
	}

}
