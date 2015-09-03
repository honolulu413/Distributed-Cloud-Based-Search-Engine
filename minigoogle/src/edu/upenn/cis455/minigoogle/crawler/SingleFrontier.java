package edu.upenn.cis455.minigoogle.crawler;

import java.util.ArrayList;

public class SingleFrontier {

	private static SingleFrontier instance;
	public ArrayList<String> frontier = new ArrayList<String>();
	
	private SingleFrontier(){
		//frontier = new ArrayList<String>();
	}
	
	public static SingleFrontier getInstance(){
		if(instance==null){
			instance = new SingleFrontier();
		}
		return instance;
	}
	
	public void setFrontier(ArrayList<String> frontier){
		this.frontier = frontier;
	}
}
