package KNN;

import java.util.Comparator;


public class DistanceComparator implements Comparator<Result> {

	@Override
    public int compare(Result a, Result b) {
       return a.distance < b.distance ? -1 : a.distance == b.distance ? 0 : 1;
    }

}
