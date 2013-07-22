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

    protected T[] m_Objects;
    protected boolean[] m_Selection;
    private transient PropertyChangeSupport m_Support = new PropertyChangeSupport(this);

    //    public PropertySelectableList() {
//    }
//    
    public PropertySelectableList(T[] initial) {
        m_Objects = initial;
        m_Selection = new boolean[initial.length];
    }

    public PropertySelectableList(PropertySelectableList<T> b) {
        if (b.m_Objects != null) {
            this.m_Objects = b.m_Objects.clone();
        }
        if (b.m_Selection != null) {
            this.m_Selection = new boolean[b.m_Selection.length];
            System.arraycopy(b.m_Selection, 0, this.m_Selection, 0, this.m_Selection.length);
        }
    }

    @Override
    public Object clone() {
        return (Object) new PropertySelectableList<T>(this);
    }

    public void setObjects(T[] o) {
        this.m_Objects = o;
        this.m_Selection = new boolean[o.length];
        m_Support.firePropertyChange("PropertySelectableList", null, this);
    }

    public void setObjects(T[] o, boolean[] selection) {
        this.m_Objects = o;
        this.m_Selection = selection;
        if (o.length != selection.length) {
            throw new RuntimeException("Error, mismatching length of arrays in " + this.getClass());
        }
        m_Support.firePropertyChange("PropertySelectableList", null, this);
    }

    public T[] getObjects() {
        return this.m_Objects;
    }

    /**
     * Returns the elements represented by this list where only the selected elements are non-null.
     *
     * @return
     */
    public T[] getSelectedObjects() {
        T[] selObjects = getObjects().clone();
        for (int i = 0; i < selObjects.length; i++) {
            if (!m_Selection[i]) {
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
        m_Selection = new boolean[getObjects().length];
        for (int i = 0; i < selection.length; i++) {
            m_Selection[selection[i]] = true;
        }
        m_Support.firePropertyChange("PropertySelectableList", null, this);
    }

    public void setSelection(boolean[] selection) {
        this.m_Selection = selection;
        m_Support.firePropertyChange("PropertySelectableList", null, this);
    }

    public boolean[] getSelection() {
        return this.m_Selection;
    }

    public void setSelectionForElement(int index, boolean b) {
        if (m_Selection[index] != b) {
            this.m_Selection[index] = b;
            m_Support.firePropertyChange("PropertySelectableList", null, this);
        }
    }

    public int size() {
        if (m_Objects == null) {
            return 0;
        } else {
            return m_Objects.length;
        }
    }

    public T get(int i) {
        return m_Objects[i];
    }

    public boolean isSelected(int i) {
        return m_Selection[i];
    }

    public void clear() {
        m_Objects = null;
        m_Selection = null;
        m_Support.firePropertyChange("PropertySelectableList", null, this);
    }

//	/**
//	 * Append an object at the end of the list and immediately select it.
//	 * @param o
//	 */
//	public void addObject(T o) {
//		if (m_Objects==null) {
//			m_Objects=(T[]) new Object[]{o};
//			m_Selection = new boolean[1];
//		} else {
//			T[] newOs = (T[])new Object[m_Objects.length+1];
//			boolean[] newSs = new boolean[m_Selection.length+1];
//			System.arraycopy(m_Objects, 0, newOs, 0, this.m_Objects.length);
//			System.arraycopy(m_Selection, 0, newSs, 0, this.m_Selection.length);
//			newOs[m_Objects.length]=o;
//			newSs[m_Objects.length]=true;
//			m_Objects=newOs;
//			m_Selection=newSs;
//		}
//		m_Support.firePropertyChange("PropertySelectableList", null, this);
//	}

    public void addPropertyChangeListener(PropertyChangeListener l) {
        if (m_Support == null) {
            m_Support = new PropertyChangeSupport(this);
        }
        m_Support.addPropertyChangeListener(l);
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
        if (m_Support == null) {
            m_Support = new PropertyChangeSupport(this);
        }
        m_Support.removePropertyChangeListener(l);
    }
}
