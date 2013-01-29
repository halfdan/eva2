package eva2.tools;

import eva2.gui.BeanInspector;
import java.util.Comparator;

public class PairComparator implements Comparator<Pair<?,?>> {
	boolean useHead = true;
	
	/**
	 * A constructor. Set useHd to true to compare based on the head, otherwise
	 * based on the tail.
	 */
	public PairComparator(boolean useHd) {
		useHead = useHead;
	}
	
	/**	
	 * Compare two Pairs of which head or tail is a primitive type that
	 * can be converted to double. 
	 * Return 1 if the first is larger, -1 if the second is larger, 0 if they
	 * are equal or not comparable.
	 */
    @Override
	public int compare(Pair<?,?> o1, Pair<?,?> o2) {
		Pair<?,?> p1=(Pair<?,?>)o1;
		Pair<?,?> p2=(Pair<?,?>)o2;
		double d1, d2;
		try {
			d1=BeanInspector.toDouble(useHead ? p1.head() : p1.tail());
			d2=BeanInspector.toDouble(useHead ? p2.head() : p2.tail());
		} catch (IllegalArgumentException e) {
			System.err.println("Error, mismatching types, thus uncomparable Pairs: " + p1.toString() + " / " + p2.toString());
			return 0;
		}
		
		if (d1==d2) {
                return 0;
            }
		else if (d1 > d2) {
                return 1;
            }
		else {
                return -1;
            }
	}
}
