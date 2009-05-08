package eva2.gui;

/**
 * An editor for a selectable List.
 * 
 */
public class GenericObjectListSelectionEditor extends AbstractListSelectionEditor {
    private PropertySelectableList objList;
    
    public GenericObjectListSelectionEditor() {
        // compiled code
    }
    
	@Override
	protected int getElementCount() {
		return objList.size();
	}

	@Override
	protected String getElementName(int i) {
		return objList.get(i).toString();
	}

	@Override
	protected boolean isElementAllowed(int i) {
		return objList.isSelected(i);
	}

	@Override
	protected boolean performOnAction() {
		boolean changed=false;
		for (int i = 0; i < this.m_BlackCheck.length; i++) {
			if (objList.isSelected(i)!=this.m_BlackCheck[i].isSelected()) {
				objList.setSelectionForElement(i, this.m_BlackCheck[i].isSelected());
				changed=true;
			}
		}
		return changed;
	}

	@Override
	protected boolean setObject(Object o) {
        if (o instanceof PropertySelectableList) {
            this.objList = (PropertySelectableList) o;
            objList.addPropertyChangeListener(this);
            return true;
        } else return false;
	}

    /** Retruns the current object.
     * @return the current object
     */
    public Object getValue() {
        return objList;
    }
}