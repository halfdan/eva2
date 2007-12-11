package javaeva.server.go.tools;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.beans.PropertyEditor;
import java.io.*;

/**
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 20.04.2005
 * Time: 11:41:29
 * To change this template use File | Settings | File Templates.
 */
public class GeneralGEOFaker extends JPanel {

    private JButton                 open, save, ok, edit;
    private JFileChooser m_FileChooser;
//    private Vector                  m_ClassesLongName;
//    private SourceCodeEditor        m_SourceCodeEditor;
//    private PropertyDialog          m_SourceCodeEditorFrame;
    private PropertyEditor          m_Editor;
    private JPanel                  m_Interior;
    private Class                   m_ClassType;

    public GeneralGEOFaker(PropertyEditor e, JPanel i) {
        this.m_Interior     = i;
        this.m_Editor       = e;
        this.m_ClassType    = this.m_Editor.getValue().getClass();
        init();
    }

    private void init() {
        this.setLayout(new BorderLayout());

        open = new JButton("Open...");
        open.setToolTipText("Load a configured object");
        open.setEnabled(true);
        open.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Object object = openObject();
                if (object != null) {
                    // setValue takes care of: Making sure obj is of right type,
                    // and firing property change.
                    m_Editor.setValue(object);
                    // Need a second setValue to get property values filled in OK.
                    // Not sure why.
                    m_Editor.setValue(object); // <- Hannes ?!?!?
                }
            }
        });

        save = new JButton("Save...");
        save.setToolTipText("Save the current configured object");
        save.setEnabled(true);
        save.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                saveObject(m_Editor.getValue());
            }
        });

        edit = new JButton("Edit Source");
        edit.setToolTipText("Edit the Source");
        edit.setEnabled(false);
//        edit.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent e) {
//                edit.setEnabled(false);
//                m_SourceCodeEditor = new SourceCodeEditor();
//                String className = m_Editor.getValue().getClass().getName();
//                m_SourceCodeEditor.editSource(EvAClient.DYNAMICCLASSES_PROPERTIES.getProperty(className));
//                m_SourceCodeEditorFrame = new PropertyDialog(m_SourceCodeEditor, "test", 50, 50);
//                m_SourceCodeEditorFrame.pack();
//                m_SourceCodeEditorFrame.addWindowListener(new WindowAdapter() {
//                    public void windowClosing (WindowEvent e) {
//                        m_SourceCodeEditor = null;
//                        edit.setEnabled(true);
//                    }
//                });
//                m_SourceCodeEditor.addPropertyChangeListener(new PropertyChangeListener() {
//                    public void propertyChange(PropertyChangeEvent evt) {
//                        sourceChanged();
//                    }
//                });
//            }
//        });

        ok = new JButton("OK");
        ok.setEnabled(true);
        ok.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
	        if ((getTopLevelAncestor() != null) && (getTopLevelAncestor() instanceof Window)) {
	            Window w = (Window) getTopLevelAncestor();
	            w.dispose();
	        }
	    }});
        setLayout(new BorderLayout());
        add(this.m_Interior, BorderLayout.CENTER);
        JPanel okcButs = new JPanel();
        okcButs.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        okcButs.setLayout(new GridLayout(1, 4, 5, 5));
        okcButs.add(open);
        okcButs.add(save);
        okcButs.add(ok);
        okcButs.add(edit);
        add(okcButs, BorderLayout.SOUTH);
    }

    /** Opens an object from a file selected by the user.
     * @return the loaded object, or null if the operation was cancelled
     */
    protected Object openObject() {
        if (m_FileChooser == null) createFileChooser();
        int returnVal = m_FileChooser.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
	        File selected = m_FileChooser.getSelectedFile();
	        try {
	            ObjectInputStream   oi  = new ObjectInputStream(new BufferedInputStream(new FileInputStream(selected)));
                Object              obj = oi.readObject();
                oi.close();
                if (!m_ClassType.isAssignableFrom(obj.getClass())) {
                    throw new Exception("Object not of type: " + m_ClassType.getName());
                }
                return obj;
	        } catch (Exception ex) {
	            JOptionPane.showMessageDialog(this, "Couldn't read object: " + selected.getName() + "\n" + ex.getMessage(), "Open object file", JOptionPane.ERROR_MESSAGE);
	        }
        }
        return null;
    }

    /** Saves the current object to a file selected by the user.
     * @param object    The object to save.
     */
    protected void saveObject(Object object) {
        if (m_FileChooser == null) createFileChooser();
        int returnVal = m_FileChooser.showSaveDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
	        File sFile = m_FileChooser.getSelectedFile();
	        try {
	            ObjectOutputStream oo = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(sFile)));
                oo.writeObject(object);
                oo.close();
	        } catch (Exception ex) {
	            JOptionPane.showMessageDialog(this, "Couldn't write to file: " + sFile.getName() + "\n" + ex.getMessage(), "Save object", JOptionPane.ERROR_MESSAGE);
	        }
        }
    }
    protected void createFileChooser() {
        m_FileChooser = new JFileChooser(new File("/resources"));
        m_FileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
    }
}
