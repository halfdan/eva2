package eva2.tools;

/**
 *
 */
public class Tag implements java.io.Serializable {
    protected int id;
    protected String text;

    /**
     *
     */
    public Tag(int ident, String str) {
        id = ident;
        text = str;
    }

    /**
     *
     */
    public int getID() {
        return id;
    }

    /**
     *
     */
    public String getString() {
        return text;
    }
}
