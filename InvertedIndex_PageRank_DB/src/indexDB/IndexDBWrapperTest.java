package indexDB;

import static org.junit.Assert.*;

import org.junit.Test;

public class IndexDBWrapperTest {

	@Test
	public void testIndexDBWrapper() {
		IndexDBWrapper db = new IndexDBWrapper("/home/honolulu413/wordIndex");
		db.setup();
		System.out.println(db.getWordIndex("zk"));
		System.out.println(db.getWordIndex(""));
		System.out.println(db.getWordIndex("yep"));
		System.out.println(db.getWordIndex("yontheroad"));

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
