package edu.upenn.cis455.minigoogle.storage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;

public class DBRDS {

	private Connection conn;

	private static final String DB_URL = "jdbc:oracle:thin:@//crawlerrds.cqxz0jhk0go4.us-east-1.rds.amazonaws.com:1521/MyDB";
	private static final String USERNAME = "lulucis555";
	private static final String PASSWORD = "cis5552015";

	public DBRDS() {	
		try {
			Class.forName("oracle.jdbc.driver.OracleDriver");
			conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void createFrontierTable() throws SQLException {
		Statement stmt = conn.createStatement();
		String sql;
		sql = "CREATE TABLE ECFRONTIER "
				+ " (urlFrontier VARCHAR(200) not null, "
				+ " PRIMARY KEY (urlFrontier))";
		try {
			stmt.executeUpdate(sql);
		} catch (Exception e) {
			e.printStackTrace();
		}
		stmt.close();
	}

	public void createCrawledTable() throws SQLException {
		Statement stmt = conn.createStatement();
		String sql;
		sql = "CREATE TABLE ECCrawled " + " (urlCrawled VARCHAR(200) not null, "
				+ " PRIMARY KEY (urlCrawled))";
		try {
			stmt.executeUpdate(sql);
		} catch (Exception e) {
			e.printStackTrace();
		}
		stmt.close();
	}

	public void createContentTable() throws SQLException {
		Statement stmt = conn.createStatement();
		String sql;
		sql = "CREATE TABLE ECcontent " + " (content VARCHAR(200) not null, "
				+ " PRIMARY KEY (content))";
		try {
			stmt.executeUpdate(sql);
		} catch (Exception e) {
			e.printStackTrace();
		}
		stmt.close();
	}

	public void deleteFrontierTable() throws SQLException {
		Statement stmt = conn.createStatement();
		String sql;
		sql = "DROP TABLE ECFrontier";
		try {
			stmt.executeUpdate(sql);
		} catch (Exception e) {
			e.printStackTrace();
		}
		stmt.close();
	}

	public void deleteCrawledTable() throws SQLException {
		Statement stmt = conn.createStatement();
		String sql;
		sql = "DROP TABLE ECCrawled";
		try {
			stmt.executeUpdate(sql);
		} catch (Exception e) {
			e.printStackTrace();
		}
		stmt.close();
	}

	public void deleteContentTable() throws SQLException {
		Statement stmt = conn.createStatement();
		String sql;
		sql = "DROP TABLE ECcontent";
		try {
			stmt.executeUpdate(sql);
		} catch (Exception e) {
			e.printStackTrace();
		}
		stmt.close();
	}

	public void insertFrontier(String url){
		try {
			Statement stmt = conn.createStatement();
			String sql = "INSERT INTO ECFrontier VALUES (?)";
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, url);
			ps.execute();
			ps.close();
			stmt.close();
		} catch (Exception ex) {
		}

	}

	public void insertAllFrontier(ArrayList<String> urlFrontier){
		
		final int batchSize = 1000;
		int count = 0;
		
		HashSet<String> urlSet = new HashSet<String>();
		for(String url: urlFrontier)
			urlSet.add(url);
			
		try{
			Statement stmt = conn.createStatement();
			String sql = "select * from ECfrontier";
			ResultSet rs = stmt.executeQuery(sql);
			ArrayList<String> list = new ArrayList<String>();
			while (rs.next()) {
				String url = rs.getString("urlfrontier");
				list.add(url);
			}
			stmt.close();
			
			sql = "INSERT INTO ECFrontier (urlFrontier) VALUES (?)";
			PreparedStatement ps = conn.prepareStatement(sql);
			for(String s: urlSet){
				if(s==null||s.equals("")||list.contains(s)) continue;
				ps.setString(1, s);
				ps.addBatch();
				if(++count%batchSize==0) ps.executeBatch();
			}
			ps.executeBatch();
			ps.close();
			
		}catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void insertCrawled(String url,String title) {
		try {
			Statement stmt = conn.createStatement();
			String sql = "INSERT INTO ECCrawled VALUES (?,?)";
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, url);
			ps.setString(2,title);
			ps.execute();
			ps.close();
			stmt.close();
		} catch (Exception e) {
			System.out.println(e.toString());
		}

	}

	public void insertContent(String url) {
		try {
			Statement stmt = conn.createStatement();
			String sql = "INSERT INTO ECContent VALUES (?)";
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, url);
			ps.execute();
			ps.close();
			stmt.close();
		} catch (Exception e) {

		}

	}

