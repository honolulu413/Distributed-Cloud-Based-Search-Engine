package master;

import java.util.ArrayList;

public class WordInURL {
	private String word;
	private String positionString;

	private double idf;
	private double tf;

	private double score;

	private ArrayList<Integer> positions = new ArrayList<Integer>();

	public WordInURL(String word, double tf, double idf, String positionString) {
		this.word = word;
		this.tf = tf;
		this.idf = idf;
		

		
		this.positionString = positionString;


		score = tf * idf;

		String[] positionSplits = this.positionString.split("\\s");

		for (int i = positionSplits.length - 1; i >= 0; i--) {
			positions.add(Integer.parseInt(positionSplits[i]));
		}

	}

	public String getWord() {
		return word;
	}

	public double getScore() {
		return score;
	}

	public ArrayList<Integer> getPositions() {
		return positions;
	}
	
	@Override
	public String toString() {
		return word + " " + score + " " + positions;
	}

}
