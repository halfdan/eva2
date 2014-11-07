package eva2.gui;

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
        StringBuilder buf = new StringBuilder(s);
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
