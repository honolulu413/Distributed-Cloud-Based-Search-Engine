package edu.upenn.cis455.minigoogle.crawler;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.net.Socket;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicLong;
import java.util.prefs.Preferences;

import edu.upenn.cis455.minigoogle.crawler.info.RobotsTxtInfo;
import edu.upenn.cis455.minigoogle.storage.DBRDS;

import org.jsoup.Jsoup;
import org.jsoup.nodes.*;
import org.jsoup.select.Elements;

import edu.upenn.cis455.minigoogle.crawler.info.*;

public class XPathCrawler implements Runnable {
	HashMap<String, RobotsTxtInfo> robots;
	HashMap<String, Long> hostCrawledTimes;
	String startUrl;
	int maxNumber;
	double maxSize;
	int crawled;
	String currentUrl;
	boolean remoteControled;
	Boolean readyPush;
	AtomicLong count;
	Object lock;
	ArrayList<String> frontier;
	ArrayList<String> workerIP;
	int numWorker;
	String selfIP;
	int selfID;
	final BigInteger MAX = new BigInteger(
			"FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF", 16);
	BigInteger basket;

	DBRDS rds;
	String outputDir;
	String outIMGDIr;
	File outIMG;
	private long maxCount;
	HashMap<String,Integer> hostCount;
	HashSet<String> seedHost;

	public XPathCrawler(String startUrl, double maxSize, int maxNumber,
			Boolean readyPush, AtomicLong count, Object lock,
			ArrayList<String> frontier, ArrayList<String> workerIP,
			String selfIP, String outputDir,long maxCount, String outIMG,HashSet<String> seedHost,HashMap<String,Integer> hostCount) throws ClassNotFoundException {

		robots = new HashMap<String, RobotsTxtInfo>();
		hostCrawledTimes = new HashMap<String, Long>();
		this.startUrl = startUrl;
		this.maxSize = maxSize;
		this.maxNumber = maxNumber;
		this.readyPush = readyPush;
		this.count = count;
		this.lock = lock;
		this.frontier = frontier;
		this.workerIP = workerIP;
		this.numWorker = workerIP.size();
		this.selfIP = selfIP;
		this.selfID = workerIP.indexOf(selfIP);
		BigInteger WorkerNo = new BigInteger("" + this.numWorker);
		this.basket = MAX.divide(WorkerNo);
		rds = new DBRDS();
		this.outputDir=outputDir;
		this.maxCount=maxCount;
		this.outIMGDIr=outIMG;
		this.seedHost = seedHost;
		this.hostCount = hostCount;
		System.out.println("seed Host size is "+ seedHost.size());
	}
	

	private void createFile(String url, HttpClient hc, String host) {
		if (!hc.getContentType().equals("text/html"))
			return;
		String body = hc.getBody();



		if(url.endsWith("/")){
			url=url.substring(0,url.length()-1);
		}


		if (!hc.getContentType().equals("text/html"))
			return;

		String urlEncode = URLEncoder.encode(url);

		File file = new File(outputDir + urlEncode);
		if (file.exists() && file.isFile())
			return;

		// MD5 for body check with RDS content

		try {

			Document doc = Jsoup.connect(currentUrl).get();
			String title = doc.title();

			String MD5 = MD5String(body);
			if(rds.checkContent(MD5)){
				rds.insertCrawled(url,title);
				System.out.println("Content Seen");
				return;
			}
			rds.insertContent(MD5);

			System.out.println("Downloading: " + url);
			System.out.println("===================Begin Creating File=================");
			file.createNewFile();
			PrintWriter out = new PrintWriter(file);
			out.print(body);
			// crawled++;
			// System.out.println("Has crawled: "+ crawled);
			out.flush();
			out.close();
			count.incrementAndGet();
			System.out.println("Has crawled: " + count.get());
			rds.insertCrawled(url,title);
			synchronized(hostCount){
				if(hostCount.containsKey(host)){
					hostCount.put(host, 1+hostCount.get(host));
				}
				else{
					hostCount.put(host, 1);
				}
			}

		} catch (Exception e) {
			System.out.println("create new file exception");
			return;
		}


		//rds.insertContent(MDBody);
	}

	private void addLinkstoQueue() throws IOException {

		try {
			Document doc = Jsoup.connect(currentUrl).get();
			Elements imgs = doc.getElementsByTag("img");
			IMGHandler(imgs, doc.title(), this.outIMG);
			Elements links = doc.getElementsByTag("a");
			for (Element link : links) {
				String linkHref = link.attr("abs:href");
				synchronized (frontier) {
					if(!frontier.contains(linkHref) && linkHref.length()<400 && (linkHref.startsWith("http")) && !tooDeep(linkHref))
						frontier.add(linkHref);
				}
			}
		} catch (Exception e) {
		}
	}

