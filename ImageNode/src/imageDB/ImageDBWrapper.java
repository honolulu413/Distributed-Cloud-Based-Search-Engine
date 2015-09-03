package imageDB;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.persist.StoreConfig;

public class ImageDBWrapper {
	private String direc;
	private static String envDirectory = null;
	private static Environment myEnv;
	private static EntityStore store;
	PrimaryIndex<String, imageOccurence> imageIndex;

	public ImageDBWrapper(String direc) {
		this.direc = direc;
	}

	public void setup() {
		EnvironmentConfig envConfig = new EnvironmentConfig();
		StoreConfig storeConfig = new StoreConfig();

		envConfig.setAllowCreate(true);
		storeConfig.setAllowCreate(true);

		myEnv = new Environment(new File(direc), envConfig);
		store = new EntityStore(myEnv, "EntityStore", storeConfig);

		imageIndex = store.getPrimaryIndex(String.class, imageOccurence.class);
	}

	public void putImageIndex(imageOccurence a) {
		imageIndex.put(a);
	}

	public imageOccurence getImageIndex(String word) {
		return imageIndex.get(word);
	}

	public void close() {
		if (store != null)
			store.close();
		if (myEnv != null)
			myEnv.close();
	}

	public static void main(String[] args) throws IOException {
		ImageDBWrapper db = new ImageDBWrapper(args[1]);

		db.setup();
		File files = new File(args[0]);
		for (File file : files.listFiles()) {
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line;
			while ((line = br.readLine()) != null) {
				String url = line.split("\t", 2)[0];
				String[] words = line.split("\t", 2)[1].split("\\s+");
				for (String word : words) {
					if (word.equals("jpg") || word.equals("png")) continue;
					word = word.toLowerCase().toString();
					if (Utility.stopList.contains(word)) continue;
					if (isAllLetter(word)) {
						word = Stemmer.getString(word);
					}
					imageOccurence image = db.getImageIndex(word);
					if (image != null) {
						if (!image.getUrls().contains(url)) {
							image.add(url);
							db.putImageIndex(image);
							//System.out.println("add more " + image);
						}
					} else {
						image = new imageOccurence();
						image.setWord(word);
						image.add(url);
						db.putImageIndex(image);
						//System.out.println("add " + image);
					}
				}
			}
			br.close();
		}
		db.close();
	}
	
	private static boolean isAllLetter(String s) {
		for (int i = 0; i < s.length(); i++) {
			if (!Character.isLetter(s.charAt(i)))
				return false;
		}
		return true;
	}

}
