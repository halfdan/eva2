package eva2.optimization.tools;


import eva2.gui.editor.GenericObjectEditor;
import eva2.tools.EVAHELP;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.util.ArrayList;
import javax.swing.*;

/**
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 24.08.2004
 * Time: 13:38:56
 * To change this template use File | Settings | File Templates.
 */
public class GeneralGenericObjectEditorPanel extends JPanel implements ItemListener {

    // This is the Object Editor i belong too
    private AbstractObjectEditor    m_ObjectEditor;
    /** The chooser component */
    private JComboBox               m_ObjectChooser;
    /** The component that performs classifier customization */
    //private PropertySheetPanel      m_ChildPropertySheet;
    private JPanel                  m_PPWrapper;
    private JPanel                  m_PropertyPanel;
    /** The model containing the list of names to select from */
    private DefaultComboBoxModel    m_ObjectNames;
    /** Open object from disk */
    private JButton                 m_OpenBut;
    /** Save object to disk */
    private JButton                 m_SaveBut;
    /** ok button */
    public JButton                  m_okBut;
    /** cancel button */
    private JButton                 m_cancelBut;
    /** The filechooser for opening and saving object files */
    private JFileChooser            m_FileChooser;
    /** Creates the GUI editor component */
    private ArrayList<String>		m_ClassesLongName;

    /** This is the general construtor method
     * @param oe    A link to the oe;
     */
    public GeneralGenericObjectEditorPanel(AbstractObjectEditor oe) {
        this.m_ObjectEditor     = oe;
        oe.makeBackup();
        m_ObjectNames           = new DefaultComboBoxModel(new String [0]);
        m_ObjectChooser         = new JComboBox(m_ObjectNames);
        m_ObjectChooser.setEditable(false);
        m_PPWrapper             = new JPanel();
        m_PropertyPanel         = this.m_ObjectEditor.getPropertyPanel();
        m_PropertyPanel.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
	        public void propertyChange(PropertyChangeEvent evt) {
	            m_ObjectEditor.firePropertyChange("", null, m_ObjectEditor.getValue());
	        }
        });
        m_OpenBut               = new JButton("Open...");
        m_OpenBut.setToolTipText("Load a configured object");
        m_OpenBut.setEnabled(true);
        m_OpenBut.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
	            Object object = openObject();
                if (object != null) {
                    // setValue takes care of: Making sure obj is of right type,
                    // and firing property change.
                    m_ObjectEditor.setValue(object);
                    // Need a second setValue to get property values filled in OK.
                    // Not sure why.
                    m_ObjectEditor.setValue(object); // <- Hannes ?!?!?
                }
	        }
        });

        m_SaveBut           = new JButton("Save...");
        m_SaveBut.setToolTipText("Save the current configured object");
        m_SaveBut.setEnabled(true);
        m_SaveBut.addActionListener(new ActionListener() {
            @Override
	        public void actionPerformed(ActionEvent e) {
	            saveObject(m_ObjectEditor.getValue());
	        }
        });