	public boolean robotsAllowed(String url, long lastCrawled)
			throws IOException {
		RobotsTxtInfo robot = getRobot(url);
		if (robot == null)
			return true;
		url = trimURL(url);
		String host = new URLInfo(url).getHostName();
		long elapsed = (System.currentTimeMillis() - lastCrawled);
		if (robot.containsUserAgent("cis455crawler")) {
			Double delay = robot.getCrawlDelay("cis455crawler");

			if (delay != null && elapsed < 1000 * delay) {
				synchronized (frontier) {
					frontier.add(currentUrl);
				}
				return false;
			}
			ArrayList<String> allowedLinks = robot
					.getAllowedLinks("cis455crawler");
			if (allowedLinks != null)
				for (String end : allowedLinks) {
					if (isSubDirec(host + end, url))
						return true;
				}
			ArrayList<String> disallowedLinks = robot
					.getDisallowedLinks("cis455crawler");
			if (disallowedLinks != null)
				for (String end : disallowedLinks) {
					if (isSubDirec(host + end, url))
						return false;
				}
		} else if (robot.containsUserAgent("*")) {
			Double delay = robot.getCrawlDelay("*");
			if (delay != null && elapsed < 1000 * delay) {
				synchronized (frontier) {
					frontier.add(currentUrl);
				}
				return false;
			}
			ArrayList<String> allowedLinks = robot.getAllowedLinks("*");
			if (allowedLinks != null)
				for (String end : allowedLinks) {
					if (isSubDirec(host + end, url))
						return true;
				}
			ArrayList<String> disallowedLinks = robot.getDisallowedLinks("*");
			if (disallowedLinks != null)
				for (String end : disallowedLinks) {
					if (isSubDirec(host + end, url))
						return false;
				}
		}
		return true;
	}

	public boolean isSubDirec(String x, String y) {
		if (x.equals(y))
			return true;
		int a = y.indexOf(x);
		if (a != 0)
			return false;
		if (x.endsWith("/"))
			return true;
		if (y.charAt(x.length()) == '/' || y.charAt(x.length()) == '.')
			return true;
		return false;
	}

	private String trimURL(String s) {
		if (s.startsWith("http://")) {
			return s.substring(7);
		} else if (s.startsWith("https://")) {
			return s.substring(8);
		}
		return s;
	}

	public RobotsTxtInfo getRobot(String url) throws IOException {
		URL urlInfo = new URL(url);
		String host = urlInfo.getHost();
		RobotsTxtInfo robot = robots.get(host);
		if (robot != null)
			return robot;
		HttpClient client = new HttpClient(urlInfo.getProtocol() + "://" + host
				+ "/robots.txt");

		client.sendHead();
		if (!client.getStatus().equals("200"))
			return null;

		client.sendGet();
		BufferedReader in = new BufferedReader(new InputStreamReader(
				client.getBodyInputStream()));
		String line;
		String currentAgent = "";
		robot = new RobotsTxtInfo();
		while ((line = in.readLine()) != null) {
			line = line.trim();
			if (line.startsWith("User-agent")) {
				currentAgent = getContent(line);
				robot.addUserAgent(currentAgent);
				robot.addCrawlDelay(currentAgent, 0.0);
			} else if (line.startsWith("Allow")) {
				if (!getContent(line).equals(""))
					robot.addAllowedLink(currentAgent, getContent(line));
			} else if (line.startsWith("Disallow")) {
				if (!getContent(line).equals(""))
					robot.addDisallowedLink(currentAgent, getContent(line));
			} else if (line.startsWith("Crawl-delay")) {
				if (!getContent(line).equals("")){
					String para = getContent(line);
					if(getContent(line).startsWith(".")){
						para="0"+para;
					}
					robot.addCrawlDelay(currentAgent,
							Double.parseDouble(para));
				}
			}
		}
		robots.put(host, robot);
		return robot;
	}

	private String getContent(String s) {
		if (s.split(":").length < 2)
			return "";
		return s.split(":")[1].trim();
	}

	/*
	 * public static void main(String[] args) throws IOException { if
	 * (args.length != 3) { System.err.println("need 3 arguments"); return; }
	 * String startUrl = args[0]; String DBdirec = args[1]; double maxSize =
	 * Double.parseDouble(args[2]); XPathCrawler crawler = new XPathCrawler();
	 * crawler.setup(startUrl, DBdirec, maxSize, 1000, false);
	 * crawler.startCrawl(); }
	 */

