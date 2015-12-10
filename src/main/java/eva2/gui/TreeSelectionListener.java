package eva2.gui;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyEditor;

/**
 * Listener for use with the TreeNode class. It implements both the tree selection listener
 * to react to selection changes in the tree view (and update the parameter panel),
 * and the property change listener to
 * react to changes in the parameters (and update the tree).
 *
 * @author mkron
 */
public class TreeSelectionListener implements javax.swing.event.TreeSelectionListener, PropertyChangeListener {
    private PropertyEditor goe = null;
    private TreeNode root = null;
    private JTree jtree = null;

    /**
     * Create a tree listener and hook it up in the editor to listen to parameter changes
     * and in the JTree to update it.
     *
     * @param rootNode the root node of the tree
     * @param goEditor the editor containing the parameter panel
     * @param jt       the GUI view of the tree
     */
    public TreeSelectionListener(TreeNode rootNode, PropertyEditor goEditor, JTree jt) {
        goe = goEditor;
        root = rootNode;
        jtree = jt;

        if (jtree != null) {
            jtree.addTreeSelectionListener(this);
        } // listen to tree selection changes
        if (goEditor != null) {
            goEditor.addPropertyChangeListener(this);
        } // listen to changes to the parameters
    }

    @Override
    public void valueChanged(TreeSelectionEvent e) {
        TreePath tp = e.getPath();

        TreeNode leafNode = (TreeNode) tp.getLastPathComponent();
        Component editComp = goe.getCustomEditor();
        if (editComp instanceof OptimizationEditorPanel) {
            // update the object in the main OptimizationEditorPanel
            ((OptimizationEditorPanel) editComp).setTarget(leafNode.getUserObject());
        } else {
            System.err.println("Error, unable to notify custom editor of type " + editComp.getClass() + ", expected OptimizationEditorPanel (TreeSelectionListener)");
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        root.setObject(evt.getNewValue(), true);
        if (jtree != null) {
            jtree.setModel(new DefaultTreeModel(root));
        } // TODO this should be done differently so that the tree is not collapsed on each change!
    }
}
