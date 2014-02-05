package eva2.gui;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 23.03.2004
 * Time: 15:04:05
 * To change this template use File | Settings | File Templates.
 */
public class PropertySelectableList<T> implements java.io.Serializable {

    protected T[] objects;
    protected boolean[] selections;
    private transient PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    //    public PropertySelectableList() {
//    }
//    
    public PropertySelectableList(T[] initial) {
        objects = initial;
        selections = new boolean[initial.length];
    }

    public PropertySelectableList(PropertySelectableList<T> b) {
        if (b.objects != null) {
            this.objects = b.objects.clone();
        }
        if (b.selections != null) {
            this.selections = new boolean[b.selections.length];
            System.arraycopy(b.selections, 0, this.selections, 0, this.selections.length);
        }
    }

    @Override
    public Object clone() {
        return (Object) new PropertySelectableList<T>(this);
    }

    public void setObjects(T[] o) {
        this.objects = o;
        this.selections = new boolean[o.length];
        propertyChangeSupport.firePropertyChange("PropertySelectableList", null, this);
    }

    public void setObjects(T[] o, boolean[] selection) {
        this.objects = o;
        this.selections = selection;
        if (o.length != selection.length) {
            throw new RuntimeException("Error, mismatching length of arrays in " + this.getClass());
        }
        propertyChangeSupport.firePropertyChange("PropertySelectableList", null, this);
    }

    public T[] getObjects() {
        return this.objects;
    }

    /**
     * Returns the elements represented by this list where only the selected elements are non-null.
     *
     * @return
     */
    public T[] getSelectedObjects() {
        T[] selObjects = getObjects().clone();
        for (int i = 0; i < selObjects.length; i++) {
            if (!selections[i]) {
                selObjects[i] = null;
            }
        }
        return selObjects;
    }

    /**
     * Set the selection by giving a list of selected indices.
     *
     * @param selection
     */
    public void setSelectionByIndices(int[] selection) {
        selections = new boolean[getObjects().length];
        for (int i = 0; i < selection.length; i++) {
            selections[selection[i]] = true;
        }
        propertyChangeSupport.firePropertyChange("PropertySelectableList", null, this);
    }

    public void setSelection(boolean[] selection) {
        this.selections = selection;
        propertyChangeSupport.firePropertyChange("PropertySelectableList", null, this);
    }

    public boolean[] getSelection() {
        return this.selections;
    }

    public void setSelectionForElement(int index, boolean b) {
        if (selections[index] != b) {
            this.selections[index] = b;
            propertyChangeSupport.firePropertyChange("PropertySelectableList", null, this);
        }
    }

    public int size() {
        if (objects == null) {
            return 0;
        } else {
            return objects.length;
        }
    }

    public T get(int i) {
        return objects[i];
    }

    public boolean isSelected(int i) {
        return selections[i];
    }

    public void clear() {
        objects = null;
        selections = null;
        propertyChangeSupport.firePropertyChange("PropertySelectableList", null, this);
    }

//	/**
//	 * Append an object at the end of the list and immediately select it.
//	 * @param o
//	 */
//	public void addObject(T o) {
//		if (objects==null) {
//			objects=(T[]) new Object[]{o};
//			selections = new boolean[1];
//		} else {
//			T[] newOs = (T[])new Object[objects.length+1];
//			boolean[] newSs = new boolean[selections.length+1];
//			System.arraycopy(objects, 0, newOs, 0, this.objects.length);
//			System.arraycopy(selections, 0, newSs, 0, this.selections.length);
//			newOs[objects.length]=o;
//			newSs[objects.length]=true;
//			objects=newOs;
//			selections=newSs;
//		}
//		propertyChangeSupport.firePropertyChange("PropertySelectableList", null, this);
//	}

    public void addPropertyChangeListener(PropertyChangeListener l) {
        if (propertyChangeSupport == null) {
            propertyChangeSupport = new PropertyChangeSupport(this);
        }
        propertyChangeSupport.addPropertyChangeListener(l);
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
        if (propertyChangeSupport == null) {
            propertyChangeSupport = new PropertyChangeSupport(this);
        }
        propertyChangeSupport.removePropertyChangeListener(l);
    }
}
