package eva2.gui;

import eva2.tools.EVAHELP;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyEditor;

/**
 *
 */
public class PropertyPanel extends JPanel {
    private PropertyEditor propertyEditor;
    private PropertyDialog propertyDialog;

    private JLabel textLabel;

    /**
     *
     */
    public PropertyPanel(PropertyEditor editor) {
        setToolTipText("Click to edit properties for this object");
        setOpaque(true);

        setLayout(new GridBagLayout());
        GridBagConstraints gbConstraints = new GridBagConstraints();
        gbConstraints.gridx = 0;
        gbConstraints.gridy = 0;
        gbConstraints.weightx = 1.0;
        gbConstraints.fill = GridBagConstraints.HORIZONTAL;
        propertyEditor = editor;

        textLabel = new JLabel();
        add(textLabel, gbConstraints);
    }

    public final void showDialog() {
        Window parent = (Window)this.getRootPane().getParent();
        if (propertyDialog == null) {
            propertyDialog = new PropertyDialog(parent, propertyEditor, EVAHELP.cutClassName(propertyEditor.getClass().getName()));
            propertyDialog.setPreferredSize(new Dimension(500, 300));
            propertyDialog.setModal(true);
            propertyDialog.setVisible(true);
        } else {
            propertyDialog.updateFrameTitle(propertyEditor);
            propertyDialog.setVisible(false);
            propertyDialog.requestFocus();
        }
    }

    /**
     *
     */
    @Override
    public void removeNotify() {
        if (propertyDialog != null) {
            propertyDialog = null;
        }
    }

    /**
     *
     */
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Insets i = textLabel.getInsets();
        Rectangle box = new Rectangle(i.left, i.top,
                getSize().width - i.left - i.right,
                getSize().height - i.top - i.bottom);
        Color back = g.getColor();
        g.setColor(Color.WHITE);
        g.fillRect(i.left, i.top,
                getSize().width - i.right - i.left,
                getSize().height - i.bottom - i.top);
        g.setColor(back);
        propertyEditor.paintValue(g, box);
    }

    public PropertyEditor getEditor() {
        return propertyEditor;
    }
}
