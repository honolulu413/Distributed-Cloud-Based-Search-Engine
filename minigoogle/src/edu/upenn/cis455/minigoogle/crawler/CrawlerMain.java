package edu.upenn.cis455.minigoogle.crawler;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicLong;

import edu.upenn.cis455.minigoogle.storage.DBRDS;

public class CrawlerMain {

	public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {

		if (args.length != 7) {
			System.err.println("need 7 arguments: maxSize, numThread, selfIP, workersIP, output, countMax,output directory for img info");
			return;
		}
		SingleFrontier singleton = SingleFrontier.getInstance();

		double maxSize = Double.parseDouble(args[0]);
		int threadPoolSize = Integer.parseInt(args[1]);

		DBRDS rds = null;
		try{
			rds = new DBRDS();
		}catch(Exception e){
			e.printStackTrace(System.out);
		}

		//get worker's IP

		ArrayList<String> workerIP = new ArrayList<String>();
		File file = new File(args[3]);
		Scanner in = new Scanner(file);
		while(in.hasNextLine()){
			workerIP.add(in.nextLine());
		}
		in.close();

		String selfIP="";
		file = new File(args[2]);
		in = new Scanner(file);
		selfIP=in.nextLine();
		in.close();
		int port = Integer.parseInt(selfIP.split(":")[1]);
		CrawlerServer server = new CrawlerServer(port);
		Thread thread = new Thread(server);
		thread.start();

		//get frontier from rds
		ArrayList<String> frontierOfRDS = rds.getAllFrontier();
		//rds.deleteAllFrontier();		


		if(frontierOfRDS!=null && !frontierOfRDS.isEmpty()){
			singleton.frontier.addAll(frontierOfRDS);
		}
		System.out.println("frontier size is " + singleton.frontier.size());
		HashSet<String> seedHost = new HashSet<String>();
		if(singleton.frontier!=null && !singleton.frontier.isEmpty()){	
			for(String s: singleton.frontier){
				seedHost.add(s);
			}
		}
		HashMap<String,Integer> hostCount = new HashMap<String,Integer>();
		Thread.sleep(30000);
		
		
		
		rds.deleteAllFrontier();
		Object[] locks = new Object[threadPoolSize];
		Boolean[] readyToPush = new Boolean[threadPoolSize];
		AtomicLong count = new AtomicLong();
		count.set(0);
		long maxCount = Long.parseLong(args[5]);

		Thread[] threadPool = new Thread[threadPoolSize];
		for(int i = 0; i < threadPoolSize;i++){
			threadPool[i] = new Thread(new XPathCrawler("",maxSize,1000,readyToPush[i],count,locks[i],singleton.frontier,workerIP,selfIP,args[4],maxCount,args[6],seedHost,hostCount),"mini"+i);
			threadPool[i].start();
		}

		for(Thread t: threadPool){
			t.join();
			System.out.println(t.getName()+" is finished");
		}
		System.out.println("******************************I reached here************************");
		System.out.println("frontier size is "+singleton.frontier.size());

		thread.join();
		synchronized(singleton.frontier){
			System.out.println("frontier size before hash is "+singleton.frontier.size());
			HashSet<String> tmp = new HashSet<String>();
			for(String s: singleton.frontier) tmp.add(s);
			System.out.println("frondier size after hash is "+ tmp.size());
			ArrayList<String> frontierF = new ArrayList<String>();
			for(String s: tmp){
				frontierF.add(s);
			}
			System.out.println("Before put into RDS size is "+ frontierF.size());
			rds.insertAllFrontier(frontierF);
			
		}
		System.out.println("&&&&&&&&&&&&&&&&&&&&&I Almost Quit&&&&&&&&&&&&&&&&&&&&&&&&&");

		return;
	}
}
