package eva2.tools;

import java.io.Serializable;

/**
 * Simple pair structure of two types, Scheme style, but typed.
 * 
 * @author mkron
 *
 */
public class Pair<S, T> implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3620465393975181451L;
	public S head;
	public T tail;
	
	public Object clone() {
		return new Pair<S,T>(head, tail);
	}

	public Pair(S head, T tail) {
		this.head = head;
		this.tail = tail;
	}
	
	public S car() {
		return head;
	}
	
	public T cdr() {
		return tail;
	}
	
	public S head() {
		return head;
	}
	
	public T tail() {
		return tail;
	}
	
	public String toString() {
		return "(" + head.toString() + "," + tail.toString()+")";
	}
}