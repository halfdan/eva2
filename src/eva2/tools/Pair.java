package eva2.tools;

/**
 * Simple pair structure of two types, Scheme style, but typed.
 * 
 * @author mkron
 *
 */
public class Pair<S, T> {
	public S head;
	public T tail;

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
}