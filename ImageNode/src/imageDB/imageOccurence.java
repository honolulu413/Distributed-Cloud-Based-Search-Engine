package imageDB;

import java.util.ArrayList;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.Persistent;
import com.sleepycat.persist.model.PrimaryKey;

@Entity
public class imageOccurence {
	@PrimaryKey
	private String word;
	private ArrayList<String> urls = new ArrayList<String>();
	
	public String getWord() {
		return word;
	}
	
	public void setWord(String word) {
		this.word = word;
	}

	public ArrayList<String> getUrls() {
		return urls;
	}
	public void add(String s) {
		urls.add(s);
	}

	public String toString() {
		return word + " " + urls;
	}
	
	
	
}
