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

	/**
	 * 
	 * @param head
	 * @param tail
	 */
	public Pair(S head, T tail) {
		this.head = head;
		this.tail = tail;
	}

	/**
	 * 
	 * @return
	 */
	public S car() {
		return head;
	}

	/**
	 * 
	 * @return
	 */
	public T cdr() {
		return tail;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#clone()
	 */
	public Pair<S, T> clone() {
		return new Pair<S, T>(head, tail);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return toString().hashCode();
	}

	/**
	 * 
	 * @return
	 */
	public S head() {
		return head;
	}

	/**
	 * 
	 * @return
	 */
	public T tail() {
		return tail;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "(" + head.toString() + "," + tail.toString() + ")";
	}

	/**
	 * 
	 * @param head
	 */
	public void setHead(S head) {
		this.head = head;		
	}

	/**
	 * 
	 * @param tail
	 */
	public void setTail(T tail) {
		this.tail = tail;
	}
}