	public boolean checkCrawled(String url){
		try {
			Statement stmt = conn.createStatement();
			String sql = "select * from ECcrawled where urlCrawled ='" + url
					+ "'";
			ResultSet rs = stmt.executeQuery(sql);
			if (!rs.next()) {
				return false;
			} else
				return true;
		} catch (Exception e) {
			return false;
		}

	}

	public boolean checkContent(String str){
		try {
			Statement stmt = conn.createStatement();
			String sql = "select * from ECcontent where content ='" + str + "'";
			ResultSet rs = stmt.executeQuery(sql);
			if (!rs.next()) {
				return false;
			} else
				return true;
		} catch (Exception e) {
			return false;
		}

	}

	public void deleteAllFrontier(){
		try {
			Statement stmt = conn.createStatement();
			String sql;
			sql = "DELETE FROM ECFrontier";
			try {
				stmt.executeUpdate(sql);
			} catch (Exception e) {
				e.printStackTrace();
			}
			stmt.close();
		} catch (Exception e) {

		}

	}
	
	public void deleteFrontierByCount(int count){
		try {
			Statement stmt = conn.createStatement();
			String sql;
			sql = "DELETE FROM ECFrontier where rownum<='"+count+"'";
			try {
				stmt.executeUpdate(sql);
			} catch (Exception e) {
				e.printStackTrace();
			}
			stmt.close();
		} catch (Exception e) {

		}
	}

	public void deleteAllCrawled(){
		try {
			Statement stmt = conn.createStatement();
			String sql;
			sql = "DELETE FROM ECCrawled";
			try {
				stmt.executeUpdate(sql);
			} catch (Exception e) {
				e.printStackTrace();
			}
			stmt.close();
		} catch (Exception e) {

		}

	}

	public void deleteAllContent(){
		try {
			Statement stmt = conn.createStatement();
			String sql;
			sql = "DELETE FROM TABLE ECContent";
			try {
				stmt.executeUpdate(sql);
			} catch (Exception e) {
				e.printStackTrace();
			}
			stmt.close();
		} catch (Exception e) {

		}

	}

	public ArrayList<String> getAllFrontier() {
		int count = 0;
		try {
			Statement stmt = conn.createStatement();
			String sql = "select count(*) as count from ECfrontier";
			ResultSet rs = stmt.executeQuery(sql);
			while (rs.next()) {
				count = Integer.parseInt(rs.getString("count"));
			}
			stmt.close();
		} catch (Exception e) {
			
		}
		if(count<100000){
			try {
				Statement stmt = conn.createStatement();
				String sql = "select * from ECfrontier";
				ResultSet rs = stmt.executeQuery(sql);
				ArrayList<String> list = new ArrayList<String>();
				while (rs.next()) {
					String url = rs.getString("urlfrontier");
					list.add(url);
				}
				stmt.close();
				deleteAllFrontier();
				return list;
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}else{
			try {
				Statement stmt = conn.createStatement();
				String sql = "select * from ECfrontier where rownum<='"+100000+"'";
				ResultSet rs = stmt.executeQuery(sql);
				ArrayList<String> list = new ArrayList<String>();
				while (rs.next()) {
					String url = rs.getString("urlfrontier");
					list.add(url);
				}
				stmt.close();
				deleteFrontierByCount(100000);
				return list;
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}
		
		
		

	}

	public HashSet<String> getAllCrawled() {
		try {
			Statement stmt = conn.createStatement();
			String sql = "select * from ECcrawled";
			ResultSet rs = stmt.executeQuery(sql);
			HashSet<String> list = new HashSet<String>();
			while (rs.next()) {
				String url = rs.getString("urlCrawled");
				list.add(url);
			}
			stmt.close();
			return list;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}

	public HashSet<String> getAllContent() {
		try {
			Statement stmt = conn.createStatement();
			String sql = "select * from ECcontent";
			ResultSet rs = stmt.executeQuery(sql);
			HashSet<String> list = new HashSet<String>();
			while (rs.next()) {
				String url = rs.getString("content");
				list.add(url);
			}
			stmt.close();
			return list;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}
}