//        m_editSourceBut         = new JButton("Edit Source");
//        m_editSourceBut.setToolTipText("Edit the Source");
//        m_editSourceBut.setEnabled(false);
//        m_editSourceBut.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent e) {
//                m_editSourceBut.setEnabled(false);
//                m_SourceCodeEditor  = new SourceCodeEditor();
//                String className    = m_ObjectEditor.getValue().getClass().getName();
//                m_SourceCodeEditor.editSource(Main.DYNAMICCLASSES_PROPERTIES.getProperty(className));
//                m_SourceCodeEditorFrame = new PropertyDialog(m_SourceCodeEditor, "test", 50, 50);
//                m_SourceCodeEditorFrame.pack();
//                m_SourceCodeEditorFrame.addWindowListener(new WindowAdapter() {
//                    public void windowClosing (WindowEvent e) {
//                        m_SourceCodeEditor = null;
//                        m_editSourceBut.setEnabled(true);
//                    }
//                });
//                m_SourceCodeEditor.addPropertyChangeListener(new PropertyChangeListener() {
//                    public void propertyChange(PropertyChangeEvent evt) {
//                        sourceChanged();
//                    }
//                });
//            }
//        });

        m_okBut             = new JButton("OK");
        m_okBut.setEnabled(true);
        m_okBut.addActionListener(new ActionListener() {
            @Override
	        public void actionPerformed(ActionEvent e) {
	            m_ObjectEditor.makeBackup();
	            if ((getTopLevelAncestor() != null) && (getTopLevelAncestor() instanceof Window)) {
	                Window w = (Window) getTopLevelAncestor();
	                w.dispose();
	            }
	        }
        });

        m_cancelBut         = new JButton("Cancel");
        m_cancelBut.setEnabled(false);
        m_cancelBut.addActionListener(new ActionListener() {
            @Override
	        public void actionPerformed(ActionEvent e) {
                m_ObjectEditor.undoBackup();
	            updateClassType();
	            updateChooser();
	            updateChildPropertySheet();
	            if ((getTopLevelAncestor() != null) && (getTopLevelAncestor() instanceof Window)) {
	                Window w = (Window) getTopLevelAncestor();
	                w.dispose();
	            }
	        }
        });

        setLayout(new BorderLayout());
        add(m_ObjectChooser, BorderLayout.NORTH);  // important
        m_PPWrapper.add(m_PropertyPanel);
        add((JComponent)m_PPWrapper, BorderLayout.CENTER);
        // Since we resize to the size of the property sheet, a scrollpane isn't
        // typically needed
        // add(new JScrollPane(m_ChildPropertySheet), BorderLayout.CENTER);

        JPanel okcButs = new JPanel();
        okcButs.setBorder(BorderFactory.createEmptyBorder());
        okcButs.setLayout(new GridLayout(1, 4, 5, 5));
        okcButs.add(m_OpenBut);
        okcButs.add(m_SaveBut);
        okcButs.add(m_okBut);
        //okcButs.add(m_cancelBut);
        add(okcButs, BorderLayout.SOUTH);

        if (this.m_ObjectEditor.getClassType() != null) {
	        updateClassType();
	        updateChooser();
	        updateChildPropertySheet();
        }
        m_ObjectChooser.addItemListener(this);
    }

    /** Opens an object from a file selected by the user.
     * @return the loaded object, or null if the operation was cancelled
     */
    protected Object openObject() {
        if (m_FileChooser == null) {
            createFileChooser();
        }

        int returnVal = m_FileChooser.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
	        File selected = m_FileChooser.getSelectedFile();
	        try {
	            ObjectInputStream   oi  = new ObjectInputStream(new BufferedInputStream(new FileInputStream(selected)));
                Object              obj = oi.readObject();
                oi.close();
                if (!this.m_ObjectEditor.getClassType().isAssignableFrom(obj.getClass())) {
                    throw new Exception("Object not of type: " + this.m_ObjectEditor.getClassType().getName());
                }
                return obj;
	        } catch (Exception ex) {
	            JOptionPane.showMessageDialog(this,
					"Couldn't read object: "
					+ selected.getName()
					+ "\n" + ex.getMessage(),
					"Open object file",
					JOptionPane.ERROR_MESSAGE);
	        }
        }
        return null;
    }

    /**Opens an object from a file selected by the user.
     * @param object the object to save.
     */
    protected void saveObject(Object object) {
        if (m_FileChooser == null) {
            createFileChooser();
        }
        int returnVal = m_FileChooser.showSaveDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
	        File sFile = m_FileChooser.getSelectedFile();
	        try {
	            ObjectOutputStream oo = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(sFile)));
                oo.writeObject(object);
                oo.close();
	        } catch (Exception ex) {
	            JOptionPane.showMessageDialog(this,
					"Couldn't write to file: "
					+ sFile.getName()
					+ "\n" + ex.getMessage(),
					"Save object",
					JOptionPane.ERROR_MESSAGE);
	        }
        }
    }

    /** This method creates a file chooser
     */
    protected void createFileChooser() {
        m_FileChooser = new JFileChooser(new File("/resources"));
        m_FileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
    }

    /**This is used to hook an action listener to the ok button
     * @param a The action listener.
     */
    public void addOkListener(ActionListener a) {
        m_okBut.addActionListener(a);
    }

    /**This is used to hook an action listener to the cancel button
     * @param a The action listener.
     */
    public void addCancelListener(ActionListener a) {
        m_cancelBut.addActionListener(a);
    }

    /** This is used to remove an action listener from the ok button
     * @param a The action listener
     */
    public void removeOkListener(ActionListener a) {
        m_okBut.removeActionListener(a);
    }

    /** This is used to remove an action listener from the cancel button
     * @param a The action listener
     */
    public void removeCancelListener(ActionListener a) {
        m_cancelBut.removeActionListener(a);
    }
    /** This method updates the class type*
     */
    protected void updateClassType() {
        m_ClassesLongName = GenericObjectEditor.getClassesFromProperties(m_ObjectEditor.getClassType().getName(), null);
        m_ObjectChooser.setModel(new DefaultComboBoxModel(m_ClassesLongName.toArray()));
        if (m_ClassesLongName.size() > 1) {
            add(m_ObjectChooser, BorderLayout.NORTH);
        }
        else {
            remove(m_ObjectChooser);
        }
    }

    protected void updateChooser() {
        String  objectName  = /*EVAHELP.cutClassName*/ (this.m_ObjectEditor.getValue().getClass().getName());
        boolean found       = false;
        for (int i = 0; i < m_ObjectNames.getSize(); i++) {
	        if (objectName.equals((String)m_ObjectNames.getElementAt(i))) {
	            found = true;
	            break;
	        }
        }
        if (!found) {
            m_ObjectNames.addElement(objectName);
        }
        m_ObjectChooser.getModel().setSelectedItem(objectName);
    }

    /** Updates the child property sheet, and creates if needed..
     * *what!?*
     */
    public void updateChildPropertySheet() {
        // Set the object as the target of the propertysheet
//           PropertyEditor edit = PropertyEditorManager.findEditor(m_Object.getClass());
//            if (!(edit instanceof GenericObjectEditor)) {
//                System.out.println("This is my chance to change something!");
//                System.out.println("Class:  " + m_Object.getClass());
//                if (edit != null) System.out.println("Editor: " + edit.getClass());
//                else System.out.println("No editor found for class " + m_Object.getClass());
//                //edit.setValue(c);
//            }
        this.m_PPWrapper.removeAll();
        this.m_PPWrapper.add(this.m_ObjectEditor.getPropertyPanel());
//        m_ChildPropertySheet.setTarget(this.m_ObjectEditor.getValue());
        // Adjust size of containing window if possible
        if ((getTopLevelAncestor() != null)  && (getTopLevelAncestor() instanceof Window)) {
	        ((Window) getTopLevelAncestor()).pack();
        }
    }

    /** When the chooser selection is changed, ensures that the Object
     * is changed appropriately.
     * @param e a value of type 'ItemEvent'
     */
    @Override
    public void itemStateChanged(ItemEvent e) {
        String className = (String)m_ObjectChooser.getSelectedItem();

        //System.out.println("Event-Quelle: " + e.getSource().toString());
        if ((e.getSource() == m_ObjectChooser) && (e.getStateChange() == ItemEvent.SELECTED)) {
            className = (String)m_ObjectChooser.getSelectedItem();
            try {
                System.out.println(className);
                Object n = (Object)Class.forName(className).newInstance();
                this.m_ObjectEditor.setValue(n);
            } catch (Exception ex) {
                System.out.println("Exeption in itemStateChanged"+ex.getMessage());
                m_ObjectChooser.hidePopup();
                m_ObjectChooser.setSelectedIndex(0);
                JOptionPane.showMessageDialog(this,
                        "Could not create an example of\n"
                        + className + "\n"
                        + "from the current classpath",
                        "GenericObjectEditor",
                        JOptionPane.ERROR_MESSAGE);
                EVAHELP.getSystemPropertyString();
            }
        }
    }
}