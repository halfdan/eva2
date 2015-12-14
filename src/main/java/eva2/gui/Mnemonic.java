package eva2.gui;

/**
 *
 */
class Mnemonic {

    private final char mnemonic;
    private final String text;

    /**
     *
     */
    public Mnemonic(String s) {
        StringBuilder buf = new StringBuilder(s);
        char c = Character.MIN_VALUE;

        for (int i = 0; i < buf.length(); i++) {
            if (buf.charAt(i) == '&') {
                buf.deleteCharAt(i);
                i++;
                if (i < buf.length() && buf.charAt(i) != '&') {
                    c = buf.charAt(i - 1);
                }
            }
        }
        mnemonic = c;
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
