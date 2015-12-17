package eva2.tools;

import eva2.util.annotation.Parameter;

import java.io.Serializable;

/**
 * Simple pair structure of two types, Scheme style, but typed.
 *
 * @author mkron
 */
public class Pair<S, T> implements Serializable {
    /**
     *
     */
    public S head;
    public T tail;

    /**
     * @param head
     * @param tail
     */
    public Pair(S head, T tail) {
        this.head = head;
        this.tail = tail;
    }

    /**
     * @return
     */
    public S car() {
        return head;
    }

    /**
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
    @Override
    public Pair<S, T> clone() {
        return new Pair<>(head, tail);
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
     * @return
     */
    public S head() {
        return head;
    }

    /**
     * @return
     */
    public S getHead() {
        return head;
    }

    /**
     * @return
     */
    public T tail() {
        return tail;
    }

    public T getTail() {
        return tail;
    }

    @Override
    public String toString() {
        return "(" + head.toString() + "," + tail.toString() + ")";
    }

    public String getName() {
        return this.toString();
    }

    /**
     * @param head
     */
    @Parameter(description="First pair entry")
    public void setHead(S head) {
        this.head = head;
    }

    /**
     * @param tail
     */
    @Parameter(description = "Last pair entry")
    public void setTail(T tail) {
        this.tail = tail;
    }
}