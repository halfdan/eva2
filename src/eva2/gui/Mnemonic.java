package eva2.gui;

/**
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 12.05.2003
 * Time: 18:30:44
 * To change this template use Options | File Templates.
 */

/**
 *
 */
class Mnemonic {

    private char mnemonic;
    private String text;

    /**
     *
     */
    public Mnemonic(String s) {
        setString(s);
    }

    /**
     *
     */
    public void setString(String s) {
        StringBuffer buf = new StringBuffer(s);
        for (int i = 0; i < buf.length(); i++) {
            if (buf.charAt(i) == '&') {
                buf.deleteCharAt(i);
                i++;
                if (i < buf.length() && buf.charAt(i) != '&') {
                    mnemonic = buf.charAt(i - 1);
                }
            }
        }
        text = buf.toString();
    }

    /**
     *
     */
    public char getMnemonic() {
        return mnemonic;
    }

    /**
     *
     */
    public String getText() {
        return text;
    }
}
