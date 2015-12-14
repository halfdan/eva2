package eva2.gui;

import javax.swing.*;

/**
 *
 */
public abstract class ExtAction extends AbstractAction {
    public final static String CAPTION = "Caption";
    public final static String MNEMONIC = "Mnemonic";
    public final static String TOOLTIP = "ToolTip";
    public final static String KEYSTROKE = "KeyStroke";

    /**
     *
     */
    private void setValues(String s, String toolTip) {
        Mnemonic m = new Mnemonic(s);
        putValue(MNEMONIC, m.getMnemonic());
        putValue(Action.NAME, m.getText());
        putValue(TOOLTIP, toolTip);
    }

    /**
     *
     */
    public ExtAction(String s, Icon i, String toolTip, KeyStroke key) {
        this(s, i, toolTip);
        if (i == null) {
            System.out.println("Icon == null");
        }
        putValue(KEYSTROKE, key);
    }

    /**
     *
     */
    public ExtAction(String s, Icon i, String toolTip) {
        super(null, i);
        if (i == null) {
            System.out.println("Icon == null");
        }
        setValues(s, toolTip);
    }

    /**
     *
     */
    public ExtAction(String s, String toolTip, KeyStroke key) {
        this(s, toolTip);
        putValue(KEYSTROKE, key);
    }

    /**
     *
     */
    public ExtAction(String s, String toolTip) {
        super();
        setValues(s, toolTip);
    }
}

