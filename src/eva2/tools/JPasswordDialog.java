package eva2.tools;

import javax.swing.JDialog;
import java.awt.HeadlessException;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ResourceBundle;
import java.util.Locale;

/**
 * A modal dialog that asks the user for a user name and password.
 * More information about this class is available from <a target="_top" href=
 * "http://ostermiller.org/utils/PasswordDialog.html">ostermiller.org</a>.
 *
 * <code>
 * <pre>
 * PasswordDialog p = new PasswordDialog(null, "Test");
 * if(p.showDialog()){
 *     System.out.println("Name: " + p.getName());
 *     System.out.println("Pass: " + p.getPass());
 * } else {
 *     System.out.println("User selected cancel");
 * }
 * </pre>
 * </code>
 *
 * @author Stephen Ostermiller http://ostermiller.org/contact.pl?regarding=Java+Utilities
 * @since ostermillerutils 1.00.00
 */
public class JPasswordDialog extends JDialog {

    /**
     * Locale specific strings displayed to the user.
     *
     * @since ostermillerutils 1.00.00
     */
    protected ResourceBundle labels;

    /**
     * Set the locale used for getting localized
     * strings.
     *
     * @param locale Locale used to for i18n.
     *
     * @since ostermillerutils 1.00.00
     */

    /**
     * Where the name is typed.
     *
     * @since ostermillerutils 1.00.00
     */
    protected JTextField name;
    /**
     * Where the password is typed.
     *
     * @since ostermillerutils 1.00.00
     */
    protected JPasswordField pass;
    /**
     * The OK button.
     *
     * @since ostermillerutils 1.00.00
     */
    protected JButton okButton;
    /**
     * The cancel button.
     *
     * @since ostermillerutils 1.00.00
     */
    protected JButton cancelButton;
    /**
     * The label for the field in which the name is typed.
     *
     * @since ostermillerutils 1.00.00
     */
    protected JLabel nameLabel;
    /**
     * The label for the field in which the password is typed.
     *
     * @since ostermillerutils 1.00.00
     */
    protected JLabel passLabel;

    /**
     * Set the name that appears as the default
     * An empty string will be used if this in not specified
     * before the dialog is displayed.
     *
     * @param name default name to be displayed.
     *
     * @since ostermillerutils 1.00.00
     */
    public void setName(String name){
        this.name.setText(name);
    }

    /**
     * Set the password that appears as the default
     * An empty string will be used if this in not specified
     * before the dialog is displayed.
     *
     * @param pass default password to be displayed.
     *
     * @since ostermillerutils 1.00.00
     */
    public void setPass(String pass){
        this.pass.setText(pass);
    }

    /**
     * Set the label on the OK button.
     * The default is a localized string.
     *
     * @param ok label for the ok button.
     *
     * @since ostermillerutils 1.00.00
     */
    public void setOKText(String ok){
        this.okButton.setText(ok);
        pack();
    }

    /**
     * Set the label on the cancel button.
     * The default is a localized string.
     *
     * @param cancel label for the cancel button.
     *
     * @since ostermillerutils 1.00.00
     */
    public void setCancelText(String cancel){
        this.cancelButton.setText(cancel);
        pack();
    }

    /**
     * Set the label for the field in which the name is entered.
     * The default is a localized string.
     *
     * @param name label for the name field.
     *
     * @since ostermillerutils 1.00.00
     */
    public void setNameLabel(String name){
        this.nameLabel.setText(name);
        pack();
    }

    /**
     * Set the label for the field in which the password is entered.
     * The default is a localized string.
     *
     * @param pass label for the password field.
     *
     * @since ostermillerutils 1.00.00
     */
    public void setPassLabel(String pass){
        this.passLabel.setText(pass);
        pack();
    }

    /**
     * Get the name that was entered into the dialog before
     * the dialog was closed.
     *
     * @return the name from the name field.
     *
     * @since ostermillerutils 1.00.00
     */
    public String getName(){
        return name.getText();
    }

    /**
     * Get the password that was entered into the dialog before
     * the dialog was closed.
     *
     * @return the password from the password field.
     *
     * @since ostermillerutils 1.00.00
     */
    public String getPass(){
        return new String(pass.getPassword());
    }

    /**
     * Finds out if user used the OK button or an equivalent action
     * to close the dialog.
     * Pressing enter in the password field may be the same as
     * 'OK' but closing the dialog and pressing the cancel button
     * are not.
     *
     * @return true if the the user hit OK, false if the user canceled.
     *
     * @since ostermillerutils 1.00.00
     */
    public boolean okPressed(){
        return pressed_OK;
    }

    /**
     * update this variable when the user makes an action
     *
     * @since ostermillerutils 1.00.00
     */
    private boolean pressed_OK = false;

