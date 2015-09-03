package master;

import java.util.ArrayList;
import java.util.List;

public class FinalDocument {
	private String url;
	private double totalScore;

	public FinalDocument(String url, double totalScore) {
		this.url = url;
		this.totalScore = totalScore;
	}

	public String getUrl() {
		return url;
	}

	public double getScore() {
		return totalScore;
	}

	@Override
	public String toString() {
		return url + " " + totalScore;
 	}
	
	public static void main(String[] args) {
		ArrayList<String> ar = new ArrayList<String>();
		ar.add("d");
		ar.add("dss");
		List<String> s = (List<String>) ar.subList(0, 1);
		System.out.println(s);
	}
	
}
