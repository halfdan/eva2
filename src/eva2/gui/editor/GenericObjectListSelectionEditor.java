package eva2.gui.editor;

import eva2.gui.PropertySelectableList;

/**
 * An editor for a selectable List.
 */
public class GenericObjectListSelectionEditor extends AbstractListSelectionEditor {
    private PropertySelectableList objList;

    public GenericObjectListSelectionEditor() {
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
    protected boolean isElementSelected(int i) {
        return objList.isSelected(i);
    }

    @Override
    protected boolean actionOnSelect() {
        boolean changed = false;
        for (int i = 0; i < this.blackCheck.length; i++) {
            if (objList.isSelected(i) != this.blackCheck[i].isSelected()) {
                objList.setSelectionForElement(i, this.blackCheck[i].isSelected());
                changed = true;
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
        } else {
            return false;
        }
    }

    /**
     * Retruns the current object.
     *
     * @return the current object
     */
    @Override
    public Object getValue() {
        return objList;
    }
}