package master;

import java.util.Comparator;

public class DocComparator implements Comparator<FinalDocument> {

	@Override
	public int compare(FinalDocument o1, FinalDocument o2) {
		if (o1.getScore() > o2.getScore()) {
			return -1;
		} else if (o1.getScore() < o2.getScore()) {
			return 1;
		}
		return 0;
			}

}
