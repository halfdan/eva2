package eva2.gui;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Keymap;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.*;
import java.util.Hashtable;


public class JTextEditorInternalFrame extends JDocFrame {
    public final static String GROUP_EDIT = "Edit";
    private JTextArea textArea;
    private final String[] actionGroups = {GROUP_EDIT};
    protected UndoManager undo = new UndoManager();

    private class UndoAction extends ExtAction {
        public UndoAction() {
            super("R�ckg�ngig", new ImageIcon("images/EditUndo.gif"), "Macht die letzte Aktion r�ckg�ngig",
                    KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_MASK));
            setEnabled(false);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                undo.undo();
            } catch (CannotUndoException exc) {
            }

            update();
            actRedo.update();
        }

        private void update() {
            if (undo.canUndo()) {
                setEnabled(true);
                putValue(Action.NAME, undo.getUndoPresentationName());
            } else {
                setEnabled(false);
                putValue(Action.NAME, "R�ckg�ngig");
            }
        }
    }  // end of inner class UndoAction

    ///////////////////////////////////////////
    //
    ///////////////////////////////////////////
    private class RedoAction extends ExtAction {
        public RedoAction() {
            super("Wiederholen", "Wiederholt die letzte Aktion", KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK));
            setEnabled(false);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                undo.redo();
            } catch (CannotRedoException exc) {
            }

            update();
            actUndo.update();
        }

        private void update() {
            if (undo.canRedo()) {
                setEnabled(true);
                putValue(Action.NAME, undo.getRedoPresentationName());
            } else {
                setEnabled(false);
                putValue(Action.NAME, "Wiederholen");
            }
        }
    } // end of inner class RedoAction

    private UndoAction actUndo;
    private RedoAction actRedo;
    public final static String undoAction = "undo";
    public final static String redoAction = "redo";

    ///////////////////////////////////////////
    //
    /////////////////////////////////////////
    @Override
    public String[] getActionGroups() {
        return actionGroups;
    }

    private JMenu mnuEdit;
    private JToolBar barEdit;

    @Override
    public JMenu getMenu(String group) {
        if (GROUP_EDIT.equals(group)) {
            return mnuEdit;
        } else {
            return null;
        }
    }

    @Override
    public JToolBar getToolBar(String group) {
        if (GROUP_EDIT.equals(group)) {
            return barEdit;
        }
        return null;
    }

    private Hashtable hashActions = new Hashtable();

    private Action cloneAction(Action a) {
        Action result = null;

        try {
            ByteArrayOutputStream bOut = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(bOut);
            out.writeObject(a);
            ByteArrayInputStream bIn = new ByteArrayInputStream(bOut.toByteArray());
            ObjectInputStream in = new ObjectInputStream(bIn);
            result = (Action) in.readObject();
        } catch (Exception exc) {
        }

        return result;
    }

    ///////////////////////////////////////////////
    //
    //////////////////////////////////////////////
    private void createActions() {
        hashActions.put(undoAction, actUndo = new UndoAction());
        hashActions.put(redoAction, actRedo = new RedoAction());

        Action[] actions = textArea.getActions();
        for (int i = 0; i < actions.length; i++) {
            hashActions.put(actions[i].getValue(Action.NAME), actions[i]);
        }

        mnuEdit = new JExtMenu("&Bearbeiten");
        barEdit = new JExtToolBar();

        Action a;
        Keymap keys = textArea.getKeymap();
        KeyStroke[] keyActions;

        mnuEdit.add(actUndo);
        barEdit.add(actUndo);
        mnuEdit.add(actRedo);
        mnuEdit.addSeparator();

        a = (Action) hashActions.get(DefaultEditorKit.cutAction);
        keyActions = keys.getKeyStrokesForAction(a);
        if (keyActions != null && keyActions.length > 0) {
            a.putValue(ExtAction.KEYSTROKE, keyActions[0]);
        }
        a.putValue(Action.SMALL_ICON, new ImageIcon("images/EditCut.gif"));
        a.putValue(ExtAction.CAPTION, "Ausschneiden");
        a.putValue(ExtAction.MNEMONIC, 'a');
        a.putValue(ExtAction.TOOLTIP, "Schneidet den markierten Text aus und setzt ihn in die Zwischenablage");
        mnuEdit.add(a);
        barEdit.add(a);

        a = (Action) hashActions.get(DefaultEditorKit.copyAction);
        keyActions = keys.getKeyStrokesForAction(a);
        if (keyActions != null && keyActions.length > 0) {
            a.putValue(ExtAction.KEYSTROKE, keyActions[0]);
        }
        a.putValue(Action.SMALL_ICON, new ImageIcon("images/EditCopy.gif"));
        a.putValue(ExtAction.CAPTION, "Kopieren");
        a.putValue(ExtAction.MNEMONIC, 'k');
        a.putValue(ExtAction.TOOLTIP, "Kopiert den markierten Text in die Zwischenablage");
        mnuEdit.add(a);
        barEdit.add(a);

        a = (Action) hashActions.get(DefaultEditorKit.pasteAction);
        keyActions = keys.getKeyStrokesForAction(a);
        if (keyActions != null && keyActions.length > 0) {
            a.putValue(ExtAction.KEYSTROKE, keyActions[0]);
        }
        a.putValue(Action.SMALL_ICON, new ImageIcon("images/EditPaste.gif"));
        a.putValue(ExtAction.CAPTION, "Einf�gen");
        a.putValue(ExtAction.MNEMONIC, 'e');
        a.putValue(ExtAction.TOOLTIP, "F�gt Text aus der Zwischenablage ein");
        mnuEdit.add(a);
        barEdit.add(a);

        mnuEdit.addSeparator();

        a = (Action) hashActions.get(DefaultEditorKit.selectAllAction);
        keyActions = keys.getKeyStrokesForAction(a);
        if (keyActions != null && keyActions.length > 0) {
            a.putValue(ExtAction.KEYSTROKE, keyActions[0]);
        }
        a.putValue(ExtAction.CAPTION, "Alles markieren");
        a.putValue(ExtAction.MNEMONIC, 'm');
        a.putValue(ExtAction.TOOLTIP, "Markiert das ganze Dokument");
        mnuEdit.add(a);
    }

    //////////////////////////////////////////
    //
    /////////////////////////////////////////
    private void createTextArea() {
        textArea = new JTextArea();
        getContentPane().add(new JScrollPane(textArea));
    }

    /////////////////////////////////////
    //
    /////////////////////////////////////
    private void createListeners() {
        textArea.getDocument().addDocumentListener(new DocumentListener() {
            private void changed() {
                setChanged(true);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                changed();
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                changed();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                changed();
            }
        });

        textArea.getDocument().addUndoableEditListener(new UndoableEditListener() {
            @Override
            public void undoableEditHappened(UndoableEditEvent e) {
                undo.addEdit(e.getEdit());
                actUndo.update();
                actRedo.update();
            }
        });
    }

    ////////////////////////////////////////////////////
    //
    ///////////////////////////////////////////////////
    public JTextEditorInternalFrame(String title) {
        super(title);
        createTextArea();
        createListeners();
        createActions();
    }

    //////////////////////////////////////////////////
    //
    /////////////////////////////////////////////////
    public JTextEditorInternalFrame(File file) {
        super(file);
        createTextArea();

        if (file.exists()) {
            FileReader in = null;
            try {
                in = new FileReader(file);
                textArea.read(in, null);
            } catch (IOException exc) {
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException exc) {
                    }
                }
            }
        }

        createListeners();
        createActions();
    }

    @Override
    public void save(File f) {
        FileWriter out = null;
        try {
            out = new FileWriter(f);
            textArea.write(out);
        } catch (IOException exc) {
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException exc) {
                }
            }
        }

        super.save(f);
    }

    @Override
    public void setSelected(boolean value) throws java.beans.PropertyVetoException {
        super.setSelected(value);

        if (value) {
            textArea.requestFocus();
        }
    }
}
