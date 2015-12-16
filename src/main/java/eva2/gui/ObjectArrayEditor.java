package eva2.gui;

import eva2.problems.AbstractProblemDouble;

import javax.swing.*;
import java.awt.*;

/**
 * Created by fabian on 16/12/15.
 */
public class ObjectArrayEditor<T> extends JPanel {
    private JList<T> objectList;
    private DefaultListModel<T> listModel;

    public ObjectArrayEditor(Class<T> type) {
        listModel = new DefaultListModel<>();

        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridwidth = 2;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        c.weighty = 0.0;
        c.gridx = 0;
        c.gridy = 0;

        TypeSelector typeSelector = new TypeSelector();
        typeSelector.updateClassType(type.getName());

        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(typeSelector, c);

        JButton addButton = new JButton("Add");
        c.gridwidth = 1;
        c.gridx = 2;
        c.gridy = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        c.weighty = 0.0;
        add(addButton, c);

        JButton removeButton = new JButton("Remove");
        c.gridx = 2;
        c.gridy = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        c.weighty = 0.0;
        add(removeButton, c);

        JButton button;
        button = new JButton("Config");
        c.gridx = 2;
        c.gridy = 2;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        c.weighty = 0.0;

        add(button, c);

        objectList = new JList<>(listModel);
        objectList.setVisibleRowCount(10);
        //objectList.setFixedCellHeight(15);
        //objectList.setFixedCellWidth(100);

        c.gridwidth = 2;
        c.gridheight = 5;
        c.gridx = 0;
        c.gridy = 1;
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1.0;
        c.weighty = 1.0;

        JScrollPane scrollPane = new JScrollPane(objectList);
        add(scrollPane, c);

        addButton.addActionListener(event -> {
            String className = ((Item) typeSelector.getSelectedItem()).getId();
            try {
                T n = (T) Class.forName(className).newInstance();
                listModel.addElement(n);
            } catch (Exception ex) {
                System.err.println("Exception in itemStateChanged " + ex.getMessage());
                System.err.println("Classpath is " + System.getProperty("java.class.path"));
                ex.printStackTrace();

                JOptionPane.showMessageDialog(this,
                        "Could not create an example of\n"
                                + className + "\n"
                                + "from the current classpath. Is the resource folder at the right place?\nIs the class abstract or the default constructor missing?",
                        "GenericObjectEditor",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        removeButton.addActionListener(event -> {
            if (!objectList.isSelectionEmpty()) {
                listModel.remove(objectList.getSelectedIndex());
            }
        });

    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("ObjectArrayEditor");
        frame.add(new ObjectArrayEditor<>(AbstractProblemDouble.class), BorderLayout.CENTER);
        frame.setVisible(true);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
}
