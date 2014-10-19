package eva2.gui.editor;

import eva2.gui.PropertyDialog;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyEditor;

public class BigStringEditor implements PropertyEditor {
    private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
    private PropertyEditor elementEditor;
    private JTextArea textArea;
    private JScrollPane scrollPane;
    private JPanel panel;
    private JButton setButton;
    static private boolean isFinished = false;

    /**
     *
     */
    public static void editSource(String file) {

        try {
            isFinished = false;
            BigStringEditor editor = new BigStringEditor();

            PropertyDialog dialog = new PropertyDialog(null, editor, file);

            while (isFinished == false) {
                try {
                    Thread.sleep(1000);
                } catch (Exception e) {
                    System.out.println("e+" + e.getMessage());
                }
            }


        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
    }

    /**
     *
     */
    public BigStringEditor() {
        super();
//    textArea = new JEditTextArea();
//    textArea.setTokenMarker(new JavaTokenMarker());
        textArea = new JTextArea(60, 60);
        textArea.setEditable(true);
        textArea.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        scrollPane = new JScrollPane(textArea);
        panel = new JPanel();
        panel.setBorder(BorderFactory.createTitledBorder("Sourcecode"));
        panel.setLayout(new BorderLayout());
        setButton = new JButton("SET");
        setButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setValue(textArea.getText());
            }
        });
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(setButton, BorderLayout.SOUTH);
    }

    /**
     *
     */
    @Override
    public void setValue(Object value) {
        elementEditor = null;
        if (value instanceof String) {
            textArea.setText((String) value);
        }
        propertyChangeSupport.firePropertyChange("", null, null);
    }

    /**
     *
     */
    @Override
    public Object getValue() {
        return null;
    }

    /**
     *
     */
    @Override
    public String getJavaInitializationString() {
        return "null";
    }

    /**
     * Returns true to indicate that we can paint a representation of the
     * string array
     *
     * @return true
     */
    @Override
    public boolean isPaintable() {
        return true;
    }

    /**
     * Paints a representation of the current classifier.
     *
     * @param gfx the graphics context to use
     * @param box the area we are allowed to paint into
     */
    @Override
    public void paintValue(Graphics gfx, Rectangle box) {
        FontMetrics fm = gfx.getFontMetrics();
        int vpad = (box.height - fm.getAscent()) / 2;
        gfx.drawString("BigStringEditor", 2, fm.getHeight() + vpad - 3);
    }

    /**
     *
     */
    @Override
    public String getAsText() {
        return null;
    }

    /**
     *
     */
    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        throw new IllegalArgumentException(text);
    }

    /**
     *
     */
    @Override
    public String[] getTags() {
        return null;
    }

    /**
     *
     */
    @Override
    public boolean supportsCustomEditor() {
        return true;
    }

    /**
     *
     */
    @Override
    public Component getCustomEditor() {
        return panel;
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener l) {
        if (propertyChangeSupport == null) {
            propertyChangeSupport = new PropertyChangeSupport(this);
        }
        propertyChangeSupport.addPropertyChangeListener(l);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener l) {
        if (propertyChangeSupport == null) {
            propertyChangeSupport = new PropertyChangeSupport(this);
        }
        propertyChangeSupport.removePropertyChangeListener(l);
    }
}



