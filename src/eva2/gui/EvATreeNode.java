package eva2.gui;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * The node class for the EvA2 tree view panel. Each node contains a parameter object.
 * Typically, the tree hierarchy starts with a
 * OptimizationParameters object, however this is not necessary.
 * The tree is constructed using the reflection functionality of PropertySheetPanel
 * which is also used to generate the nested panels for parameter configuration.
 * 
 * @see PropertySheetPanel
 * @see GOParameters
 * @author mkron
 *
 */
public class EvATreeNode extends DefaultMutableTreeNode  {
	private String[] childrenNames = null;
	private Object[] childrenValues = null;
	private String myName="EvATreeNode";
	private boolean doListPrimitives=false;

	/**
	 * A default constructor setting the name and target object of
	 * the tree. The children are generated immediately.
	 * The node label is constructed from the name String and the
	 * information retrieved from the target object if it implements the getName method.
	 * 
	 * @param name title of the node
	 * @param target
	 */
	public EvATreeNode(String name, Object target) {
		super(target);
		myName = name;
		setObject(target, true);
	}

	/**
	 * Set the target object of the tree. Note that the name is not automatically
	 * updated and may be out of date if the new target object is incompatible to the
	 * old name.
	 * 
	 * @param target	the new target object
	 * @param expand	should be true to generate child nodes immediately
	 */
	public void setObject(Object target, boolean expand) {
		super.setUserObject(target);
		childrenNames = PropertySheetPanel.getPropertyNames(target);
		childrenValues = PropertySheetPanel.getPropertyValues(target, true, true, true);
		super.removeAllChildren();
		if (expand) {
                initChildren();
            }
	}

	public void setName(String name) {
		myName=name;
	}
	
	public String getName() {
		return myName;
	}
	
	/**
	 * Actually create child nodes.
	 */
	private void initChildren() {
		for (int i=0; i<childrenValues.length; i++) {
			if (childrenValues[i]!=null) {
				if (doListPrimitives || !(BeanInspector.isJavaPrimitive(childrenValues[i].getClass()))) {
                                super.add(new EvATreeNode(childrenNames[i], childrenValues[i]));
                            }
			}
		}
	}

	@Override
	public String toString() {
		String extendedInfo=null;
		try {
			extendedInfo=(String)BeanInspector.callIfAvailable(this.getUserObject(), "getName", new Object[]{});
		} catch (Exception e) {
			extendedInfo=null;
		}
		if (extendedInfo != null) {
                return myName + " - "+ extendedInfo;
            }
		else {
                return myName;
            }
	}
}
