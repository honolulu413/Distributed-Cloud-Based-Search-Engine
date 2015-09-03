package model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

public class DB {
	private Connection conn;

	private static final String DB_URL = "jdbc:oracle:thin:@//crawlerrds.cqxz0jhk0go4.us-east-1.rds.amazonaws.com:1521/MyDB";
	private static final String USERNAME = "lulucis555";
	private static final String PASSWORD = "cis5552015";

	public DB() {
		try {
			Class.forName("oracle.jdbc.driver.OracleDriver");
			conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public ArrayList<SearchResult> getResultWithTitlte(ArrayList<String> urlList) {
		ArrayList<SearchResult> list = new ArrayList<SearchResult>();
		for (int i = 0; i < urlList.size(); i++) {
			String url = urlList.get(i);
			try {
				Statement stmt = conn.createStatement();
				
				url = url.replaceAll("'|\"", "");
				
				String sql = "select * from crawled where urlcrawled='" + url
						+ "'";
				ResultSet rs = stmt.executeQuery(sql);
				boolean exist = false;
				while (rs.next()) {
					String title = rs.getString("title");
					// System.out.println("title:"+title);
					exist = true;
					SearchResult sr = new SearchResult(title, url);
					list.add(sr);
				}
				if (!exist) {
					String titleStr = "TitleNotFound";
					SearchResult sresult = new SearchResult(titleStr, url);
					list.add(sresult);
				}
				stmt.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return list;
	}
	
	public ArrayList<SearchResult> getImgWithTitlte(ArrayList<String> urlList) {
		ArrayList<SearchResult> list = new ArrayList<SearchResult>();
		for (int i = 0; i < urlList.size(); i++) {
			String url = urlList.get(i);
			try {
				Statement stmt = conn.createStatement();
				
				url = url.replaceAll("'|\"", "");
				
				String sql = "select * from eccrawled where urlcrawled='" + url
						+ "'";
				ResultSet rs = stmt.executeQuery(sql);
				boolean exist = false;
				while (rs.next()) {
					String title = rs.getString("title");
					// System.out.println("title:"+title);
					exist = true;
					SearchResult sr = new SearchResult(title, url);
					list.add(sr);
				}
				if (!exist) {
					String titleStr = "TitleNotFound";
					SearchResult sresult = new SearchResult(titleStr, url);
					list.add(sresult);
				}
				stmt.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return list;
	}
}