    /**
     * Create this dialog with the given parent and title.
     *
     * @param parent window from which this dialog is launched
     * @param title the title for the dialog box window
     *
     * @since ostermillerutils 1.00.00
     */
    public JPasswordDialog(Frame parent, String title) {

        super(parent, title, true);


        if (title==null){
            setTitle(labels.getString("dialog.title"));
        }
        if (parent != null){
            setLocationRelativeTo(parent);
        }
        // super calls dialogInit, so we don't need to do it again.
    }

    /**
     * Create this dialog with the given parent and the default title.
     *
     * @param parent window from which this dialog is launched
     *
     * @since ostermillerutils 1.00.00
     */
    public JPasswordDialog(Frame parent) {
        this(parent, null);
    }

    /**
     * Create this dialog with the default title.
     *
     * @since ostermillerutils 1.00.00
     */
    public JPasswordDialog() {
        this(null, null);
    }

    /**
     * Called by constructors to initialize the dialog.
     *
     * @since ostermillerutils 1.00.00
     */
    protected void dialogInit(){


        name = new JTextField("", 20);
        pass = new JPasswordField("", 20);
        okButton = new JButton("OK");
        cancelButton = new JButton("cancel");
        nameLabel = new JLabel("login:" + " ");
        passLabel = new JLabel("password" + " ");
        super.dialogInit();

        KeyListener keyListener = (new KeyAdapter() {
            public void keyPressed(KeyEvent e){
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE ||
                        (e.getSource() == cancelButton
                        && e.getKeyCode() == KeyEvent.VK_ENTER)){
                    pressed_OK = false;
                    JPasswordDialog.this.hide();
                }
                if (e.getSource() == okButton &&
                        e.getKeyCode() == KeyEvent.VK_ENTER){
                    pressed_OK = true;
                    JPasswordDialog.this.hide();
                }
            }
        });
        addKeyListener(keyListener);

        ActionListener actionListener = new ActionListener() {
            public void actionPerformed(ActionEvent e){
                Object source = e.getSource();
                if (source == name){
                    // the user pressed enter in the name field.
                    name.transferFocus();
                } else {
                    // other actions close the dialog.
                    pressed_OK = (source == pass || source == okButton);
                    JPasswordDialog.this.hide();
                }
            }
        };

        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        c.insets.top = 5;
        c.insets.bottom = 5;
        JPanel pane = new JPanel(gridbag);
        pane.setBorder(BorderFactory.createEmptyBorder(10, 20, 5, 20));
        JLabel label;

        c.anchor = GridBagConstraints.EAST;
        gridbag.setConstraints(nameLabel, c);
        pane.add(nameLabel);

        gridbag.setConstraints(name, c);
        name.addActionListener(actionListener);
        name.addKeyListener(keyListener);
        pane.add(name);

        c.gridy = 1;
        gridbag.setConstraints(passLabel, c);
        pane.add(passLabel);

        gridbag.setConstraints(pass, c);
        pass.addActionListener(actionListener);
        pass.addKeyListener(keyListener);
        pane.add(pass);

        c.gridy = 2;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.anchor = GridBagConstraints.CENTER;
        JPanel panel = new JPanel();
        okButton.addActionListener(actionListener);
        okButton.addKeyListener(keyListener);
        panel.add(okButton);
        cancelButton.addActionListener(actionListener);
        cancelButton.addKeyListener(keyListener);
        panel.add(cancelButton);
        gridbag.setConstraints(panel, c);
        pane.add(panel);

        getContentPane().add(pane);

        pack();
    }

    /**
     * Shows the dialog and returns true if the user pressed ok.
     *
     * @return true if the the user hit OK, false if the user canceled.
     *
     * @since ostermillerutils 1.00.00
     */
    public boolean showDialog(){
        show();
        return okPressed();
    }

    /**
     * A simple example to show how this might be used.
     * If there are arguments passed to this program, the first
     * is treated as the default name, the second as the default password
     *
     * @param args command line arguments: name and password (optional)
     *
     * @since ostermillerutils 1.00.00
     */
    private static void main(String[] args){
        JPasswordDialog p = new JPasswordDialog();
        if(args.length > 0){
            p.setName(args[0]);
        }
        if(args.length > 1){
            p.setPass(args[1]);
        }
        if(p.showDialog()){
            System.out.println("Name: " + p.getName());
            System.out.println("Pass: " + p.getPass());
        } else {
            System.out.println("User selected cancel");
        }
        p.dispose();
        p = null;
        System.exit(0);
    }
}


/**
 * <p>Title: EvA2</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author planatsc
 * @version 1.0
 */

/*public class JPasswordDialog extends JDialog {
  JTextField loginfield;
  JPasswordField passfield;

  public JPasswordDialog() throws HeadlessException {
     passfield = new JPasswordField(10);
     loginfield = new JTextField(10);
  }

  public static void main(String[] args) throws HeadlessException {
    JPasswordDialog JPasswordDialog1 = new JPasswordDialog();
  }



}*/
