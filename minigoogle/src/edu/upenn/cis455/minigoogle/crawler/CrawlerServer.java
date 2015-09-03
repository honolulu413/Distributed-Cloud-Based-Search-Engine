package edu.upenn.cis455.minigoogle.crawler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Set;


public class CrawlerServer implements Runnable {
	private int port;

	public CrawlerServer(int port){
		this.port = port;
	}

	public void run(){

		SingleFrontier singleton=SingleFrontier.getInstance();

		try{
			ServerSocket listener = new ServerSocket(port);
			listener.setSoTimeout(180000);
			while (true) {
				try{
					Socket socket = listener.accept();
					InputStreamReader reader = new InputStreamReader(socket.getInputStream());
					BufferedReader buf = new BufferedReader(reader);
					String str = buf.readLine();
					str= str.trim();
					synchronized (singleton.frontier) {
						if(!singleton.frontier.contains(str)){
							System.out.println("str:"+str);
							singleton.frontier.add(str);
							singleton.frontier.notifyAll();
						}
					}
					socket.close();
				}catch(SocketTimeoutException e){
					System.out.println("##############Server Closed###########");
					listener.close();
					break;
				}catch(IOException e){

				}
			}
		}catch(IOException e){
		}
	}
}
