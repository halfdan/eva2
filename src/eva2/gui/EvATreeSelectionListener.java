package eva2.gui;

import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyEditor;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

/**
 * Listener for use with the EvATreeNode class. It implements both the tree selection listener
 * to react to selection changes in the tree view (and update the parameter panel),
 * and the property change listener to
 * react to changes in the parameters (and update the tree).
 *
 * @author mkron
 */
public class EvATreeSelectionListener implements TreeSelectionListener, PropertyChangeListener {
    private PropertyEditor goe = null;
    private EvATreeNode root = null;
    private JTree jtree = null;
    public static final boolean TRACE = true;

    /**
     * Create a tree listener and hook it up in the editor to listen to parameter changes
     * and in the JTree to update it.
     *
     * @param rootNode the root node of the tree
     * @param goEditor the editor containing the parameter panel
     * @param jt       the GUI view of the tree
     */
    public EvATreeSelectionListener(EvATreeNode rootNode, PropertyEditor goEditor, JTree jt) {
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
        if (TRACE) {
            System.out.println("valueChanged to " + BeanInspector.toString(e.getPath()));
        }
        TreePath tp = e.getPath();
        if (TRACE) {
            for (int i = tp.getPathCount() - 1; i >= 0; i--) {
                System.out.println("* " + i + " " + tp.getPathComponent(i));
            }
        }
        EvATreeNode leafNode = (EvATreeNode) tp.getLastPathComponent();
//		goe.setValue(leafNode.getUserObject());
        Component editComp = goe.getCustomEditor();
        if (editComp instanceof GOEPanel) {
            // update the object in the main GOEPanel
            ((GOEPanel) editComp).setTarget(leafNode.getUserObject());
        } else {
            System.err.println("Error, unable to notify custom editor of type " + editComp.getClass() + ", expected GOEPanel (EvATreeSelectionListener)");
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (TRACE) {
            System.out.println("EvATreeNode received change event " + evt);
        }
        root.setObject(evt.getNewValue(), true);
        if (jtree != null) {
            jtree.setModel(new DefaultTreeModel(root));
        } // TODO this should be done differently so that the tree is not collapsed on each change!
    }
}
