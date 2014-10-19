package eva2.tools;

import javax.swing.*;
import java.awt.*;

/**
 * Some helper methods connected to the GUI.
 *
 * @author mkron
 */
public class ToolBoxGui {

    /**
     * Create a modal dialog similar to JOptionPane.showInputDialog, with the difference that an initial
     * value can be set.
     *
     * @param parent     the parent component
     * @param title      title of the dialog
     * @param message    message the input field is annotated with
     * @param initialVal initial value of the input field
     * @return A string the user has entered or null if the user canceled the action.
     * @see JOptionPane
     */
    public static String getInputPaneInitialVal(Component parent, String title, String message, String initialVal) {
        return getInputPaneInitialVal(parent, title, message, initialVal, JOptionPane.QUESTION_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
    }

    /**
     * Create a modal dialog similar to JOptionPane.showInputDialog, with the difference that an initial
     * value can be set.
     *
     * @param parent     the parent component
     * @param title      title of the dialog
     * @param message    message the input field is annotated with
     * @param initialVal initial value of the input field
     * @param msgType    message type
     * @param optType    option type
     * @return A string the user has entered or null if the user canceled the action.
     * @see JOptionPane
     */
    public static String getInputPaneInitialVal(Component parent, String title, String message, String initialVal, int msgType, int optType) {
        JOptionPane jop = new JOptionPane(message, msgType, optType, null);
        jop.setWantsInput(true);
        // its a mess with these initial values
        jop.setInputValue(initialVal); // this I expected to work
        jop.setInitialValue(initialVal); // this I expected to work next
        jop.setInitialSelectionValue(initialVal); // this actually seems to work...
        JDialog dialog = jop.createDialog(parent, title);
        dialog.setVisible(true);
        Object value = jop.getValue();
        if (value != null && (value instanceof Integer) && ((Integer) value) == JOptionPane.OK_OPTION) {
            return (String) jop.getInputValue();
        } else {
            return null;
        }
    }

    public static JButton createIconifiedButton(final String iconSrc, final String title, final boolean withTitle) {
        JButton newButton;
        byte[] bytes;
        bytes = BasicResourceLoader.instance().getBytesFromResourceLocation(iconSrc, false);
        if (bytes == null) {
            newButton = new JButton(title);
        } else if (withTitle) {
            newButton = new JButton(title, new ImageIcon(Toolkit.getDefaultToolkit().createImage(bytes)));
        } else {
            newButton = new JButton(new ImageIcon(Toolkit.getDefaultToolkit().createImage(bytes)));
            newButton.setToolTipText(title);
        }
        return newButton;
    }
}