	@Override
	public void run() {
		try{
			File outIMG = new File(this.outIMGDIr+Thread.currentThread().getName());
			if(!outIMG.exists()) outIMG.createNewFile();
			this.outIMG=outIMG;
			while (true) {
				synchronized (frontier) {
					while (frontier.size() == 0) {
						frontier.wait(10000);
						if(count.get()>=maxCount)
							throw new InterruptedException();
					}
				}
				synchronized (frontier) {
					startUrl = frontier.remove(0);
					frontier.notifyAll();
				}

				if (rds.checkCrawled(startUrl)) {
					System.out.println(startUrl + " has crawled before");
					continue;
				}
				if(startUrl.length()>=400) continue;

				try{
					URL tmp = new URL(startUrl);
					
					synchronized(hostCount){
						if(hostCount.containsKey(tmp.getHost())){
							if(hostCount.get(tmp.getHost())>500 && !seedHost.contains(tmp.getHost())){
								System.out.println("too many page from the host: "+ tmp.getHost());
								continue;
							}
						}
					}
					
					
					
					int whoseWork = findWhoseWork(tmp.getHost());
					if (whoseWork == selfID) {
						doSelfWork();
					} else {
						System.out.println(startUrl + " belongs to worker" + whoseWork);
						sendToOtherWorker(whoseWork);
					}

				}catch (IOException e) {
					e.printStackTrace();
					continue;
				} 

				if(count.get()>=maxCount){
					System.out.println("!!!!!!!!!! THREAD STOP!!!!!!!!!!!!!!!!!");
					return;
				}

			}

		}catch (InterruptedException e) {
			System.out.println("!!!!!!!!!!!!!! THREAD IS Interrupted !!!!!!!!!!!!!!!!!");
			e.printStackTrace();
			return;
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	private int findWhoseWork(String url) {
		int i = -1;
		String encryptAlg = "SHA-1";
		MessageDigest md;
		try {
			md = MessageDigest.getInstance(encryptAlg);
			byte[] sha1hash = new byte[40];
			md.update(url.getBytes(), 0, url.length());
			sha1hash = md.digest();
			BigInteger urlHash = new BigInteger(1, sha1hash);
			BigInteger basketNo = urlHash.divide(basket);
			i = basketNo.intValue();


		} catch (NoSuchAlgorithmException e) {

			e.printStackTrace();
		}
		return i;
	}

	private void sendToOtherWorker(int workerID) {
		String targetIP = workerIP.get(workerID).split(":")[0];
		int targetPort = Integer.parseInt(workerIP.get(workerID).split(":")[1]);
		//System.out.println("ip:"+targetIP+";port:"+targetPort);
		try{
			Socket s = new Socket(targetIP, targetPort);
			PrintWriter pw = new PrintWriter(s.getOutputStream());
			pw.println(startUrl);
			pw.flush();
			pw.close();
			s.close();
		}catch(Exception e){
		}
	}

	private void doSelfWork() throws IOException, SQLException {
		Preferences root = Preferences.userRoot();
		root.putBoolean("stop", false);

		currentUrl = startUrl;
		HttpClient client = new HttpClient(currentUrl);
		if (!client.isUrlValid())
			return;
		client.sendHead();
		if (client.getStatus() == null)
			return;

		if (Integer.parseInt(client.getStatus()) >= 400)
			return;

		String contentType = client.getContentType();
		if (contentType == null)
			return;
		int contentLength = client.getContentLength();

		//long lastModified = client.getLastModified();
		if (contentLength > maxSize * 100000)
			return;

		String host = new URLInfo(currentUrl).getHostName();
		Long last = hostCrawledTimes.get(host);
		long hostLastCrawled;
		if (last == null)
			hostLastCrawled = 0;
		else
			hostLastCrawled = last;
		if (!robotsAllowed(currentUrl, hostLastCrawled)) {
			return;
		}

		client.sendGet();
		createFile(currentUrl, client, host);
		if (contentType.equals("text/html")) {
			hostCrawledTimes.put(host, System.currentTimeMillis());
			addLinkstoQueue();
		}
	}

	private String MD5String(String s){

		try{
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(s.getBytes());

			byte byteData[] = md.digest();

			//convert the byte to hex format method 1
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < byteData.length; i++) {
				sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
			}

			//convert the byte to hex format method 2
			StringBuffer hexString = new StringBuffer();
			for (int i=0;i<byteData.length;i++) {
				String hex=Integer.toHexString(0xff & byteData[i]);
				if(hex.length()==1) hexString.append('0');
				hexString.append(hex);
			}
			return hexString.toString();
		}catch(Exception e){
			return "";
		}
	}
	private boolean tooDeep(String s){
		String[] depth = s.split("/");
		if(depth.length<5) return false;
		return true;
	}

	private void IMGHandler(Elements links, String title, File IMGout){
		try{
			PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(IMGout, true)));

			for(Element e: links){
				String url =e.attr("abs:src");
				
				if(url==null || url.equals("")) continue;
				if(rds.checkCrawled(url)) continue;
				if(url.length()>=400) continue;
				int start = url.lastIndexOf("/")+1;
				int end = url.lastIndexOf("?");
				String fileName ="";

				if(end == -1){
					fileName = url.substring(start);
				}
				else{
					fileName = url.substring(start, end);
				}

				String ele = title+" "+ fileName+" "+e.attr("anchor")+" "+e.attr("alt");
				ele.replaceAll("\t", " ");
				String[] text = ele.split("[^a-zA-Z0-9']");


				String content = URLEncoder.encode(url)+"\t";
				for(String s: text){
					String tmp = s.trim();
					if(tmp.equals("")) continue;
					content+=tmp+" ";
				}
				content = content.toLowerCase();
				out.println(content);
				out.flush();
				rds.insertCrawled(url, title);
				System.out.println("^^^^^^^^^Image Crawled^^^^^^^^^^");
			}
			out.close();
		}catch(Exception e){

		}
	}

}
