package imageDB;

import static org.junit.Assert.*;

import org.junit.Test;

public class ImageDBWrapperTest {

	@Test
	public void testIndexDBWrapper() {
		ImageDBWrapper db = new ImageDBWrapper("/home/honolulu413/imageDB");
		db.setup();
		System.out.println(db.getImageIndex("zk"));
		System.out.println(db.getImageIndex("pk"));
		System.out.println(db.getImageIndex("zack"));
		System.out.println(db.getImageIndex("yontheroad"));

		db.close();
		

	}

	public static boolean isAllLetter(String s) {
		for (int i = 0; i < s.length(); i++) {
			if (!Character.isLetter(s.charAt(i)))
				return false;
		}
		return true;
	}

}
